import React, { useState, useEffect } from 'react';
import { Armchair, Users, ToggleLeft, ToggleRight, CalendarDays } from 'lucide-react';
import { mesaService } from '../../services/mesaService';
import { reservaService } from '../../services/reservaService';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import toast from 'react-hot-toast';

export default function MeseroMesas() {
  const [mesas,setMesas]=useState<any[]>([]);
  const [reservas,setReservas]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  const load=()=>Promise.all([mesaService.listar(),reservaService.listar()])
    .then(([m,r])=>{setMesas(m.data.data||[]);setReservas(r.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));

  useEffect(()=>{load();},[]);

  const toggle=async(m:any)=>{
    const nuevoEstado=m.estado==='OCUPADA'?'LIBRE':'OCUPADA';
    try{
      await mesaService.cambiarEstado(m.id,nuevoEstado);
      toast.success(`Mesa ${m.numeroMesa} → ${nuevoEstado}`);
      load();
    }catch{toast.error('Error al cambiar estado');}
  };

  const reservasHoy=reservas.filter((r:any)=>{
    const f=new Date(r.fecha||r.fechaReserva);
    return f.toDateString()===new Date().toDateString();
  });

  const getReservasMesa=(mesaId:number)=>reservasHoy.filter((r:any)=>r.mesa?.id===mesaId);

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mesas</h1><p className="text-gray-500 text-sm mt-1">Toca el toggle para cambiar estado</p></div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">{mesas.map((m:any)=>{
        const resv=getReservasMesa(m.id);
        return(
          <div key={m.id} className={`card p-5 transition-shadow hover:shadow-md ${m.estado==='OCUPADA'?'border-red-200 dark:border-red-900/50 bg-red-50/30 dark:bg-red-900/10':''}`}>
            <div className="flex items-center gap-3 mb-3">
              <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${m.estado==='OCUPADA'?'bg-red-100 dark:bg-red-900/30':'bg-green-100 dark:bg-green-900/30'}`}>
                <Armchair className={`w-6 h-6 ${m.estado==='OCUPADA'?'text-red-600':'text-green-600'}`}/>
              </div>
              <div className="flex-1">
                <p className="font-bold text-gray-900 dark:text-white text-lg">Mesa {m.numeroMesa}</p>
                <p className="text-xs text-gray-500">{m.ubicacion}</p>
              </div>
            </div>

            <div className="flex items-center justify-between mb-3">
              <span className="text-sm text-gray-500 flex items-center gap-1"><Users className="w-3.5 h-3.5"/>{m.capacidad}p</span>
              <StatusBadge status={m.estado||'LIBRE'}/>
            </div>

            {/* Toggle */}
            <button onClick={()=>toggle(m)} className={`w-full flex items-center justify-center gap-2 py-2 rounded-lg text-sm font-medium transition-all ${m.estado==='OCUPADA'?'bg-green-50 dark:bg-green-900/20 text-green-700 dark:text-green-400 hover:bg-green-100':'bg-red-50 dark:bg-red-900/20 text-red-700 dark:text-red-400 hover:bg-red-100'}`}>
              {m.estado==='OCUPADA'?<><ToggleRight className="w-5 h-5"/>Marcar Libre</>:<><ToggleLeft className="w-5 h-5"/>Marcar Ocupada</>}
            </button>

            {/* Reservas de hoy */}
            {resv.length>0&&(
              <div className="mt-3 pt-3 border-t border-gray-100 dark:border-gray-800">
                <p className="text-xs font-semibold text-amber-600 dark:text-amber-400 flex items-center gap-1 mb-1"><CalendarDays className="w-3.5 h-3.5"/>Reservas hoy:</p>
                {resv.map((r:any,i:number)=>(
                  <p key={i} className="text-xs text-gray-500">🕐 {r.hora} — {r.personas}p {r.cliente?`(${r.cliente.nombre})`:''}</p>
                ))}
              </div>
            )}
          </div>
        );
      })}</div>
    </div>
  );
}