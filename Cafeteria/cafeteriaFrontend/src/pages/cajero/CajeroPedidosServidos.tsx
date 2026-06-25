import React, { useState, useEffect } from 'react';
import { ClipboardList, FileText, Eye } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { facturaService } from '../../services/facturaService';
import { useAuth } from '../../context/AuthContext';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function CajeroPedidosServidos() {
  const { user } = useAuth();
  const [pedidos, setPedidos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState<any>(null);
  const [creando, setCreando] = useState(false);

  const load = async () => {
    try {
      const res = await pedidoService.porEstado('SERVIDO');
      setPedidos((res.data.data || []).reverse());
    } catch {} finally { setLoading(false); }
  };
  useEffect(() => { load(); }, []);

  const crearFactura = async (pedidoId: number) => {
    if (!user) return;
    setCreando(true);
    try {
      await facturaService.desdePedido(pedidoId, user.id);
      toast.success('Factura creada — ve a Facturas para cobrar');
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error al crear factura');
    }
    setCreando(false);
  };

  if (loading) return <LoadingSpinner />;
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Pedidos Servidos</h1>
        <p className="text-gray-500 text-sm mt-1">Crea facturas desde los pedidos listos para cobrar</p>
      </div>

      {pedidos.length === 0 ? (
        <EmptyState title="Sin pedidos servidos" description="Los pedidos aparecen aquí cuando el mesero los marca como servidos" icon={ClipboardList} />
      ) : (
        <div className="space-y-4">{pedidos.map((p: any) => (
          <div key={p.id} className="card overflow-hidden">
            <div className="px-4 py-3 bg-green-50 dark:bg-green-900/10 border-b border-green-100 dark:border-green-900/20 flex items-center justify-between flex-wrap gap-2">
              <div className="flex items-center gap-3">
                <span className="font-mono font-bold text-lg">#{p.id}</span>
                <span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa || '—'}</span>
                <StatusBadge status={p.estado} />
              </div>
              <button onClick={() => crearFactura(p.id)} disabled={creando} className="btn-gold btn-sm">
                <FileText className="w-4 h-4" />{creando ? 'Creando...' : 'Crear Factura'}
              </button>
            </div>
            <div className="p-4">
              <div className="space-y-2">
                {p.detalles && p.detalles.length > 0 ? p.detalles.map((d: any, i: number) => (
                  <div key={i} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="w-6 h-6 bg-brand-50 dark:bg-brand-900/20 text-brand-500 rounded-md flex items-center justify-center text-xs font-bold">{d.cantidad}</span>
                      <span className="text-sm text-gray-700 dark:text-gray-300">{d.producto?.nombre}</span>
                    </div>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">{money(d.subtotal || (d.precioUnitario || 0) * d.cantidad)}</span>
                  </div>
                )) : <p className="text-sm text-gray-400">Sin detalles</p>}
              </div>
              {p.notas && <p className="text-xs text-amber-600 dark:text-amber-400 italic mt-3 pt-2 border-t border-gray-100 dark:border-gray-800">📝 {p.notas}</p>}
              <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100 dark:border-gray-800">
                <span className="text-xs text-gray-400">{p.cliente ? `${p.cliente.nombre} ${p.cliente.apellido}` : ''} · {dateTime(p.fechaCreacion)}</span>
                <span className="text-lg font-bold text-brand-500">{money(p.total)}</span>
              </div>
            </div>
          </div>
        ))}</div>
      )}
    </div>
  );
}
