import React, { useState, useEffect } from 'react';
import { FileText, Eye, Download, Mail, CreditCard, Banknote, Search } from 'lucide-react';
import { facturaService } from '../../services/facturaService';
import { pagoService } from '../../services/pagoService';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function CajeroFacturas() {
  const [facturas, setFacturas] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [detail, setDetail] = useState<any>(null);
  const [search, setSearch] = useState('');
  const [filtro, setFiltro] = useState('');

  // Cobro
  const [cobroModal, setCobroModal] = useState<any>(null);
  const [metodo, setMetodo] = useState<'EFECTIVO' | 'TARJETA' | 'TRANSFERENCIA'>('EFECTIVO');
  const [referencia, setReferencia] = useState('');
  const [cobrando, setCobrando] = useState(false);

  // Datos cliente para factura
  const [clienteData, setClienteData] = useState({ nombre: '', cedula: '', direccion: '', email: '', telefono: '' });

  const load = () => {
    facturaService.listar()
      .then(r => setFacturas((r.data.data || []).reverse()))
      .catch(() => {})
      .finally(() => setLoading(false));
  };
  useEffect(() => { load(); }, []);

  const filtered = facturas.filter(f => {
    if (filtro && f.estado !== filtro) return false;
    if (search) {
      const q = search.toLowerCase();
      return f.numeroFactura?.toLowerCase().includes(q) ||
        (f.cliente && `${f.cliente.nombre} ${f.cliente.apellido}`.toLowerCase().includes(q));
    }
    return true;
  });

  const abrirCobro = (f: any) => {
    setCobroModal(f);
    setMetodo('EFECTIVO');
    setReferencia('');
    setClienteData({
      nombre: f.cliente ? `${f.cliente.nombre} ${f.cliente.apellido}` : '',
      cedula: f.cliente?.cedula || '',
      direccion: f.cliente?.direccion || '',
      email: f.cliente?.email || '',
      telefono: f.cliente?.telefono || ''
    });
  };

  const cobrar = async () => {
    if (!cobroModal) return;
    setCobrando(true);
    try {
      if (metodo === 'EFECTIVO') {
        await pagoService.efectivo(cobroModal.id, cobroModal.total);
      } else if (metodo === 'TARJETA') {
        await pagoService.tarjeta(cobroModal.id, cobroModal.total, referencia || undefined);
      } else {
        await pagoService.registrar({ facturaId: cobroModal.id, monto: cobroModal.total, metodoPago: metodo, referencia: referencia || undefined });
      }
      toast.success('¡Pago registrado! Factura PAGADA');
      setCobroModal(null);
      load();
    } catch (err: any) {
      toast.error(err.response?.data?.message || 'Error al registrar pago');
    }
    setCobrando(false);
  };

  const descargarPdf = async (id: number, num: string) => {
    try {
      const res = await facturaService.descargarPdf(id);
      const url = window.URL.createObjectURL(new Blob([res.data]));
      const a = document.createElement('a'); a.href = url; a.download = `factura-${num}.pdf`; a.click();
      window.URL.revokeObjectURL(url);
      toast.success('PDF descargado');
    } catch { toast.error('Error al descargar PDF'); }
  };

  const enviarEmail = async (id: number, email?: string) => {
    const dest = email || prompt('Email del cliente:');
    if (!dest) return;
    try { await facturaService.enviarEmail(id, dest); toast.success('Factura enviada'); } catch { toast.error('Error al enviar'); }
  };

  if (loading) return <LoadingSpinner />;
  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Facturas</h1>
        <p className="text-gray-500 text-sm mt-1">{facturas.length} facturas</p>
      </div>

      <div className="flex gap-3 flex-wrap">
        <div className="relative flex-1 max-w-md">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400" />
          <input className="input pl-9" placeholder="Buscar por número o cliente..." value={search} onChange={e => setSearch(e.target.value)} />
        </div>
        <div className="flex gap-2 overflow-x-auto scrollbar-hide">
          {['', 'PENDIENTE', 'PAGADA', 'ANULADA'].map(e => (
            <button key={e} onClick={() => setFiltro(e)} className={`btn-sm whitespace-nowrap ${filtro === e ? 'btn-gold' : 'btn-outline'}`}>{e || 'Todas'}</button>
          ))}
        </div>
      </div>

      {filtered.length === 0 ? <EmptyState title="Sin facturas" icon={FileText} /> : (
        <div className="space-y-3">{filtered.map(f => (
          <div key={f.id} className={`card p-4 ${f.estado === 'ANULADA' ? 'opacity-50' : ''}`}>
            <div className="flex items-center justify-between flex-wrap gap-2">
              <div className="flex-1 min-w-0">
                <div className="flex items-center gap-2 mb-1">
                  <span className="font-mono font-semibold text-sm text-gray-900 dark:text-white">{f.numeroFactura}</span>
                  <StatusBadge status={f.estado} />
                </div>
                <p className="text-sm text-gray-500">{f.cliente ? `${f.cliente.nombre} ${f.cliente.apellido}` : 'Sin cliente'}</p>
              </div>
              <p className="text-lg font-bold text-gray-900 dark:text-white">{money(f.total)}</p>
              <div className="flex gap-1">
                <button onClick={() => setDetail(f)} className="btn-ghost btn-xs" title="Ver"><Eye className="w-3.5 h-3.5" /></button>
                <button onClick={() => descargarPdf(f.id, f.numeroFactura)} className="btn-ghost btn-xs" title="PDF"><Download className="w-3.5 h-3.5" /></button>
                {f.estado === 'PENDIENTE' && (
                  <button onClick={() => abrirCobro(f)} className="btn-sm bg-emerald-600 hover:bg-emerald-700 text-white rounded-lg px-3 py-1 text-xs font-semibold flex items-center gap-1">
                    <CreditCard className="w-3.5 h-3.5" />Cobrar
                  </button>
                )}
              </div>
            </div>
          </div>
        ))}</div>
      )}

      {/* Modal detalle */}
      <Modal open={!!detail} onClose={() => setDetail(null)} title={`Factura ${detail?.numeroFactura || ''}`}>
        {detail && (
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3 text-sm">
              <div><span className="text-gray-500">Cliente:</span><p className="font-medium text-gray-900 dark:text-white">{detail.cliente ? `${detail.cliente.nombre} ${detail.cliente.apellido}` : '—'}</p></div>
              <div><span className="text-gray-500">Cajero:</span><p className="font-medium text-gray-900 dark:text-white">{detail.cajero ? `${detail.cajero.nombre} ${detail.cajero.apellido}` : '—'}</p></div>
              <div><span className="text-gray-500">Estado:</span><div className="mt-0.5"><StatusBadge status={detail.estado} /></div></div>
              <div><span className="text-gray-500">Fecha:</span><p className="font-medium text-gray-900 dark:text-white">{dateTime(detail.fechaEmision || detail.fechaCreacion)}</p></div>
            </div>
            {detail.detalles && detail.detalles.length > 0 && (
              <div>
                <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Productos</h4>
                <div className="space-y-2">{detail.detalles.map((d: any, i: number) => (
                  <div key={i} className="flex items-center justify-between py-1.5 border-b border-gray-50 dark:border-gray-800 last:border-0">
                    <div><p className="text-sm font-medium text-gray-900 dark:text-white">{d.producto?.nombre || 'Producto'}</p><p className="text-xs text-gray-400">{d.cantidad} x {money(d.precioUnitario || d.producto?.precio || 0)}</p></div>
                    <span className="text-sm font-semibold">{money((d.precioUnitario || d.producto?.precio || 0) * d.cantidad)}</span>
                  </div>
                ))}</div>
              </div>
            )}
            <div className="border-t border-gray-100 dark:border-gray-800 pt-3 space-y-1.5">
              <div className="flex justify-between text-sm"><span className="text-gray-500">Subtotal</span><span>{money(detail.subtotal)}</span></div>
              <div className="flex justify-between text-sm"><span className="text-gray-500">IVA 15%</span><span>{money(detail.iva)}</span></div>
              {detail.descuento > 0 && <div className="flex justify-between text-sm"><span className="text-gray-500">Descuento</span><span className="text-green-600">-{money(detail.descuento)}</span></div>}
              <div className="flex justify-between font-bold text-lg pt-1"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
            </div>
            <div className="flex gap-2">
              <button onClick={() => descargarPdf(detail.id, detail.numeroFactura)} className="btn-gold flex-1"><Download className="w-4 h-4" />PDF</button>
              <button onClick={() => enviarEmail(detail.id, detail.cliente?.email)} className="btn-outline flex-1"><Mail className="w-4 h-4" />Email</button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal cobrar */}
      <Modal open={!!cobroModal} onClose={() => setCobroModal(null)} title={`Cobrar ${cobroModal?.numeroFactura || ''}`}>
        {cobroModal && (
          <div className="space-y-4">
            {/* Datos del cliente */}
            <div>
              <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Datos del cliente</h4>
              <div className="grid grid-cols-2 gap-3">
                <div><label className="label">Nombre</label><input className="input" value={clienteData.nombre} onChange={e => setClienteData(p => ({ ...p, nombre: e.target.value }))} placeholder="Nombre completo" /></div>
                <div><label className="label">Cédula / RUC</label><input className="input" value={clienteData.cedula} onChange={e => setClienteData(p => ({ ...p, cedula: e.target.value }))} placeholder="0000000000" /></div>
                <div><label className="label">Dirección</label><input className="input" value={clienteData.direccion} onChange={e => setClienteData(p => ({ ...p, direccion: e.target.value }))} placeholder="Dirección" /></div>
                <div><label className="label">Email</label><input className="input" value={clienteData.email} onChange={e => setClienteData(p => ({ ...p, email: e.target.value }))} placeholder="email@ejemplo.com" /></div>
                <div><label className="label">Teléfono</label><input className="input" value={clienteData.telefono} onChange={e => setClienteData(p => ({ ...p, telefono: e.target.value }))} placeholder="0999999999" /></div>
              </div>
            </div>

            {/* Resumen */}
            <div className="bg-gray-50 dark:bg-gray-800/50 rounded-xl p-4">
              <div className="flex justify-between text-sm mb-1"><span className="text-gray-500">Subtotal</span><span>{money(cobroModal.subtotal)}</span></div>
              <div className="flex justify-between text-sm mb-1"><span className="text-gray-500">IVA 15%</span><span>{money(cobroModal.iva)}</span></div>
              <div className="flex justify-between font-bold text-xl pt-2 border-t border-gray-200 dark:border-gray-700"><span>Total a cobrar</span><span className="text-brand-500">{money(cobroModal.total)}</span></div>
            </div>

            {/* Método de pago */}
            <div>
              <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-3">Método de pago</h4>
              <div className="grid grid-cols-3 gap-2">
                {([['EFECTIVO', Banknote, 'Efectivo'], ['TARJETA', CreditCard, 'Tarjeta'], ['TRANSFERENCIA', CreditCard, 'Transfer.']] as const).map(([m, Icon, label]) => (
                  <button key={m} onClick={() => setMetodo(m)} className={`p-3 rounded-xl border text-center transition-all flex flex-col items-center gap-1.5 ${metodo === m ? 'border-brand-500 bg-brand-50 dark:bg-brand-900/20 ring-1 ring-brand-500' : 'border-gray-200 dark:border-gray-700 hover:border-gray-300'}`}>
                    <Icon className={`w-5 h-5 ${metodo === m ? 'text-brand-500' : 'text-gray-400'}`} />
                    <span className={`text-xs font-medium ${metodo === m ? 'text-brand-500' : 'text-gray-500'}`}>{label}</span>
                  </button>
                ))}
              </div>
            </div>

            {metodo === 'EFECTIVO' && (
  <div className="bg-emerald-50 dark:bg-emerald-900/10 rounded-xl p-4 space-y-3">
    <div><label className="label">Monto recibido</label><input className="input text-lg font-bold" type="number" step="0.01" min={cobroModal.total} placeholder="0.00" id="montoRecibido" onChange={e=>{const el=document.getElementById('vueltoDisplay');if(el)el.textContent=money(Math.max(0,+e.target.value-cobroModal.total));}}/></div>
    <div className="flex justify-between items-center pt-2 border-t border-emerald-200 dark:border-emerald-800">
      <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Vuelto:</span>
      <span id="vueltoDisplay" className="text-2xl font-bold text-emerald-600">$0.00</span>
    </div>
  </div>
)}

{(metodo === 'TARJETA' || metodo === 'TRANSFERENCIA') && (
  <div><label className="label">{metodo === 'TARJETA' ? 'Últimos 4 dígitos' : 'Nº referencia'}</label><input className="input" value={referencia} onChange={e => setReferencia(e.target.value)} placeholder={metodo === 'TARJETA' ? '1234' : 'REF-001'} /></div>
)}

            <button onClick={cobrar} disabled={cobrando} className="btn-gold w-full !py-3 text-base">
              {cobrando ? 'Procesando...' : `Confirmar Cobro — ${money(cobroModal.total)}`}
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
}
