import React, { useState, useEffect } from 'react';
import { Search, Plus, Minus, Trash2, Send, CheckCircle } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { productService } from '../../services/productService';
import { mesaService } from '../../services/mesaService';
import { userService } from '../../services/userService';
import { pedidoService } from '../../services/pedidoService';
import { useAuth } from '../../context/AuthContext';
import { Producto, Mesa } from '../../types';
import { money } from '../../utils/format';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import toast from 'react-hot-toast';

interface ItemSel { producto:Producto; cantidad:number; notas:string; }

export default function MeseroNuevoPedido() {
  const { user } = useAuth();
  const navigate = useNavigate();
  const [productos,setProductos]=useState<Producto[]>([]);
  const [mesas,setMesas]=useState<Mesa[]>([]);
  const [clientes,setClientes]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [search,setSearch]=useState('');
  const [mesaId,setMesaId]=useState<number|null>(null);
  const [clienteId,setClienteId]=useState<number|null>(null);
  const [items,setItems]=useState<ItemSel[]>([]);
  const [notas,setNotas]=useState('');
  const [sending,setSending]=useState(false);
  const [success,setSuccess]=useState(false);

  useEffect(()=>{
    Promise.all([productService.disponibles(),mesaService.disponibles(),userService.porRol('CLIENTE')])
    .then(([p,m,c])=>{setProductos(p.data.data||[]);setMesas(m.data.data||[]);setClientes(c.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  const addItem=(p:Producto)=>{toast.success(`${p.nombre} agregado`,{icon:"🛒",duration:1500});
    setItems(prev=>{const ex=prev.find(i=>i.producto.id===p.id);if(ex)return prev.map(i=>i.producto.id===p.id?{...i,cantidad:i.cantidad+1}:i);return[...prev,{producto:p,cantidad:1,notas:''}];});
  };
  const removeItem=(id:number)=>setItems(p=>p.filter(i=>i.producto.id!==id));
  const updateQty=(id:number,q:number)=>{if(q<=0)return removeItem(id);setItems(p=>p.map(i=>i.producto.id===id?{...i,cantidad:q}:i));};

  const subtotal=items.reduce((s,i)=>s+i.producto.precio*i.cantidad,0);
  const total=subtotal*1.15;

  const enviar=async()=>{
    if(!mesaId){toast.error('Selecciona mesa');return;}
    if(!clienteId){toast.error('Selecciona cliente');return;}
    if(items.length===0){toast.error('Agrega productos');return;}
    setSending(true);
    try{
      await pedidoService.crear({mesaId,clienteId,meseroId:user!.id,notas,items:items.map(i=>({productoId:i.producto.id,cantidad:i.cantidad,notas:i.notas}))});
      toast.success('¡Pedido creado!');setSuccess(true);
    }catch(e:any){toast.error(e.response?.data?.message||'Error al crear pedido');}
    setSending(false);
  };

  if(loading) return <LoadingSpinner/>;
  if(success) return(
    <div className="flex items-center justify-center py-20">
      <div className="text-center"><CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4"/><h2 className="text-2xl font-display font-bold text-gray-900 mb-2">¡Pedido Creado!</h2><p className="text-gray-500 mb-6">El pedido fue enviado a cocina</p>
      <div className="flex gap-3 justify-center"><button onClick={()=>{setSuccess(false);setItems([]);setMesaId(null);setClienteId(null);setNotas('');}} className="btn-gold">Nuevo Pedido</button><button onClick={()=>navigate('/mesero/pedidos')} className="btn-outline">Ver Pedidos</button></div></div>
    </div>
  );

  const filteredProds=search?productos.filter(p=>p.nombre.toLowerCase().includes(search.toLowerCase())):productos;

  return(
    <div className="space-y-6">
      <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Nuevo Pedido</h1>
      <div className="grid lg:grid-cols-3 gap-6">
        {/* Left: product selection */}
        <div className="lg:col-span-2 space-y-4">
          <div className="card p-4 grid sm:grid-cols-2 gap-3">
            <div><label className="label">Mesa *</label><select className="input" value={mesaId||''} onChange={e=>setMesaId(+e.target.value||null)}>
              <option value="">Seleccionar mesa</option>{mesas.map(m=><option key={m.id} value={m.id}>Mesa {m.numeroMesa} ({m.capacidad}p - {m.ubicacion})</option>)}
            </select></div>
            <div><label className="label">Cliente *</label><select className="input" value={clienteId||''} onChange={e=>setClienteId(+e.target.value||null)}>
              <option value="">Seleccionar cliente</option>{clientes.map((c:any)=><option key={c.id} value={c.id}>{c.nombre} {c.apellido}</option>)}
            </select></div>
          </div>
          <div className="relative"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"/><input className="input pl-9" placeholder="Buscar producto..." value={search} onChange={e=>setSearch(e.target.value)}/></div>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">{filteredProds.slice(0,18).map(p=>(
            <button key={p.id} onClick={()=>addItem(p)} className="card p-3 text-left hover:shadow-md hover:border-gold-300 transition-all group">
              <div className="flex items-center gap-3">
                <div className="w-10 h-10 bg-brand-50 rounded-lg flex items-center justify-center text-lg flex-shrink-0 group-hover:scale-110 transition-transform">🍽️</div>
                <div className="min-w-0 flex-1"><p className="text-sm font-medium text-gray-900 truncate">{p.nombre}</p><p className="text-brand-500 font-bold text-sm">{money(p.precio)}</p></div>
                <Plus className="w-4 h-4 text-gray-300 group-hover:text-gold-500 transition-colors"/>
              </div>
            </button>
          ))}</div>
        </div>

        {/* Right: order summary */}
        <div className="card p-5 sticky top-24 h-fit">
          <h3 className="font-semibold text-gray-900 mb-4">Resumen del Pedido</h3>
          {items.length===0?<p className="text-gray-400 text-sm py-8 text-center">Agrega productos al pedido</p>:(
            <div className="space-y-3 mb-4">{items.map(i=>(
              <div key={i.producto.id} className="flex items-center gap-2">
                <div className="flex-1 min-w-0"><p className="text-sm font-medium truncate">{i.producto.nombre}</p><p className="text-xs text-gray-400">{money(i.producto.precio)} c/u</p></div>
                <div className="flex items-center gap-1">
                  <button onClick={()=>updateQty(i.producto.id,i.cantidad-1)} className="w-6 h-6 border rounded flex items-center justify-center hover:bg-gray-50"><Minus className="w-3 h-3"/></button>
                  <span className="w-6 text-center text-sm font-bold">{i.cantidad}</span>
                  <button onClick={()=>updateQty(i.producto.id,i.cantidad+1)} className="w-6 h-6 border rounded flex items-center justify-center hover:bg-gray-50"><Plus className="w-3 h-3"/></button>
                </div>
                <span className="text-sm font-medium w-14 text-right">{money(i.producto.precio*i.cantidad)}</span>
                <button onClick={()=>removeItem(i.producto.id)} className="text-gray-300 hover:text-red-500"><Trash2 className="w-3.5 h-3.5"/></button>
              </div>
            ))}</div>
          )}
          <textarea className="input resize-none h-16 text-sm mb-3" placeholder="Notas del pedido..." value={notas} onChange={e=>setNotas(e.target.value)}/>
          <div className="border-t pt-3 space-y-1.5 mb-4">
            <div className="flex justify-between text-sm text-gray-500"><span>Subtotal</span><span>{money(subtotal)}</span></div>
            <div className="flex justify-between text-sm text-gray-500"><span>IVA 15%</span><span>{money(subtotal*0.15)}</span></div>
            <div className="flex justify-between font-bold text-lg"><span>Total</span><span className="text-brand-500">{money(total)}</span></div>
          </div>
          <button onClick={enviar} disabled={sending||items.length===0} className="btn-gold w-full"><Send className="w-4 h-4"/>{sending?'Enviando...':'Enviar Pedido'}</button>
        </div>
      </div>
    </div>
  );
}
