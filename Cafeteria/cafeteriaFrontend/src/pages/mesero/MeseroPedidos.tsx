import React, { useState, useEffect } from 'react';
import { ClipboardList, Eye } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { useAuth } from '../../context/AuthContext';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function MeseroPedidos() {
  const { user } = useAuth();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [detail,setDetail]=useState<any>(null);
  const [filtro,setFiltro]=useState('');

  const load=()=>(user?pedidoService.porMesero(user.id):pedidoService.listar()).then(r=>setPedidos((r.data.data||[]).reverse())).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[user]);

  const cambiar=async(id:number,e:string)=>{try{await pedidoService.cambiarEstado(id,e);toast.success(`#${id} → ${e.replace('_',' ')}`);load();}catch{toast.error('Error');}};
  const filtered=filtro?pedidos.filter((p:any)=>p.estado===filtro):pedidos;

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mis Pedidos</h1></div>
      <div className="flex gap-2 overflow-x-auto scrollbar-hide">
        {['','PENDIENTE','EN_PREPARACION','SERVIDO'].map(e=>(
          <button key={e} onClick={()=>setFiltro(e)} className={`btn-sm whitespace-nowrap ${filtro===e?'btn-gold':'btn-outline'}`}>{e?e.replace('_',' '):'Todos'}</button>
        ))}
      </div>
      {filtered.length===0?<EmptyState title="Sin pedidos" icon={ClipboardList}/>:(
        <div className="space-y-3">{filtered.map((p:any)=>(
          <div key={p.id} className="card p-4">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-3">
                <span className="font-mono font-bold">#{p.id}</span>
                <span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa||'—'}</span>
                <StatusBadge status={p.estado}/>
              </div>
              <div className="flex gap-2">
                {p.estado==='PENDIENTE'&&<button onClick={()=>cambiar(p.id,'EN_PREPARACION')} className="btn-sm btn-outline text-blue-600">Preparar</button>}
                {p.estado==='EN_PREPARACION'&&<button onClick={()=>cambiar(p.id,'SERVIDO')} className="btn-sm btn-outline text-green-600">Servir</button>}
                <button onClick={()=>setDetail(p)} className="btn-ghost btn-xs"><Eye className="w-4 h-4"/></button>
              </div>
            </div>
            <div className="space-y-1 ml-1">
              {p.detalles?.map((d:any,i:number)=>(
                <p key={i} className="text-sm text-gray-600 dark:text-gray-400">• {d.cantidad}× <span className="font-medium text-gray-800 dark:text-gray-200">{d.producto?.nombre}</span></p>
              ))}
            </div>
            <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-100 dark:border-gray-800">
              <span className="text-xs text-gray-400">{dateTime(p.fechaCreacion)}</span>
              <span className="font-bold text-brand-500">{money(p.total)}</span>
            </div>
          </div>
        ))}</div>
      )}
      <Modal open={!!detail} onClose={()=>setDetail(null)} title={`Pedido #${detail?.id}`}>
        {detail&&<div className="space-y-3">
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div><span className="text-gray-500">Cliente:</span><p className="font-medium">{detail.cliente?`${detail.cliente.nombre} ${detail.cliente.apellido}`:'—'}</p></div>
            <div><span className="text-gray-500">Mesa:</span><p className="font-medium">{detail.mesa?`Mesa ${detail.mesa.numeroMesa}`:'—'}</p></div>
          </div>
          {detail.detalles?.map((d:any,i:number)=><div key={i} className="flex justify-between text-sm py-1.5 border-b border-gray-50 dark:border-gray-800"><span>{d.cantidad}× {d.producto?.nombre}</span><span className="font-medium">{money(d.subtotal||(d.producto?.precio*d.cantidad))}</span></div>)}
          {detail.notas&&<p className="text-sm text-gray-500 italic">Nota: {detail.notas}</p>}
          <div className="flex justify-between font-bold text-lg pt-2"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
        </div>}
      </Modal>
    </div>
  );
}
