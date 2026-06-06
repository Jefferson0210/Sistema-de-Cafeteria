import React, { useState, useEffect } from 'react';
import { History, Eye } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { useAuth } from '../../context/AuthContext';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';

export default function ClientePedidos() {
  const { user } = useAuth();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [detail,setDetail]=useState<any>(null);

  useEffect(()=>{if(user)pedidoService.porCliente(user.id).then(r=>setPedidos((r.data.data||[]).reverse())).catch(()=>{}).finally(()=>setLoading(false));},[user]);

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mis Pedidos</h1><p className="text-gray-500 text-sm mt-1">Historial de pedidos realizados</p></div>
      {pedidos.length===0?<EmptyState title="No tienes pedidos aún" description="Realiza tu primer pedido desde el menú" icon={History}/>:(
        <div className="space-y-3">{pedidos.map(p=>(
          <div key={p.id} className="card p-5 hover:shadow-sm transition-shadow">
            <div className="flex items-center justify-between mb-2">
              <div className="flex items-center gap-3"><span className="font-mono font-bold text-lg">#{p.id}</span><span className="text-gray-400 text-sm">Mesa {p.mesa?.numeroMesa||'—'}</span></div>
              <StatusBadge status={p.estado}/>
            </div>
            {p.detalles?.slice(0,3).map((d:any,i:number)=><p key={i} className="text-sm text-gray-500">{d.cantidad}× {d.producto?.nombre}</p>)}
            {(p.detalles?.length||0)>3&&<p className="text-xs text-gray-400">+{p.detalles.length-3} más</p>}
            <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100 dark:border-gray-800">
              <span className="text-xs text-gray-400">{dateTime(p.fechaCreacion)}</span>
              <div className="flex items-center gap-3"><span className="font-bold text-lg text-brand-500">{money(p.total)}</span><button onClick={()=>setDetail(p)} className="btn-ghost btn-xs"><Eye className="w-4 h-4"/></button></div>
            </div>
          </div>
        ))}</div>
      )}
      <Modal open={!!detail} onClose={()=>setDetail(null)} title={`Pedido #${detail?.id}`}>
        {detail&&<div className="space-y-3">
          <div className="grid grid-cols-2 gap-3 text-sm"><div><span className="text-gray-500">Estado:</span><div className="mt-0.5"><StatusBadge status={detail.estado}/></div></div><div><span className="text-gray-500">Mesa:</span><p className="font-medium">{detail.mesa?`Mesa ${detail.mesa.numeroMesa}`:'—'}</p></div></div>
          {detail.detalles?.map((d:any,i:number)=><div key={i} className="flex justify-between text-sm py-1.5 border-b border-gray-50 dark:border-gray-800"><span>{d.cantidad}× {d.producto?.nombre}</span><span className="font-medium">{money(d.subtotal)}</span></div>)}
          <div className="flex justify-between font-bold text-lg pt-2"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
        </div>}
      </Modal>
    </div>
  );
}
