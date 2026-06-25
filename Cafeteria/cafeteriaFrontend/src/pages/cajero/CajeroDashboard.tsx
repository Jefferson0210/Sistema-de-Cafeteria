import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { FileText, ClipboardList, DollarSign, Clock, CheckCircle, CreditCard } from 'lucide-react';
import { facturaService } from '../../services/facturaService';
import { pedidoService } from '../../services/pedidoService';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { money, dateTime } from '../../utils/format';

export default function CajeroDashboard() {
  const nav = useNavigate();
  const [facturas, setFacturas] = useState<any[]>([]);
  const [pedidosServidos, setPedidosServidos] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    Promise.all([facturaService.listar(), pedidoService.porEstado('SERVIDO')])
      .then(([f, p]) => {
        setFacturas((f.data.data || []).reverse());
        setPedidosServidos(p.data.data || []);
      })
      .catch(() => {})
      .finally(() => setLoading(false));
  }, []);

  if (loading) return <LoadingSpinner />;

  const pendientes = facturas.filter(f => f.estado === 'PENDIENTE');
  const pagadas = facturas.filter(f => f.estado === 'PAGADA');
  const totalCobrado = pagadas.reduce((s: number, f: any) => s + (f.total || 0), 0);
  const totalPendiente = pendientes.reduce((s: number, f: any) => s + (f.total || 0), 0);

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard Cajero</h1>
        <p className="text-gray-500 text-sm mt-1">Resumen de facturación</p>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Por Servir" value={pedidosServidos.length} icon={ClipboardList} color="text-amber-600" bgColor="bg-amber-50" onClick={() => nav('/cajero/pedidos-servidos')} />
        <StatCard label="Pendientes" value={pendientes.length} icon={Clock} color="text-red-600" bgColor="bg-red-50" onClick={() => nav('/cajero/facturas')} />
        <StatCard label="Cobradas" value={pagadas.length} icon={CheckCircle} color="text-emerald-600" bgColor="bg-emerald-50" onClick={() => nav('/cajero/facturas')} />
        <StatCard label="Total Cobrado" value={money(totalCobrado)} icon={DollarSign} color="text-brand-500" bgColor="bg-brand-50" />
      </div>

      {/* Facturas pendientes de cobro */}
      {pendientes.length > 0 && (
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2">
            <CreditCard className="w-5 h-5 text-red-500" />Pendientes de cobro
          </h3>
          {pendientes.slice(0, 5).map((f: any) => (
            <div key={f.id} className="card p-4 cursor-pointer hover:shadow-md transition-shadow" onClick={() => nav('/cajero/facturas')}>
              <div className="flex items-center justify-between">
                <div>
                  <div className="flex items-center gap-2 mb-1">
                    <span className="font-mono font-semibold text-sm text-gray-900 dark:text-white">{f.numeroFactura}</span>
                    <StatusBadge status={f.estado} />
                  </div>
                  <p className="text-sm text-gray-500">{f.cliente ? `${f.cliente.nombre} ${f.cliente.apellido}` : 'Sin cliente'}</p>
                </div>
                <p className="text-lg font-bold text-brand-500">{money(f.total)}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Pedidos servidos sin factura */}
      {pedidosServidos.length > 0 && (
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2">
            <ClipboardList className="w-5 h-5 text-amber-500" />Pedidos listos para facturar
          </h3>
          {pedidosServidos.slice(0, 5).map((p: any) => (
            <div key={p.id} className="card p-4 cursor-pointer hover:shadow-md transition-shadow" onClick={() => nav('/cajero/pedidos-servidos')}>
              <div className="flex items-center justify-between">
                <div className="flex items-center gap-3">
                  <span className="font-mono font-bold">#{p.id}</span>
                  <span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa || '—'}</span>
                </div>
                <p className="text-lg font-bold text-gray-900 dark:text-white">{money(p.total)}</p>
              </div>
            </div>
          ))}
        </div>
      )}

      {/* Últimas cobradas */}
      {pagadas.length > 0 && (
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4">
            <CheckCircle className="w-5 h-5 text-emerald-500" />Últimas cobradas
          </h3>
          <div className="space-y-2">{pagadas.slice(0, 5).map((f: any) => (
            <div key={f.id} className="flex items-center justify-between py-2 border-b border-gray-50 dark:border-gray-800 last:border-0">
              <div>
                <span className="font-mono text-sm font-semibold text-gray-900 dark:text-white">{f.numeroFactura}</span>
                <span className="text-xs text-gray-400 ml-2">{f.cliente ? `${f.cliente.nombre}` : ''}</span>
              </div>
              <span className="font-bold text-emerald-600">{money(f.total)}</span>
            </div>
          ))}</div>
          <p className="text-xs text-gray-400 text-right mt-3">Total: {money(totalPendiente)} pendiente · {money(totalCobrado)} cobrado</p>
        </div>
      )}
    </div>
  );
}
