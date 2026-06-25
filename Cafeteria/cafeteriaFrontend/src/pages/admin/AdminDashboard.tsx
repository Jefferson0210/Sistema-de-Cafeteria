import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { Package, Tags, Users, ClipboardList, FileText, Armchair, DollarSign, AlertTriangle, TrendingUp, ChefHat, BarChart3, Calendar, Download } from 'lucide-react';
import { dashboardService } from '../../services/dashboardService';
import StatCard from '../../components/ui/StatCard';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import { money } from '../../utils/format';
import { BarChart, Bar, XAxis, YAxis, Tooltip, ResponsiveContainer, PieChart, Pie, Cell, Legend } from 'recharts';
import toast from 'react-hot-toast';

const COLORS = ['#910048','#EAAA00','#002D72','#10b981','#f59e0b','#8b5cf6','#ef4444','#06b6d4','#ec4899','#84cc16'];
type Periodo = 'hoy'|'semana'|'mes'|'trimestre'|'anio'|'todo';

function getRango(p:Periodo):{desde:string,hasta:string}|null {
  const h=new Date();const fmt=(d:Date)=>d.toISOString().split('T')[0];const hasta=fmt(h);
  switch(p){
    case 'hoy':return{desde:hasta,hasta};
    case 'semana':{const d=new Date(h);d.setDate(d.getDate()-7);return{desde:fmt(d),hasta};}
    case 'mes':{const d=new Date(h);d.setMonth(d.getMonth()-1);return{desde:fmt(d),hasta};}
    case 'trimestre':{const d=new Date(h);d.setMonth(d.getMonth()-3);return{desde:fmt(d),hasta};}
    case 'anio':{const d=new Date(h);d.setFullYear(d.getFullYear()-1);return{desde:fmt(d),hasta};}
    default:return null;
  }
}

const periodoLabel:{[k:string]:string}={hoy:'Hoy',semana:'Última semana',mes:'Último mes',trimestre:'Último trimestre',anio:'Último año',todo:'Todo el tiempo'};

export default function AdminDashboard() {
  const nav=useNavigate();
  const [r,setR]=useState<any>(null);
  const [v,setV]=useState<any>(null);
  const [top,setTop]=useState<any[]>([]);
  const [stock,setStock]=useState<any[]>([]);
  const [meseros,setMeseros]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [periodo,setPeriodo]=useState<Periodo>('todo');
  const [loadingVentas,setLoadingVentas]=useState(false);

  useEffect(()=>{
    Promise.all([dashboardService.resumen(),dashboardService.ventas(),dashboardService.topProductos(10),dashboardService.stockBajo(10),dashboardService.ventasPorMesero()])
    .then(([a,b,c,d,e])=>{setR(a.data.data);setV(b.data.data);setTop(c.data.data||[]);setStock(d.data.data||[]);setMeseros(e.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  useEffect(()=>{
    if(loading)return;setLoadingVentas(true);
    const rango=getRango(periodo);
    dashboardService.ventas(rango?.desde,rango?.hasta).then(r=>setV(r.data.data)).catch(()=>{}).finally(()=>setLoadingVentas(false));
  },[periodo]);

  const exportarPDF=async()=>{
    try{
      const jspdf=await import('jspdf');
      const autoTable=(await import('jspdf-autotable')).default;
      const doc:any=new jspdf.default();
      const hoy=new Date().toLocaleDateString('es-EC',{day:'2-digit',month:'long',year:'numeric'});

      doc.setFillColor(145,0,72);
      doc.rect(0,0,210,35,'F');
      doc.setTextColor(255,255,255);
      doc.setFontSize(20);doc.setFont('helvetica','bold');
      doc.text('Cafetería UIDE',14,18);
      doc.setFontSize(10);doc.setFont('helvetica','normal');
      doc.text(`Reporte generado: ${hoy} | Periodo: ${periodoLabel[periodo]}`,14,28);

      let y=45;
      doc.setTextColor(0,0,0);

      doc.setFontSize(14);doc.setFont('helvetica','bold');
      doc.text('Resumen Financiero',14,y);y+=8;
      autoTable(doc,{startY:y,head:[['Concepto','Valor']],body:[
        ['Ingreso Total',`$${(v?.ingresoTotal||0).toFixed(2)}`],
        ['IVA Recaudado',`$${(v?.ivaTotal||0).toFixed(2)}`],
        ['Ticket Promedio',`$${(v?.ticketPromedio||0).toFixed(2)}`],
        ['Facturas Pagadas',`${v?.facturasPagadas||0}`],
        ['Por Cobrar',`$${(v?.montoPorCobrar||0).toFixed(2)}`],
        ['Facturas Pendientes',`${v?.facturasPendientes||0}`],
      ],theme:'grid',headStyles:{fillColor:[145,0,72]},margin:{left:14,right:14}});
      y=doc.lastAutoTable.finalY+12;

      doc.setFontSize(14);doc.setFont('helvetica','bold');
      doc.text('Top Productos Vendidos',14,y);y+=8;
      autoTable(doc,{startY:y,head:[['#','Producto','Vendidos','Órdenes','Ingreso']],body:top.map((p:any,i:number)=>[`${i+1}`,p.nombre,`${p.cantidadVendida}`,`${p.vecesOrdenado}`,`$${(p.ingresoGenerado||0).toFixed(2)}`]),theme:'grid',headStyles:{fillColor:[145,0,72]},margin:{left:14,right:14}});
      y=doc.lastAutoTable.finalY+12;

      if(stock.length>0){
        if(y>240){doc.addPage();y=20;}
        doc.setFontSize(14);doc.setFont('helvetica','bold');
        doc.text('Alerta de Stock Bajo',14,y);y+=8;
        autoTable(doc,{startY:y,head:[['Producto','Stock','Precio']],body:stock.map((p:any)=>[p.nombre,`${p.stock}`,`$${(p.precio||0).toFixed(2)}`]),theme:'grid',headStyles:{fillColor:[239,68,68]},margin:{left:14,right:14}});
        y=doc.lastAutoTable.finalY+12;
      }

      if(meseros.length>0){
        if(y>240){doc.addPage();y=20;}
        doc.setFontSize(14);doc.setFont('helvetica','bold');
        doc.text('Rendimiento por Mesero',14,y);y+=8;
        autoTable(doc,{startY:y,head:[['Mesero','Pedidos','Ingreso']],body:meseros.map((m:any)=>[m.nombre,`${m.totalPedidos}`,`$${(m.ingresoGenerado||0).toFixed(2)}`]),theme:'grid',headStyles:{fillColor:[0,45,114]},margin:{left:14,right:14}});
        y=doc.lastAutoTable.finalY+12;
      }

      if(r?.pedidosPorEstado){
        if(y>240){doc.addPage();y=20;}
        doc.setFontSize(14);doc.setFont('helvetica','bold');
        doc.text('Pedidos por Estado',14,y);y+=8;
        autoTable(doc,{startY:y,head:[['Estado','Cantidad']],body:Object.entries(r.pedidosPorEstado).map(([k,val]:any)=>[k,`${val}`]),theme:'grid',headStyles:{fillColor:[234,170,0],textColor:[0,0,0]},margin:{left:14,right:14}});
      }

      const pages=doc.getNumberOfPages();
      for(let i=1;i<=pages;i++){doc.setPage(i);doc.setFontSize(8);doc.setTextColor(150);doc.text(`Cafetería UIDE — Página ${i} de ${pages}`,105,290,{align:'center'});}

      doc.save(`reporte-cafeteria-${periodo}-${new Date().toISOString().split('T')[0]}.pdf`);
      toast.success('PDF descargado');
    }catch(e){console.error(e);toast.error('Error al generar PDF');}
  };

  const exportarExcel=async()=>{
    try{
      const XLSX=await import('xlsx');
      const wb=XLSX.utils.book_new();

      const resumen=[['REPORTE CAFETERÍA UIDE'],[`Periodo: ${periodoLabel[periodo]}`],[`Fecha: ${new Date().toLocaleDateString('es-EC')}`],[],['RESUMEN FINANCIERO'],['Concepto','Valor'],['Ingreso Total',v?.ingresoTotal||0],['IVA Recaudado',v?.ivaTotal||0],['Ticket Promedio',v?.ticketPromedio||0],['Facturas Pagadas',v?.facturasPagadas||0],['Por Cobrar',v?.montoPorCobrar||0],['Facturas Pendientes',v?.facturasPendientes||0],[],['RESUMEN GENERAL'],['Total Productos',r?.totalProductos||0],['Total Categorías',r?.totalCategorias||0],['Total Usuarios',r?.totalUsuarios||0],['Total Pedidos',r?.totalPedidos||0],['Total Facturas',r?.totalFacturas||0],['Total Mesas',r?.totalMesas||0]];
      const ws1=XLSX.utils.aoa_to_sheet(resumen);ws1['!cols']=[{wch:25},{wch:15}];
      XLSX.utils.book_append_sheet(wb,ws1,'Resumen');

      const prodRows=top.map((p:any,i:number)=>[i+1,p.nombre,p.cantidadVendida,p.vecesOrdenado,p.ingresoGenerado]);
      prodRows.push([]);prodRows.push(['','TOTAL',top.reduce((s:number,p:any)=>s+p.cantidadVendida,0),'',top.reduce((s:number,p:any)=>s+p.ingresoGenerado,0)]);
      const ws2=XLSX.utils.aoa_to_sheet([['#','Producto','Uds Vendidas','Veces Ordenado','Ingreso'],...prodRows]);
      ws2['!cols']=[{wch:5},{wch:30},{wch:15},{wch:15},{wch:18}];
      XLSX.utils.book_append_sheet(wb,ws2,'Top Productos');

      if(stock.length>0){const ws3=XLSX.utils.aoa_to_sheet([['Producto','Stock','Precio'],...stock.map((p:any)=>[p.nombre,p.stock,p.precio])]);ws3['!cols']=[{wch:30},{wch:15},{wch:15}];XLSX.utils.book_append_sheet(wb,ws3,'Stock Bajo');}
      if(meseros.length>0){const ws4=XLSX.utils.aoa_to_sheet([['Mesero','Pedidos','Ingreso'],...meseros.map((m:any)=>[m.nombre,m.totalPedidos,m.ingresoGenerado])]);ws4['!cols']=[{wch:25},{wch:15},{wch:18}];XLSX.utils.book_append_sheet(wb,ws4,'Meseros');}
      if(r?.pedidosPorEstado){const ws5=XLSX.utils.aoa_to_sheet([['Estado','Cantidad'],...Object.entries(r.pedidosPorEstado).map(([k,val]:any)=>[k,val])]);ws5['!cols']=[{wch:20},{wch:12}];XLSX.utils.book_append_sheet(wb,ws5,'Pedidos Estado');}

      XLSX.writeFile(wb,`reporte-cafeteria-${periodo}-${new Date().toISOString().split('T')[0]}.xlsx`);
      toast.success('Excel descargado');
    }catch(e){console.error(e);toast.error('Error al generar Excel');}
  };

  if(loading) return <LoadingSpinner/>;

  const topChart=top.slice(0,8).map(p=>({name:p.nombre?.length>14?p.nombre.substring(0,14)+'…':p.nombre,vendidos:p.cantidadVendida,ingreso:p.ingresoGenerado}));
  const pedidosEstado=r?.pedidosPorEstado?Object.entries(r.pedidosPorEstado).filter(([,v]:any)=>v>0).map(([k,v]:any)=>({name:k.replace('_',' '),value:v})):[];
  const metodosPago=v?.ingresosPorMetodo?Object.entries(v.ingresosPorMetodo).filter(([,v]:any)=>v>0).map(([k,v]:any)=>({name:k.replace('_',' '),value:v})):[];
  const meseroChart=meseros.map(m=>({name:m.nombre?.split(' ')[0]||'',pedidos:m.totalPedidos,ingreso:m.ingresoGenerado}));
  const totalVendido=top.reduce((s:number,p:any)=>s+(p.cantidadVendida||0),0);
  const totalIngreso=top.reduce((s:number,p:any)=>s+(p.ingresoGenerado||0),0);

  return(
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Dashboard</h1><p className="text-gray-500 text-sm mt-1">Estadísticas del sistema</p></div>
        <div className="flex gap-2">
          <button onClick={exportarPDF} className="btn-outline btn-sm"><Download className="w-4 h-4"/>PDF</button>
          <button onClick={exportarExcel} className="btn-sm bg-emerald-600 hover:bg-emerald-700 text-white rounded-xl px-4 py-2 text-xs font-semibold flex items-center gap-2"><Download className="w-4 h-4"/>Excel</button>
        </div>
      </div>

      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-6 gap-4">
        <StatCard label="Productos" value={r?.totalProductos||0} icon={Package} color="text-blue-600" bgColor="bg-blue-50" onClick={()=>nav('/admin/productos')}/>
        <StatCard label="Categorías" value={r?.totalCategorias||0} icon={Tags} color="text-purple-600" bgColor="bg-purple-50" onClick={()=>nav('/admin/categorias')}/>
        <StatCard label="Usuarios" value={r?.totalUsuarios||0} icon={Users} color="text-emerald-600" bgColor="bg-emerald-50" onClick={()=>nav('/admin/usuarios')}/>
        <StatCard label="Pedidos" value={r?.totalPedidos||0} icon={ClipboardList} color="text-amber-600" bgColor="bg-amber-50" onClick={()=>nav('/admin/pedidos')}/>
        <StatCard label="Facturas" value={r?.totalFacturas||0} icon={FileText} color="text-brand-500" bgColor="bg-brand-50" onClick={()=>nav('/admin/facturas')}/>
        <StatCard label="Mesas" value={r?.totalMesas||0} icon={Armchair} color="text-indigo-600" bgColor="bg-indigo-50" onClick={()=>nav('/admin/mesas')}/>
      </div>

      <div className="card p-4">
        <div className="flex items-center gap-3 flex-wrap">
          <Calendar className="w-5 h-5 text-brand-500"/>
          <span className="text-sm font-medium text-gray-700 dark:text-gray-300">Periodo:</span>
          <div className="flex gap-2 overflow-x-auto scrollbar-hide">
            {(['hoy','semana','mes','trimestre','anio','todo'] as Periodo[]).map(p=>(
              <button key={p} onClick={()=>setPeriodo(p)} className={`btn-sm whitespace-nowrap ${periodo===p?'btn-gold':'btn-outline'}`}>{periodoLabel[p]}</button>
            ))}
          </div>
        </div>
      </div>

      <div className="grid sm:grid-cols-2 lg:grid-cols-4 gap-4 relative">
        {loadingVentas&&<div className="absolute inset-0 bg-white/50 dark:bg-gray-950/50 z-10 flex items-center justify-center rounded-xl"><div className="animate-spin w-6 h-6 border-2 border-brand-500 border-t-transparent rounded-full"/></div>}
        <div className="card p-5 border-l-4 border-l-emerald-500">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Ingreso</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">{money(v?.ingresoTotal||0)}</p>
          <p className="text-xs text-emerald-600 mt-1">{v?.facturasPagadas||0} facturas</p>
          <p className="text-[10px] text-gray-400 mt-0.5">{periodoLabel[periodo]}</p>
        </div>
        <div className="card p-5 border-l-4 border-l-blue-500">
          <p className="text-xs text-gray-500 uppercase tracking-wider">IVA</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">{money(v?.ivaTotal||0)}</p>
          <p className="text-xs text-blue-600 mt-1">15% sobre ventas</p>
        </div>
        <div className="card p-5 border-l-4 border-l-amber-500">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Ticket Promedio</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">{money(v?.ticketPromedio||0)}</p>
          <p className="text-xs text-amber-600 mt-1">Por factura</p>
        </div>
        <div className="card p-5 border-l-4 border-l-red-500">
          <p className="text-xs text-gray-500 uppercase tracking-wider">Por Cobrar</p>
          <p className="text-2xl font-bold text-gray-900 dark:text-white mt-1">{money(v?.montoPorCobrar||0)}</p>
          <p className="text-xs text-red-600 mt-1">{v?.facturasPendientes||0} pendientes</p>
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><TrendingUp className="w-5 h-5 text-brand-500"/>Top Productos</h3>
          {topChart.length>0?(
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={topChart} layout="vertical" margin={{left:10,right:20}}>
                <XAxis type="number" tick={{fontSize:11}} stroke="#9ca3af"/>
                <YAxis type="category" dataKey="name" width={110} tick={{fontSize:11}} stroke="#9ca3af"/>
                <Tooltip formatter={(v:any)=>[v,'Vendidos']} contentStyle={{borderRadius:'12px',border:'none',boxShadow:'0 4px 12px rgba(0,0,0,0.1)'}}/>
                <Bar dataKey="vendidos" fill="#910048" radius={[0,6,6,0]} barSize={20}/>
              </BarChart>
            </ResponsiveContainer>
          ):<p className="text-gray-400 text-sm text-center py-10">Sin datos</p>}
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><BarChart3 className="w-5 h-5 text-blue-500"/>Pedidos por Estado</h3>
          {pedidosEstado.length>0?(
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={pedidosEstado} cx="50%" cy="50%" innerRadius={55} outerRadius={95} paddingAngle={3} dataKey="value">
                  {pedidosEstado.map((_:any,i:number)=><Cell key={i} fill={COLORS[i%COLORS.length]}/>)}
                </Pie>
                <Tooltip formatter={(v:any)=>[v,'pedidos']} contentStyle={{borderRadius:'12px',border:'none',boxShadow:'0 4px 12px rgba(0,0,0,0.1)'}}/>
                <Legend wrapperStyle={{fontSize:'12px'}}/>
              </PieChart>
            </ResponsiveContainer>
          ):<p className="text-gray-400 text-sm text-center py-10">Sin datos</p>}
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><DollarSign className="w-5 h-5 text-emerald-500"/>Por Método de Pago</h3>
          {metodosPago.length>0?(
            <ResponsiveContainer width="100%" height={280}>
              <PieChart>
                <Pie data={metodosPago} cx="50%" cy="50%" outerRadius={95} paddingAngle={3} dataKey="value">
                  {metodosPago.map((_:any,i:number)=><Cell key={i} fill={COLORS[(i+2)%COLORS.length]}/>)}
                </Pie>
                <Tooltip formatter={(v:any)=>[money(v),'Ingreso']} contentStyle={{borderRadius:'12px',border:'none',boxShadow:'0 4px 12px rgba(0,0,0,0.1)'}}/>
                <Legend wrapperStyle={{fontSize:'12px'}}/>
              </PieChart>
            </ResponsiveContainer>
          ):<p className="text-gray-400 text-sm text-center py-10">Sin pagos</p>}
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><ChefHat className="w-5 h-5 text-amber-500"/>Rendimiento por Mesero</h3>
          {meseroChart.length>0?(
            <ResponsiveContainer width="100%" height={280}>
              <BarChart data={meseroChart} margin={{left:0,right:10}}>
                <XAxis dataKey="name" tick={{fontSize:11}} stroke="#9ca3af"/>
                <YAxis tick={{fontSize:11}} stroke="#9ca3af"/>
                <Tooltip formatter={(v:any,name:any)=>[name==='ingreso'?money(v):v,name==='ingreso'?'Ingreso':'Pedidos']} contentStyle={{borderRadius:'12px',border:'none',boxShadow:'0 4px 12px rgba(0,0,0,0.1)'}}/>
                <Bar dataKey="pedidos" fill="#EAAA00" radius={[6,6,0,0]} barSize={22} name="Pedidos"/>
                <Bar dataKey="ingreso" fill="#002D72" radius={[6,6,0,0]} barSize={22} name="Ingreso"/>
                <Legend wrapperStyle={{fontSize:'12px'}}/>
              </BarChart>
            </ResponsiveContainer>
          ):<p className="text-gray-400 text-sm text-center py-10">Sin datos</p>}
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><TrendingUp className="w-5 h-5 text-brand-500"/>Ranking de Productos</h3>
          <div className="text-xs text-gray-500 flex justify-between mb-3"><span>{totalVendido} uds vendidas</span><span>{money(totalIngreso)} ingreso</span></div>
          <div className="space-y-2">{top.map((p:any,i:number)=>{
            const pct=totalVendido>0?((p.cantidadVendida/totalVendido)*100):0;
            return(
              <div key={i} className="flex items-center gap-3 py-2 border-b border-gray-50 dark:border-gray-800 last:border-0">
                <span className={`w-7 h-7 rounded-lg flex items-center justify-center text-xs font-bold ${i<3?'bg-gold-400 text-gray-900':'bg-gray-100 dark:bg-gray-800 text-gray-500'}`}>{i+1}</span>
                <div className="flex-1 min-w-0">
                  <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{p.nombre}</p>
                  <div className="w-full h-1.5 bg-gray-100 dark:bg-gray-800 rounded-full mt-1 overflow-hidden">
                    <div className="h-full bg-brand-500 rounded-full" style={{width:`${pct}%`}}/>
                  </div>
                </div>
                <div className="text-right shrink-0">
                  <p className="text-sm font-bold text-gray-900 dark:text-white">{p.cantidadVendida} uds</p>
                  <p className="text-xs text-emerald-600">{money(p.ingresoGenerado)}</p>
                </div>
              </div>
            );
          })}</div>
          {top.length===0&&<p className="text-gray-400 text-sm text-center py-4">Sin datos</p>}
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><AlertTriangle className="w-5 h-5 text-red-500"/>Alerta de Stock</h3>
          <div className="space-y-2">{stock.map((p:any,i:number)=>(
            <div key={i} className="flex items-center justify-between py-2 border-b border-gray-50 dark:border-gray-800 last:border-0">
              <div className="flex-1 min-w-0">
                <p className="text-sm font-medium text-gray-900 dark:text-white truncate">{p.nombre}</p>
                <p className="text-xs text-gray-400">{money(p.precio)} c/u</p>
              </div>
              <span className={`badge ${(p.stock||0)<=0?'bg-red-100 text-red-700 dark:bg-red-900/30 dark:text-red-400':'bg-amber-100 text-amber-700 dark:bg-amber-900/30 dark:text-amber-400'}`}>{p.stock||0} uds</span>
            </div>
          ))}</div>
          {stock.length===0&&<p className="text-emerald-500 text-sm text-center py-4">✓ Todo en stock</p>}
        </div>
      </div>

      <div className="grid lg:grid-cols-2 gap-6">
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><ClipboardList className="w-5 h-5 text-amber-500"/>Pedidos por Estado</h3>
          <div className="space-y-3">{r?.pedidosPorEstado?Object.entries(r.pedidosPorEstado).map(([k,val]:any)=>(
            <div key={k} className="flex items-center justify-between">
              <StatusBadge status={k}/>
              <div className="flex items-center gap-2">
                <div className="w-28 h-2 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                  <div className="h-full bg-brand-500 rounded-full" style={{width:`${Math.min(100,(val/(r?.totalPedidos||1))*100)}%`}}/>
                </div>
                <span className="font-bold text-gray-900 dark:text-white text-sm w-8 text-right">{val}</span>
              </div>
            </div>
          )):null}</div>
        </div>
        <div className="card p-6">
          <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><Armchair className="w-5 h-5 text-indigo-500"/>Estado de Mesas</h3>
          <div className="space-y-3">{r?.mesasPorEstado?Object.entries(r.mesasPorEstado).map(([k,val]:any)=>(
            <div key={k} className="flex items-center justify-between">
              <StatusBadge status={k}/>
              <div className="flex items-center gap-2">
                <div className="w-28 h-2 bg-gray-100 dark:bg-gray-800 rounded-full overflow-hidden">
                  <div className="h-full bg-indigo-500 rounded-full" style={{width:`${Math.min(100,(val/(r?.totalMesas||1))*100)}%`}}/>
                </div>
                <span className="font-bold text-gray-900 dark:text-white text-sm w-8 text-right">{val}</span>
              </div>
            </div>
          )):null}</div>
        </div>
      </div>
    </div>
  );
}