import React, { useState, useEffect } from 'react';
import { ChefHat, Receipt } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { facturaService } from '../../services/facturaService';
import { useAuth } from '../../context/AuthContext';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import toast from 'react-hot-toast';

export default function CajeroPedidosServidos() {
  const { user } = useAuth();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  const load=()=>pedidoService.porEstado('SERVIDO').then(r=>setPedidos(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[]);

  const facturar=async(pedidoId:number)=>{
    try{await facturaService.desdePedido(pedidoId,user!.id);toast.success('Factura generada');load();}catch(e:any){toast.error(e.response?.data?.message||'Error');}
  };

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Pedidos Servidos</h1><p className="text-gray-500 text-sm mt-1">Pendientes de cobro</p></div>
      {pedidos.length===0?<EmptyState title="No hay pedidos servidos" icon={ChefHat}/>:(
        <div className="space-y-3">{pedidos.map((p:any)=>(
          <div key={p.id} className="card p-5 hover:shadow-md transition-shadow">
            <div className="flex items-center justify-between mb-3">
              <div><span className="font-mono font-bold text-lg">#{p.id}</span><span className="text-gray-500 text-sm ml-3">Mesa {p.mesa?.numeroMesa||'—'}</span></div>
              <StatusBadge status={p.estado}/>
            </div>
            <div className="text-sm text-gray-500 mb-3">Cliente: {p.cliente?`${p.cliente.nombre} ${p.cliente.apellido}`:'—'} · {dateTime(p.fechaCreacion)}</div>
            {p.detalles?.map((d:any,i:number)=><div key={i} className="flex justify-between text-sm py-1 border-b border-gray-50 dark:border-gray-800"><span>{d.cantidad}× {d.producto?.nombre}</span><span className="font-medium">{money(d.subtotal)}</span></div>)}
            <div className="flex items-center justify-between mt-4 pt-3 border-t border-gray-100 dark:border-gray-800">
              <span className="text-xl font-bold text-brand-500">{money(p.total)}</span>
              <button onClick={()=>facturar(p.id)} className="btn-gold"><Receipt className="w-4 h-4"/>Facturar</button>
            </div>
          </div>
        ))}</div>
      )}
    </div>
  );
}
