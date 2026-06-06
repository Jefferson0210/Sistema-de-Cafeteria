import React, { useState, useEffect } from 'react';
import { CalendarDays, Plus, X, Clock, Users } from 'lucide-react';
import { reservaService } from '../../services/reservaService';
import { mesaService } from '../../services/mesaService';
import { useAuth } from '../../context/AuthContext';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import toast from 'react-hot-toast';

export default function ClienteReservas() {
  const { user } = useAuth();
  const [reservas,setReservas]=useState<any[]>([]);
  const [mesas,setMesas]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [showForm,setShowForm]=useState(false);
  const [f,setF]=useState({fecha:'',hora:'',personas:2,notas:''});

  const load=()=>{if(user)Promise.all([reservaService.porUsuario(user.id),mesaService.disponibles()]).then(([r,m])=>{setReservas(r.data.data||[]);setMesas(m.data.data||[]);}).catch(()=>{}).finally(()=>setLoading(false));};
  useEffect(()=>{load();},[user]);

  const crear=async(e:React.FormEvent)=>{
    e.preventDefault();if(!user)return;
    // Find a mesa that fits the party size, or use first available
    const mesaFit = mesas.find((m:any)=>m.capacidad>=f.personas) || mesas[0];
    if(!mesaFit){toast.error('No hay mesas disponibles');return;}
    try{
      await reservaService.crear({mesaId:mesaFit.id,fecha:f.fecha,hora:f.hora,personas:f.personas,notas:f.notas},user.id);
      toast.success('¡Reserva creada!');
      setShowForm(false);setF({fecha:'',hora:'',personas:2,notas:''});load();
    }catch(err:any){toast.error(err.response?.data?.message||'Error al crear reserva');}
  };
  const cancelar=async(id:number)=>{try{await reservaService.cancelar(id);toast.success('Cancelada');load();}catch{toast.error('Error');}};

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mis Reservas</h1></div>
        <button onClick={()=>setShowForm(!showForm)} className="btn-gold">{showForm?<><X className="w-4 h-4"/>Cancelar</>:<><Plus className="w-4 h-4"/>Nueva Reserva</>}</button>
      </div>
      {showForm&&(<form onSubmit={crear} className="card p-6 space-y-4">
        <div className="grid grid-cols-2 gap-4">
          <div><label className="label">Fecha</label><input className="input" type="date" value={f.fecha} onChange={e=>setF(p=>({...p,fecha:e.target.value}))} min={new Date().toISOString().split('T')[0]} required/></div>
          <div><label className="label">Hora</label><input className="input" type="time" value={f.hora} onChange={e=>setF(p=>({...p,hora:e.target.value}))} required/></div>
        </div>
        <div className="grid grid-cols-2 gap-4">
          <div><label className="label">Personas</label><input className="input" type="number" min={1} max={20} value={f.personas} onChange={e=>setF(p=>({...p,personas:+e.target.value}))}/></div>
          <div><label className="label">Notas</label><input className="input" placeholder="Opcional..." value={f.notas} onChange={e=>setF(p=>({...p,notas:e.target.value}))}/></div>
        </div>
        <button type="submit" className="btn-gold w-full">Confirmar Reserva</button>
        <p className="text-xs text-gray-400 text-center">Se asignará la mejor mesa disponible automáticamente</p>
      </form>)}
      {reservas.length===0&&!showForm?<EmptyState title="Sin reservas" description="Haz tu primera reserva" icon={CalendarDays}/>:(
        <div className="space-y-3">{reservas.map((r:any)=>(
          <div key={r.id} className="card p-5">
            <div className="flex items-start justify-between">
              <div className="space-y-1.5">
                <div className="flex flex-wrap gap-3 text-sm">
                  <span className="flex items-center gap-1"><CalendarDays className="w-4 h-4 text-brand-500"/>{r.fecha||r.fechaReserva}</span>
                  <span className="flex items-center gap-1"><Clock className="w-4 h-4 text-brand-500"/>{r.hora}</span>
                  <span className="flex items-center gap-1"><Users className="w-4 h-4 text-brand-500"/>{r.personas}p</span>
                </div>
                {r.mesa&&<p className="text-xs text-gray-400">Mesa {r.mesa.numeroMesa} · {r.mesa.ubicacion}</p>}
              </div>
              <div className="flex items-center gap-2">
                <StatusBadge status={r.estado}/>
                {r.estado!=='CANCELADA'&&r.estado!=='COMPLETADA'&&<button onClick={()=>cancelar(r.id)} className="text-xs text-red-500 font-medium">Cancelar</button>}
              </div>
            </div>
          </div>
        ))}</div>
      )}
    </div>
  );
}
