import React, { useState, useEffect } from 'react';
import { Armchair, Clock, ChefHat, CheckCircle, PlusCircle } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { pedidoService } from '../../services/pedidoService';
import { mesaService } from '../../services/mesaService';
import { useAuth } from '../../context/AuthContext';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { money, dateTime } from '../../utils/format';
import toast from 'react-hot-toast';

const hoy=(d:string)=>{if(!d)return false;const f=new Date(d);const h=new Date();return f.toDateString()===h.toDateString();};
const ayer=(d:string)=>{if(!d)return false;const f=new Date(d);const h=new Date();h.setDate(h.getDate()-1);return f.toDateString()===h.toDateString();};
const formatFecha=(d:string)=>new Date(d).toLocaleDateString('es-EC',{weekday:'short',day:'numeric',month:'short'});

export default function MeseroDashboard() {
  const { user } = useAuth();
  const nav = useNavigate();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [mesas,setMesas]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  const load=async()=>{
    try{
      const [p,m]=await Promise.all([user?pedidoService.porMesero(user.id):pedidoService.listar(),mesaService.listar()]);
      const lista=(p.data.data||[]).reverse();
      const conDetalles=await Promise.all(lista.map(async(pe:any)=>{
        if(pe.detalles&&pe.detalles.length>0&&pe.detalles[0].producto) return pe;
try{const r=await pedidoService.obtener(pe.id);return r.data.data||r.data||pe;}catch{return pe;}      }));
      setPedidos(conDetalles);setMesas(m.data.data||[]);
    }catch{}finally{setLoading(false);}
  };
  useEffect(()=>{load();},[user]);

  const cambiar=async(id:number,e:string)=>{try{await pedidoService.cambiarEstado(id,e);toast.success(`#${id} → ${e.replace('_',' ')}`);load();}catch{toast.error('Error');}};

  if(loading) return <LoadingSpinner/>;

  const pedidosHoy=pedidos.filter(p=>hoy(p.fechaCreacion));
  const pedidosAyer=pedidos.filter(p=>ayer(p.fechaCreacion));
  const pedidosAnteriores=pedidos.filter(p=>!hoy(p.fechaCreacion)&&!ayer(p.fechaCreacion));

  const pendientes=pedidosHoy.filter(p=>p.estado==='PENDIENTE');
  const enPrep=pedidosHoy.filter(p=>p.estado==='EN_PREPARACION');
  const servidos=pedidosHoy.filter(p=>p.estado==='SERVIDO');
  const mesasDisp=mesas.filter((m:any)=>m.estado==='LIBRE'||!m.estado).length;
  const activos=[...pendientes,...enPrep];

  const PedidoCard=({p}:{p:any})=>(
    <div className="card p-4">
      <div className="flex items-center justify-between mb-2 flex-wrap gap-2">
        <div className="flex items-center gap-3">
          <span className="font-mono font-bold">#{p.id}</span>
          <span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa||'—'}</span>
          <StatusBadge status={p.estado}/>
        </div>
        <div className="flex gap-2">
          {p.estado==='PENDIENTE'&&<button onClick={()=>cambiar(p.id,'EN_PREPARACION')} className="btn-sm btn-outline text-blue-600">Preparar</button>}
          {p.estado==='EN_PREPARACION'&&<button onClick={()=>cambiar(p.id,'SERVIDO')} className="btn-sm btn-outline text-green-600">Servir</button>}
        </div>
      </div>
      <div className="bg-gray-50 dark:bg-gray-800/50 rounded-lg p-3 space-y-1.5">
        {p.detalles&&p.detalles.length>0?p.detalles.map((d:any,i:number)=>(
          <div key={i} className="flex items-center justify-between text-sm">
            <span className="text-gray-700 dark:text-gray-300"><span className="font-semibold text-brand-500">{d.cantidad}×</span> {d.producto?.nombre||'Producto'}</span>
            <span className="text-gray-500">{money(d.subtotal||(d.producto?.precio||0)*d.cantidad)}</span>
          </div>
        )):<p className="text-xs text-gray-400">Sin detalles</p>}
      </div>
      {p.notas&&<p className="text-xs text-amber-600 dark:text-amber-400 italic mt-2">📝 {p.notas}</p>}
      <div className="flex items-center justify-between mt-2 pt-2 border-t border-gray-100 dark:border-gray-800">
        <span className="text-xs text-gray-400">{p.cliente?`${p.cliente.nombre} ${p.cliente.apellido}`:''} · {dateTime(p.fechaCreacion)}</span>
        <span className="font-bold text-brand-500">{money(p.total)}</span>
      </div>
    </div>
  );

  return(
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard Mesero</h1><p className="text-gray-500 text-sm mt-1">Resumen de tu turno</p></div>
        <Link to="/mesero/nuevo-pedido" className="btn-gold shrink-0"><PlusCircle className="w-4 h-4"/>Nuevo Pedido</Link>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Pendientes" value={pendientes.length} icon={Clock} color="text-amber-600" bgColor="bg-amber-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="En Preparación" value={enPrep.length} icon={ChefHat} color="text-blue-600" bgColor="bg-blue-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="Servidos hoy" value={servidos.length} icon={CheckCircle} color="text-green-600" bgColor="bg-green-50" onClick={()=>nav('/mesero/pedidos')}/>
        <StatCard label="Mesas Libres" value={mesasDisp} icon={Armchair} color="text-indigo-600" bgColor="bg-indigo-50" onClick={()=>nav('/mesero/mesas')}/>
      </div>

      {/* Pedidos activos */}
      {activos.length>0&&(
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white">🔥 Pedidos activos</h3>
          {activos.map(p=><PedidoCard key={p.id} p={p}/>)}
        </div>
      )}

      {/* Hoy - servidos */}
      {servidos.length>0&&(
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white">📅 Hoy — Servidos</h3>
          {servidos.map(p=><PedidoCard key={p.id} p={p}/>)}
        </div>
      )}

      {/* Ayer */}
      {pedidosAyer.length>0&&(
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white">📅 Ayer</h3>
          {pedidosAyer.slice(0,5).map(p=><PedidoCard key={p.id} p={p}/>)}
          {pedidosAyer.length>5&&<p className="text-xs text-gray-400 text-center">+{pedidosAyer.length-5} más</p>}
        </div>
      )}

      {/* Anteriores */}
      {pedidosAnteriores.length>0&&(
        <div className="space-y-3">
          <h3 className="font-semibold text-gray-900 dark:text-white">📅 Anteriores</h3>
          {pedidosAnteriores.slice(0,5).map(p=><PedidoCard key={p.id} p={p}/>)}
          {pedidosAnteriores.length>5&&<p className="text-xs text-gray-400 text-center">+{pedidosAnteriores.length-5} más</p>}
        </div>
      )}

      {pedidos.length===0&&<div className="card p-8 text-center text-gray-400">No hay pedidos</div>}
    </div>
  );
}