import React, { useState, useEffect } from 'react';
import { Armchair, Users } from 'lucide-react';
import { mesaService } from '../../services/mesaService';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';

export default function MeseroMesas() {
  const [mesas,setMesas]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  useEffect(()=>{mesaService.listar().then(r=>setMesas(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));},[]);
  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mesas</h1><p className="text-gray-500 text-sm mt-1">Estado de las mesas</p></div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">{mesas.map((m:any)=>(
        <div key={m.id} className={`card p-5 transition-shadow hover:shadow-md ${m.estado==='OCUPADA'?'border-red-200 bg-red-50/30':''}`}>
          <div className="flex items-center gap-3 mb-3">
            <div className={`w-12 h-12 rounded-xl flex items-center justify-center ${m.estado==='OCUPADA'?'bg-red-100':'bg-green-100'}`}><Armchair className={`w-6 h-6 ${m.estado==='OCUPADA'?'text-red-600':'text-green-600'}`}/></div>
            <div><p className="font-bold text-gray-900 text-lg">Mesa {m.numeroMesa}</p><p className="text-xs text-gray-500">{m.ubicacion}</p></div>
          </div>
          <div className="flex items-center justify-between">
            <span className="text-sm text-gray-500 flex items-center gap-1"><Users className="w-3.5 h-3.5"/>{m.capacidad}p</span>
            <StatusBadge status={m.estado||'DISPONIBLE'}/>
          </div>
        </div>
      ))}</div>
    </div>
  );
}
