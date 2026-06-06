import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, Tags, Users, ClipboardList, FileText, Armchair, TrendingUp, AlertTriangle, DollarSign } from 'lucide-react';
import { dashboardService } from '../../services/dashboardService';
import StatCard from '../../components/ui/StatCard';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatusBadge from '../../components/ui/StatusBadge';
import { money } from '../../utils/format';

export default function AdminDashboard() {
  const nav = useNavigate();
  const [r,setR]=useState<any>(null);
  const [v,setV]=useState<any>(null);
  const [top,setTop]=useState<any[]>([]);
  const [stock,setStock]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  useEffect(()=>{
    Promise.all([dashboardService.resumen(),dashboardService.ventas(),dashboardService.topProductos(5),dashboardService.stockBajo(10)])
    .then(([a,b,c,d])=>{setR(a.data.data);setV(b.data.data);setTop(c.data.data||[]);setStock(d.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  if(loading) return <LoadingSpinner/>;
  return (
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard</h1><p className="text-gray-500 text-sm mt-1">Resumen general del sistema</p></div>
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard label="Productos" value={r?.totalProductos||0} icon={Package} color="text-blue-600" bgColor="bg-blue-50" onClick={()=>nav('/admin/productos')}/>
        <StatCard label="Categorías" value={r?.totalCategorias||0} icon={Tags} color="text-purple-600" bgColor="bg-purple-50" onClick={()=>nav('/admin/categorias')}/>
        <StatCard label="Usuarios" value={r?.totalUsuarios||0} icon={Users} color="text-emerald-600" bgColor="bg-emerald-50" onClick={()=>nav('/admin/usuarios')}/>
        <StatCard label="Pedidos" value={r?.totalPedidos||0} icon={ClipboardList} color="text-amber-600" bgColor="bg-amber-50" onClick={()=>nav('/admin/pedidos')}/>
        <StatCard label="Facturas" value={r?.totalFacturas||0} icon={FileText} color="text-brand-500" bgColor="bg-brand-50" onClick={()=>nav('/admin/facturas')}/>
        <StatCard label="Mesas" value={r?.totalMesas||0} icon={Armchair} color="text-indigo-600" bgColor="bg-indigo-50" onClick={()=>nav('/admin/mesas')}/>
      </div>
      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><DollarSign className="w-5 h-5 text-emerald-500"/>Resumen de Ventas</h3>
          <div className="space-y-3">
            <div className="flex justify-between py-2 border-b border-gray-100 dark:border-gray-800"><span className="text-sm text-gray-500">Ingreso Total</span><span className="font-bold text-gray-900 dark:text-white">{money(v?.ingresoTotal||0)}</span></div>
            <div className="flex justify-between py-2 border-b border-gray-100 dark:border-gray-800"><span className="text-sm text-gray-500">IVA Total</span><span className="font-medium text-gray-700 dark:text-gray-300">{money(v?.ivaTotal||0)}</span></div>
            <div className="flex justify-between py-2"><span className="text-sm text-gray-500">Ticket Promedio</span><span className="font-medium text-gray-700 dark:text-gray-300">{money(v?.ticketPromedio||0)}</span></div>
          </div>
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><TrendingUp className="w-5 h-5 text-blue-500"/>Top Productos</h3>
          <div className="space-y-3">{top.length===0?<p className="text-gray-400 text-sm py-4 text-center">Sin datos</p>:top.map((p:any,i:number)=>(
            <div key={i} className="flex items-center gap-3">
              <span className="w-7 h-7 bg-gold-50 text-gold-500 rounded-lg flex items-center justify-center text-xs font-bold">{i+1}</span>
              <span className="text-sm text-gray-700 dark:text-gray-300 flex-1 truncate">{p.nombre||p.producto||'—'}</span>
              <span className="text-xs font-medium text-gray-400">{p.cantidad||p.ventas||0} uds</span>
            </div>
          ))}</div>
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><ClipboardList className="w-5 h-5 text-amber-500"/>Pedidos por Estado</h3>
          <div className="space-y-2">{r?.pedidosPorEstado?Object.entries(r.pedidosPorEstado).map(([k,v]:any)=>(
            <div key={k} className="flex items-center justify-between py-1.5"><StatusBadge status={k}/><span className="font-bold text-gray-900 dark:text-white">{v}</span></div>
          )):<p className="text-gray-400 text-sm py-4 text-center">Sin datos</p>}</div>
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><AlertTriangle className="w-5 h-5 text-red-500"/>Stock Bajo</h3>
          <div className="space-y-2">{stock.length===0?<p className="text-gray-400 text-sm py-4 text-center">Todo en stock</p>:stock.map((p:any,i:number)=>(
            <div key={i} className="flex items-center justify-between py-1.5">
              <span className="text-sm text-gray-700 dark:text-gray-300 truncate">{p.nombre||'—'}</span>
              <span className={`badge ${(p.stock||0)<=0?'bg-red-100 text-red-700':'bg-amber-100 text-amber-700'}`}>{p.stock||0} uds</span>
            </div>
          ))}</div>
        </div>
      </div>
    </div>
  );
}
