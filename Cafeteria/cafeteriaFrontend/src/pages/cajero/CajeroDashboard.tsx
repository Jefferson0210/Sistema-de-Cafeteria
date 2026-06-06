import React, { useState, useEffect } from 'react';
import { FileText, DollarSign, Clock, CheckCircle } from 'lucide-react';
import { dashboardService } from '../../services/dashboardService';
import { pedidoService } from '../../services/pedidoService';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { money } from '../../utils/format';

export default function CajeroDashboard() {
  const [v,setV]=useState<any>(null);
  const [servidos,setServidos]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  useEffect(()=>{
    Promise.all([dashboardService.ventas(),pedidoService.porEstado('SERVIDO')])
    .then(([a,b])=>{setV(a.data.data);setServidos(b.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard Cajero</h1><p className="text-gray-500 text-sm mt-1">Resumen de caja</p></div>
      <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
        <StatCard label="Ingreso Total" value={money(v?.ingresoTotal||0)} icon={DollarSign} color="text-green-600" bgColor="bg-green-50"/>
        <StatCard label="Facturas Pendientes" value={v?.facturasPendientes||0} icon={Clock} color="text-amber-600" bgColor="bg-amber-50"/>
        <StatCard label="Pedidos Servidos" value={servidos.length} icon={CheckCircle} color="text-blue-600" bgColor="bg-blue-50"/>
        <StatCard label="Ticket Promedio" value={money(v?.ticketPromedio||0)} icon={FileText} color="text-purple-600" bgColor="bg-purple-50"/>
      </div>
      <div className="card p-5">
        <h3 className="font-semibold text-gray-900 mb-4">Pedidos pendientes de cobro</h3>
        <div className="space-y-2">{servidos.length===0?<p className="text-gray-400 text-sm py-4 text-center">No hay pedidos servidos</p>:servidos.slice(0,6).map((p:any)=>(
          <div key={p.id} className="flex items-center justify-between py-2 border-b border-gray-50 dark:border-gray-800">
            <div className="flex items-center gap-3"><span className="font-mono font-bold">#{p.id}</span><span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa||'—'}</span></div>
            <div className="flex items-center gap-3"><span className="font-semibold">{money(p.total)}</span><StatusBadge status="SERVIDO"/></div>
          </div>
        ))}</div>
      </div>
    </div>
  );
}
