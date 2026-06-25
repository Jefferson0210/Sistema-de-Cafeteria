import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, ClipboardList, CheckCircle, Send, FileText, UserCheck, Trash2, BookmarkPlus } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { mesaService } from '../../services/mesaService';
import { pedidoService } from '../../services/pedidoService';
import { Mesa } from '../../types';
import { money } from '../../utils/format';
import toast from 'react-hot-toast';

interface DatosFactura { nombre:string; cedula:string; direccion:string; email:string; telefono:string; }

const STORAGE_KEY='uide_datos_facturacion';

function getSaved():DatosFactura[] {
  try{return JSON.parse(localStorage.getItem(STORAGE_KEY)||'[]');}catch{return[];}
}
function saveDatos(d:DatosFactura) {
  const list=getSaved().filter(s=>s.cedula!==d.cedula||s.nombre!==d.nombre);
  list.unshift(d);
  localStorage.setItem(STORAGE_KEY,JSON.stringify(list.slice(0,5)));
}
function removeDatos(i:number) {
  const list=getSaved();list.splice(i,1);
  localStorage.setItem(STORAGE_KEY,JSON.stringify(list));
}

export default function CheckoutPage() {
  const { items,subtotal,iva,total,clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [mesas,setMesas]=useState<Mesa[]>([]);
  const [mesaId,setMesaId]=useState<number|null>(null);
  const [notas,setNotas]=useState('');
  const [loading,setLoading]=useState(false);
  const [success,setSuccess]=useState(false);
  const [consumidorFinal,setConsumidorFinal]=useState(false);
  const [factura,setFactura]=useState<DatosFactura>({nombre:'',cedula:'',direccion:'',email:'',telefono:''});
  const [guardados,setGuardados]=useState<DatosFactura[]>([]);
  const [seleccionado,setSeleccionado]=useState<number|null>(null);

  useEffect(()=>{
    if(!user){navigate('/');return;}
    if(items.length===0&&!success){navigate('/cliente/menu');return;}
    mesaService.disponibles().then(r=>setMesas(r.data.data||[])).catch(()=>{});
    const saved=getSaved();
    setGuardados(saved);
    if(saved.length>0){
      setFactura(saved[0]);setSeleccionado(0);
    }else if(user){
      setFactura({nombre:`${user.nombre} ${user.apellido}`,cedula:'',direccion:'',email:user.email||'',telefono:user.telefono||''});
    }
  },[]);

  const seleccionar=(i:number)=>{
    setSeleccionado(i);setFactura(guardados[i]);setConsumidorFinal(false);
  };

  const nuevo=()=>{
    setSeleccionado(null);setConsumidorFinal(false);
    setFactura(user?{nombre:`${user.nombre} ${user.apellido}`,cedula:'',direccion:'',email:user.email||'',telefono:user.telefono||''}:{nombre:'',cedula:'',direccion:'',email:'',telefono:''});
  };

  const eliminarGuardado=(i:number)=>{
    removeDatos(i);
    const updated=getSaved();setGuardados(updated);
    if(seleccionado===i){nuevo();}
    toast.success('Datos eliminados');
  };

  const toggleConsumidorFinal=()=>{
    const next=!consumidorFinal;setConsumidorFinal(next);setSeleccionado(null);
    if(next){setFactura({nombre:'Consumidor Final',cedula:'9999999999999',direccion:'N/A',email:'',telefono:''});}
    else{nuevo();}
  };

  const handleOrder=async()=>{
    if(!mesaId){toast.error('Selecciona una mesa');return;}
    setLoading(true);
    try{
      await pedidoService.crear({mesaId,clienteId:user!.id,notas,datosFacturacion:consumidorFinal?null:factura,items:items.map(i=>({productoId:i.producto.id,cantidad:i.cantidad,notas:i.notas||''}))});
      // Guardar datos para próxima vez
      if(!consumidorFinal&&factura.nombre&&factura.cedula){
        saveDatos(factura);
      }
      clearCart();setSuccess(true);toast.success('¡Pedido realizado!');
    }catch(e:any){toast.error(e.response?.data?.message||'Error al crear pedido');}
    setLoading(false);
  };

  if(success) return(
    <div className="flex items-center justify-center py-20"><div className="text-center">
      <CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4"/>
      <h2 className="text-2xl font-display font-bold text-gray-900 dark:text-white mb-2">¡Pedido Confirmado!</h2>
      <p className="text-gray-500 mb-6">Tu pedido está siendo preparado</p>
      <div className="flex gap-3 justify-center"><button onClick={()=>navigate('/cliente/mis-pedidos')} className="btn-gold">Ver Mis Pedidos</button><button onClick={()=>navigate('/cliente/menu')} className="btn-outline">Volver al Menú</button></div>
    </div></div>
  );

  return(
    <div className="space-y-6">
      <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Confirmar Pedido</h1>
      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-5">
          {/* Mesa */}
          <div className="card p-6">
            <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><MapPin className="w-5 h-5 text-brand-500"/>Selecciona Mesa</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">{mesas.map(m=>(
              <button key={m.id} onClick={()=>setMesaId(m.id)} className={`p-3 rounded-xl border text-left transition-all ${mesaId===m.id?'border-brand-500 bg-brand-50 dark:bg-brand-900/20 ring-1 ring-brand-500':'border-gray-200 dark:border-gray-700 hover:border-gray-300'}`}>
                <p className="font-bold text-gray-900 dark:text-white">Mesa {m.numeroMesa}</p><p className="text-xs text-gray-500 mt-0.5">{m.ubicacion} · {m.capacidad}p</p>
              </button>
            ))}</div>
            {mesas.length===0&&<p className="text-gray-400 text-sm text-center py-4">No hay mesas disponibles</p>}
          </div>

          {/* Datos de facturación */}
          <div className="card p-6">
            <div className="flex items-center justify-between mb-4">
              <h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2"><FileText className="w-5 h-5 text-brand-500"/>Datos de Facturación</h3>
              <button onClick={toggleConsumidorFinal} className={`flex items-center gap-2 px-3 py-1.5 rounded-lg text-xs font-medium transition-all ${consumidorFinal?'bg-brand-500 text-white':'bg-gray-100 dark:bg-gray-800 text-gray-600 dark:text-gray-400'}`}>
                <UserCheck className="w-3.5 h-3.5"/>{consumidorFinal?'Consumidor Final ✓':'Consumidor Final'}
              </button>
            </div>

            {/* Datos guardados */}
            {guardados.length>0&&!consumidorFinal&&(
              <div className="mb-4">
                <p className="text-xs font-medium text-gray-500 mb-2">Datos guardados:</p>
                <div className="flex gap-2 overflow-x-auto scrollbar-hide pb-1">
                  {guardados.map((g,i)=>(
                    <div key={i} className="relative flex-shrink-0">
                      <button onClick={()=>seleccionar(i)} className={`px-3 py-2 rounded-lg border text-left text-xs transition-all min-w-[140px] ${seleccionado===i?'border-brand-500 bg-brand-50 dark:bg-brand-900/20 ring-1 ring-brand-500':'border-gray-200 dark:border-gray-700 hover:border-gray-300'}`}>
                        <p className="font-medium text-gray-900 dark:text-white truncate">{g.nombre}</p>
                        <p className="text-gray-400 truncate">{g.cedula}</p>
                      </button>
                      <button onClick={(e)=>{e.stopPropagation();eliminarGuardado(i);}} className="absolute -top-1.5 -right-1.5 w-5 h-5 bg-red-500 text-white rounded-full flex items-center justify-center hover:bg-red-600">
                        <Trash2 className="w-3 h-3"/>
                      </button>
                    </div>
                  ))}
                  <button onClick={nuevo} className={`px-3 py-2 rounded-lg border border-dashed text-xs flex items-center gap-1 min-w-[100px] justify-center transition-all ${seleccionado===null&&!consumidorFinal?'border-brand-500 text-brand-500':'border-gray-300 dark:border-gray-700 text-gray-400 hover:border-gray-400'}`}>
                    <BookmarkPlus className="w-3.5 h-3.5"/>Nuevo
                  </button>
                </div>
              </div>
            )}

            <div className="grid sm:grid-cols-2 gap-4">
              <div><label className="label">Nombre completo</label><input className="input" value={factura.nombre} onChange={e=>setFactura(p=>({...p,nombre:e.target.value}))} disabled={consumidorFinal}/></div>
              <div><label className="label">Cédula / RUC</label><input className="input" value={factura.cedula} onChange={e=>setFactura(p=>({...p,cedula:e.target.value}))} placeholder="0000000000" disabled={consumidorFinal}/></div>
              <div><label className="label">Dirección</label><input className="input" value={factura.direccion} onChange={e=>setFactura(p=>({...p,direccion:e.target.value}))} placeholder="Dirección" disabled={consumidorFinal}/></div>
              <div><label className="label">Email</label><input className="input" value={factura.email} onChange={e=>setFactura(p=>({...p,email:e.target.value}))} placeholder="email@ejemplo.com"/></div>
              <div><label className="label">Teléfono</label><input className="input" value={factura.telefono} onChange={e=>setFactura(p=>({...p,telefono:e.target.value}))} placeholder="0999999999"/></div>
            </div>
          </div>

          {/* Notas */}
          <div className="card p-6"><h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-3"><ClipboardList className="w-5 h-5 text-brand-500"/>Notas</h3><textarea className="input resize-none h-20" placeholder="Alergias, preferencias..." value={notas} onChange={e=>setNotas(e.target.value)}/></div>
        </div>

        {/* Resumen */}
        <div className="card p-6 sticky top-24 h-fit">
          <h3 className="font-semibold text-gray-900 dark:text-white mb-4">Resumen</h3>
          <div className="space-y-2 mb-4">{items.map(i=><div key={i.producto.id} className="flex justify-between text-sm"><span className="text-gray-600 dark:text-gray-400">{i.cantidad}× {i.producto.nombre}</span><span className="font-medium text-gray-900 dark:text-white">{money(i.producto.precio*i.cantidad)}</span></div>)}</div>
          <div className="border-t dark:border-gray-800 pt-3 space-y-1.5">
            <div className="flex justify-between text-sm text-gray-500"><span>Subtotal</span><span>{money(subtotal)}</span></div>
            <div className="flex justify-between text-sm text-gray-500"><span>IVA 15%</span><span>{money(iva)}</span></div>
            <div className="flex justify-between font-bold text-lg pt-1"><span className="text-gray-900 dark:text-white">Total</span><span className="text-brand-500">{money(total)}</span></div>
          </div>
          <div className="mt-3 pt-3 border-t dark:border-gray-800">
            <p className="text-xs text-gray-400 mb-1">Factura a nombre de:</p>
            <p className="text-sm font-medium text-gray-900 dark:text-white">{factura.nombre||'—'}</p>
            {factura.cedula&&factura.cedula!=='9999999999999'&&<p className="text-xs text-gray-500">{factura.cedula}</p>}
          </div>
          <button onClick={handleOrder} disabled={loading||!mesaId} className="btn-gold w-full mt-4"><Send className="w-4 h-4"/>{loading?'Procesando...':'Confirmar Pedido'}</button>
        </div>
      </div>
    </div>
  );
}