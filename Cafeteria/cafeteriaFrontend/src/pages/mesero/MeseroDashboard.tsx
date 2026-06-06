import React, { useState, useEffect } from 'react';
import { ClipboardList, Armchair, Clock, ChefHat, CheckCircle, PlusCircle, Eye } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { pedidoService } from '../../services/pedidoService';
import { mesaService } from '../../services/mesaService';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import Modal from '../../components/ui/Modal';
import { money, dateTime } from '../../utils/format';
import toast from 'react-hot-toast';

export default function MeseroDashboard() {
  const { user } = useAuth();
  const nav = useNavigate();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [mesas,setMesas]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [detail,setDetail]=useState<any>(null);

  const load=()=>Promise.all([user?pedidoService.porMesero(user.id):pedidoService.listar(),mesaService.listar()])
    .then(([p,m])=>{setPedidos(p.data.data||[]);setMesas(m.data.data||[]);}).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[user]);

  const cambiar=async(id:number,e:string)=>{try{await pedidoService.cambiarEstado(id,e);toast.success(`#${id} → ${e}`);load();}catch{toast.error('Error');}};

  if(loading) return <LoadingSpinner/>;
  const pendientes=pedidos.filter(p=>p.estado==='PENDIENTE');
  const enPrep=pedidos.filter(p=>p.estado==='EN_PREPARACION');
  const servidos=pedidos.filter(p=>p.estado==='SERVIDO');
  const mesasDisp=mesas.filter((m:any)=>m.estado==='DISPONIBLE'||!m.estado).length;

  return(
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard Mesero</h1><p className="text-gray-500 text-sm mt-1">Resumen de tu turno</p></div>
        <Link to="/mesero/nuevo-pedido" className="btn-gold"><PlusCircle className="w-4 h-4"/>Nuevo Pedido</Link>
      </div>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Pendientes" value={pendientes.length} icon={Clock} color="text-amber-600" bgColor="bg-amber-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="En Preparación" value={enPrep.length} icon={ChefHat} color="text-blue-600" bgColor="bg-blue-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="Servidos" value={servidos.length} icon={CheckCircle} color="text-green-600" bgColor="bg-green-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="Mesas Libres" value={mesasDisp} icon={Armchair} color="text-indigo-600" bgColor="bg-indigo-50" onClick={()=>nav('/mesero/mesas')}/>
      </div>

      {/* Pedidos activos con detalle de platos */}
      {[...pendientes,...enPrep].length>0&&(
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white">Pedidos activos</h3>
          {[...pendientes,...enPrep].map(p=>(
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
              {p.notas&&<p className="text-xs text-gray-400 italic mt-2">Nota: {p.notas}</p>}
              <p className="text-right font-bold text-brand-500 mt-2">{money(p.total)}</p>
            </div>
          ))}
        </div>
      )}

      {/* Últimos servidos */}
      {servidos.length>0&&(
        <div className="card p-5">
          <h3 className="font-semibold text-gray-900 dark:text-white mb-3">Últimos servidos</h3>
          <div className="space-y-2">{servidos.slice(0,5).map(p=>(
            <div key={p.id} className="flex items-center justify-between py-2 border-b border-gray-50 dark:border-gray-800 last:border-0">
              <div className="flex items-center gap-3"><span className="font-mono font-bold text-sm">#{p.id}</span><span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa||'—'}</span></div>
              <div className="flex items-center gap-3"><span className="font-semibold text-sm">{money(p.total)}</span><StatusBadge status={p.estado}/></div>
            </div>
          ))}</div>
        </div>
      )}

      <Modal open={!!detail} onClose={()=>setDetail(null)} title={`Pedido #${detail?.id}`}>
        {detail&&<div className="space-y-3">
          <div className="text-sm"><span className="text-gray-500">Cliente:</span> <span className="font-medium">{detail.cliente?`${detail.cliente.nombre} ${detail.cliente.apellido}`:'—'}</span></div>
          <div className="text-sm"><span className="text-gray-500">Mesa:</span> <span className="font-medium">{detail.mesa?`Mesa ${detail.mesa.numeroMesa} (${detail.mesa.ubicacion||''})`:'—'}</span></div>
          {detail.detalles?.map((d:any,i:number)=><div key={i} className="flex justify-between text-sm py-1.5 border-b border-gray-50 dark:border-gray-800"><span>{d.cantidad}× {d.producto?.nombre}</span><span className="font-medium">{money(d.subtotal||(d.producto?.precio*d.cantidad))}</span></div>)}
          <div className="flex justify-between font-bold text-lg pt-2"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
        </div>}
      </Modal>
    </div>
  );
}
