import React, { useState, useEffect } from 'react';
import { Search, Plus, Minus, Trash2, Receipt, CheckCircle } from 'lucide-react';
import { productService } from '../../services/productService';
import { userService } from '../../services/userService';
import { facturaService } from '../../services/facturaService';
import { useAuth } from '../../context/AuthContext';
import { Producto } from '../../types';
import { money } from '../../utils/format';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import toast from 'react-hot-toast';

interface ItemSel { producto:Producto; cantidad:number; }

export default function CajeroFacturaManual() {
  const { user } = useAuth();
  const [productos,setProductos]=useState<Producto[]>([]);
  const [clientes,setClientes]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [search,setSearch]=useState('');
  const [clienteId,setClienteId]=useState<number|null>(null);
  const [items,setItems]=useState<ItemSel[]>([]);
  const [descuento,setDescuento]=useState(0);
  const [sending,setSending]=useState(false);
  const [success,setSuccess]=useState(false);

  useEffect(()=>{
    Promise.all([productService.disponibles(),userService.porRol('CLIENTE')])
    .then(([p,c])=>{setProductos(p.data.data||[]);setClientes(c.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  const addItem=(p:Producto)=>setItems(prev=>{const ex=prev.find(i=>i.producto.id===p.id);if(ex)return prev.map(i=>i.producto.id===p.id?{...i,cantidad:i.cantidad+1}:i);return[...prev,{producto:p,cantidad:1}];});
  const removeItem=(id:number)=>setItems(p=>p.filter(i=>i.producto.id!==id));
  const updateQty=(id:number,q:number)=>{if(q<=0)return removeItem(id);setItems(p=>p.map(i=>i.producto.id===id?{...i,cantidad:q}:i));};

  const subtotal=items.reduce((s,i)=>s+i.producto.precio*i.cantidad,0);
  const iva=subtotal*0.15;
  const total=subtotal+iva-descuento;

  const enviar=async()=>{
    if(items.length===0){toast.error('Agrega productos');return;}
    setSending(true);
    try{
      await facturaService.manual({clienteId,cajeroId:user!.id,empresaRuc:null,descuento,items:items.map(i=>({productoId:i.producto.id,cantidad:i.cantidad}))});
      toast.success('Factura generada');setSuccess(true);
    }catch(e:any){toast.error(e.response?.data?.message||'Error');}
    setSending(false);
  };

  if(loading) return <LoadingSpinner/>;
  if(success) return(<div className="flex items-center justify-center py-20"><div className="text-center"><CheckCircle className="w-16 h-16 text-green-500 mx-auto mb-4"/><h2 className="text-2xl font-display font-bold mb-2">¡Factura Generada!</h2><button onClick={()=>{setSuccess(false);setItems([]);setDescuento(0);setClienteId(null);}} className="btn-gold mt-4">Nueva Factura</button></div></div>);

  const filteredProds=search?productos.filter(p=>p.nombre.toLowerCase().includes(search.toLowerCase())):productos;

  return(
    <div className="space-y-6">
      <h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Factura Manual</h1>
      <div className="grid lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 space-y-4">
          <div className="card p-4"><label className="label">Cliente (opcional)</label><select className="input" value={clienteId||''} onChange={e=>setClienteId(+e.target.value||null)}><option value="">Consumidor final</option>{clientes.map((c:any)=><option key={c.id} value={c.id}>{c.nombre} {c.apellido}</option>)}</select></div>
          <div className="relative"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"/><input className="input pl-9" placeholder="Buscar producto..." value={search} onChange={e=>setSearch(e.target.value)}/></div>
          <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-3">{filteredProds.slice(0,18).map(p=>(
            <button key={p.id} onClick={()=>addItem(p)} className="card p-3 text-left hover:shadow-md hover:border-gold-300 transition-all"><div className="flex items-center gap-3"><div className="w-10 h-10 bg-brand-50 rounded-lg flex items-center justify-center text-lg">🍽️</div><div className="min-w-0 flex-1"><p className="text-sm font-medium truncate">{p.nombre}</p><p className="text-brand-500 font-bold text-sm">{money(p.precio)}</p></div><Plus className="w-4 h-4 text-gray-300"/></div></button>
          ))}</div>
        </div>
        <div className="card p-5 sticky top-24 h-fit">
          <h3 className="font-semibold text-gray-900 mb-4">Detalle</h3>
          {items.length===0?<p className="text-gray-400 text-sm py-8 text-center">Agrega productos</p>:(
            <div className="space-y-2 mb-4">{items.map(i=>(
              <div key={i.producto.id} className="flex items-center gap-2">
                <span className="text-sm flex-1 truncate">{i.producto.nombre}</span>
                <div className="flex items-center gap-1"><button onClick={()=>updateQty(i.producto.id,i.cantidad-1)} className="w-5 h-5 border rounded text-xs flex items-center justify-center">-</button><span className="w-5 text-center text-xs font-bold">{i.cantidad}</span><button onClick={()=>updateQty(i.producto.id,i.cantidad+1)} className="w-5 h-5 border rounded text-xs flex items-center justify-center">+</button></div>
                <span className="text-sm font-medium w-12 text-right">{money(i.producto.precio*i.cantidad)}</span>
                <button onClick={()=>removeItem(i.producto.id)} className="text-gray-300 hover:text-red-500"><Trash2 className="w-3 h-3"/></button>
              </div>
            ))}</div>
          )}
          <div><label className="label">Descuento ($)</label><input className="input" type="number" step="0.01" value={descuento} onChange={e=>setDescuento(+e.target.value)}/></div>
          <div className="border-t pt-3 mt-3 space-y-1">
            <div className="flex justify-between text-sm text-gray-500"><span>Subtotal</span><span>{money(subtotal)}</span></div>
            <div className="flex justify-between text-sm text-gray-500"><span>IVA 15%</span><span>{money(iva)}</span></div>
            {descuento>0&&<div className="flex justify-between text-sm text-red-500"><span>Descuento</span><span>-{money(descuento)}</span></div>}
            <div className="flex justify-between font-bold text-lg pt-1"><span>Total</span><span className="text-brand-500">{money(total)}</span></div>
          </div>
          <button onClick={enviar} disabled={sending||items.length===0} className="btn-gold w-full mt-4"><Receipt className="w-4 h-4"/>{sending?'Procesando...':'Generar Factura'}</button>
        </div>
      </div>
    </div>
  );
}
