import React, { useState, useEffect } from 'react';
import { ClipboardList, Search, Eye } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { Pedido } from '../../types';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function AdminPedidos() {
  const [pedidos,setPedidos]=useState<Pedido[]>([]);
  const [loading,setLoading]=useState(true);
  const [filtro,setFiltro]=useState('');
  const [detail,setDetail]=useState<Pedido|null>(null);

  const load=()=>pedidoService.listar().then(r=>setPedidos((r.data.data||[]).reverse())).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[]);

  const cambiarEstado=async(id:number,estado:string)=>{
    try{await pedidoService.cambiarEstado(id,estado);toast.success(`Pedido #${id} → ${estado}`);load();}catch{toast.error('Error');}
  };

  const filtered=filtro?pedidos.filter(p=>p.estado===filtro):pedidos;

  if(loading)return<LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Pedidos</h1><p className="text-gray-500 text-sm mt-1">{pedidos.length} pedidos</p></div>
      <div className="flex gap-2 flex-wrap">
        {['','PENDIENTE','EN_PREPARACION','SERVIDO','PAGADO','CANCELADO'].map(e=>(
          <button key={e} onClick={()=>setFiltro(e)} className={`btn-sm ${filtro===e?'btn-gold':'btn-outline'}`}>{e||'Todos'}</button>
        ))}
      </div>
      {filtered.length===0?<EmptyState title="Sin pedidos" icon={ClipboardList}/>:(
        <div className="table-container"><table className="w-full">
          <thead><tr><th className="th">#</th><th className="th">Cliente</th><th className="th">Mesa</th><th className="th">Mesero</th><th className="th">Total</th><th className="th">Estado</th><th className="th">Fecha</th><th className="th">Acciones</th></tr></thead>
          <tbody>{filtered.map(p=>(
            <tr key={p.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
              <td className="td font-mono font-bold text-gray-900 dark:text-white">#{p.id}</td>
              <td className="td text-sm">{p.cliente?`${p.cliente.nombre} ${p.cliente.apellido}`:'—'}</td>
              <td className="td text-sm">{p.mesa?`Mesa ${p.mesa.numeroMesa}`:'—'}</td>
              <td className="td text-sm">{p.mesero?`${p.mesero.nombre}`:'—'}</td>
              <td className="td font-semibold">{money(p.total)}</td>
              <td className="td"><StatusBadge status={p.estado}/></td>
              <td className="td text-xs text-gray-400">{dateTime(p.fechaCreacion)}</td>
              <td className="td"><div className="flex gap-1">
                <button onClick={()=>setDetail(p)} className="btn-ghost btn-xs"><Eye className="w-3.5 h-3.5"/></button>
                {p.estado==='PENDIENTE'&&<button onClick={()=>cambiarEstado(p.id,'EN_PREPARACION')} className="btn-xs btn-outline text-blue-600">Preparar</button>}
                {p.estado==='EN_PREPARACION'&&<button onClick={()=>cambiarEstado(p.id,'SERVIDO')} className="btn-xs btn-outline text-green-600">Servir</button>}
              </div></td>
            </tr>
          ))}</tbody>
        </table></div>
      )}
      <Modal open={!!detail} onClose={()=>setDetail(null)} title={`Pedido #${detail?.id}`} size="max-w-lg">
        {detail&&(<div className="space-y-4">
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div><span className="text-gray-500">Cliente:</span><p className="font-medium">{detail.cliente?`${detail.cliente.nombre} ${detail.cliente.apellido}`:'—'}</p></div>
            <div><span className="text-gray-500">Mesero:</span><p className="font-medium">{detail.mesero?detail.mesero.nombre:'—'}</p></div>
            <div><span className="text-gray-500">Mesa:</span><p className="font-medium">{detail.mesa?`Mesa ${detail.mesa.numeroMesa}`:'—'}</p></div>
            <div><span className="text-gray-500">Estado:</span><div className="mt-0.5"><StatusBadge status={detail.estado}/></div></div>
          </div>
          {detail.notas&&<div className="bg-amber-50 rounded-lg p-3 text-sm text-amber-700">{detail.notas}</div>}
          {detail.detalles&&detail.detalles.length>0&&(<div>
            <p className="text-sm font-medium text-gray-700 mb-2">Items:</p>
            {detail.detalles.map((d,i)=>(<div key={i} className="flex justify-between text-sm py-1.5 border-b border-gray-50 dark:border-gray-800">
              <span className="text-gray-700">{d.cantidad}× {d.producto?.nombre||'Producto'}</span><span className="font-medium">{money(d.subtotal)}</span>
            </div>))}
          </div>)}
          <div className="flex justify-between text-lg font-bold pt-2"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
        </div>)}
      </Modal>
    </div>
  );
}
