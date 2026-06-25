import React, { useState, useEffect } from 'react';
import { ClipboardList, Trash2, Search, Plus } from 'lucide-react';
import { pedidoService } from '../../services/pedidoService';
import { productService } from '../../services/productService';
import { useAuth } from '../../context/AuthContext';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function MeseroPedidos() {
  const { user } = useAuth();
  const [pedidos,setPedidos]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [filtro,setFiltro]=useState('');
  const [editPedido,setEditPedido]=useState<any>(null);
  const [productos,setProductos]=useState<any[]>([]);
  const [buscar,setBuscar]=useState('');

  const load=async()=>{
    try{
      const res=user?await pedidoService.porMesero(user.id):await pedidoService.listar();
      setPedidos((res.data.data||[]).reverse());
    }catch{}finally{setLoading(false);}
  };
  useEffect(()=>{load();},[user]);

  const cambiar=async(id:number,e:string)=>{
    try{await pedidoService.cambiarEstado(id,e);toast.success(`#${id} → ${e.replace('_',' ')}`);load();}catch(err:any){toast.error(err.response?.data?.message||'Error');}
  };

  const cancelar=async(id:number)=>{
    if(!confirm('¿Cancelar este pedido? Se restaurará el stock.'))return;
    try{await pedidoService.cambiarEstado(id,'CANCELADO');toast.success('Pedido cancelado');load();}catch(err:any){toast.error(err.response?.data?.message||'Error');}
  };

  const abrirEditar=async(p:any)=>{
    try{
      const r=await pedidoService.obtener(p.id);
      setEditPedido(r.data.data||r.data);
    }catch{
      setEditPedido(p);
    }
    if(productos.length===0){
      try{const r=await productService.disponibles();setProductos(r.data.data||[]);}catch{}
    }
  };

  const agregarItem=async(prodId:number)=>{
    if(!editPedido)return;
    const prod=productos.find(p=>p.id===prodId);
    try{
      await pedidoService.agregarItem(editPedido.id,{productoId:prodId,cantidad:1,notas:''});
      setEditPedido((prev:any)=>({
        ...prev,
        detalles:[...prev.detalles,{id:Date.now(),producto:prod,cantidad:1,precioUnitario:prod?.precio||0,subtotal:prod?.precio||0}]
      }));
      toast.success('Item agregado');
      load();
    }catch(err:any){toast.error(err.response?.data?.message||'Error');}
  };

  const eliminarItem=async(itemId:number)=>{
    if(!editPedido)return;
    try{
      await pedidoService.eliminarItem(editPedido.id,itemId);
      setEditPedido((prev:any)=>({
        ...prev,
        detalles: prev.detalles.filter((d:any)=>d.id!==itemId)
      }));
      toast.success('Item eliminado');
      load();
    }catch(err:any){toast.error(err.response?.data?.message||'Error');}
  };

  const filtered=filtro?pedidos.filter((p:any)=>p.estado===filtro):pedidos;
  const prodsFiltrados=buscar?productos.filter(p=>p.nombre.toLowerCase().includes(buscar.toLowerCase())):productos.slice(0,12);

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mis Pedidos</h1></div>
      <div className="flex gap-2 overflow-x-auto scrollbar-hide">
        {['','PENDIENTE','EN_PREPARACION','SERVIDO','CANCELADO'].map(e=>(
          <button key={e} onClick={()=>setFiltro(e)} className={`btn-sm whitespace-nowrap ${filtro===e?'btn-gold':'btn-outline'}`}>{e?e.replace('_',' '):'Todos'}</button>
        ))}
      </div>

      {filtered.length===0?<EmptyState title="Sin pedidos" icon={ClipboardList}/>:(
        <div className="space-y-4">{filtered.map((p:any)=>(
          <div key={p.id} className={`card overflow-hidden ${p.estado==='CANCELADO'?'opacity-60':''}`}>
            <div className={`px-4 py-3 flex items-center justify-between flex-wrap gap-2 ${
              p.estado==='PENDIENTE'?'bg-amber-50 dark:bg-amber-900/10 border-b border-amber-100 dark:border-amber-900/20':
              p.estado==='EN_PREPARACION'?'bg-blue-50 dark:bg-blue-900/10 border-b border-blue-100 dark:border-blue-900/20':
              p.estado==='SERVIDO'?'bg-green-50 dark:bg-green-900/10 border-b border-green-100 dark:border-green-900/20':
              'bg-gray-50 dark:bg-gray-800/50 border-b border-gray-100 dark:border-gray-800'
            }`}>
              <div className="flex items-center gap-3">
                <span className="font-mono font-bold text-lg">#{p.id}</span>
                <span className="text-sm text-gray-500">Mesa {p.mesa?.numeroMesa||'—'}</span>
                <StatusBadge status={p.estado}/>
              </div>
              <div className="flex gap-2 flex-wrap">
                {(p.estado==='PENDIENTE'||p.estado==='EN_PREPARACION')&&(
                  <>
                    <button onClick={()=>abrirEditar(p)} className="btn-sm btn-outline">Editar</button>
                    {p.estado==='PENDIENTE'&&<button onClick={()=>cambiar(p.id,'EN_PREPARACION')} className="btn-sm btn-outline text-blue-600">Preparar</button>}
                    {p.estado==='EN_PREPARACION'&&<button onClick={()=>cambiar(p.id,'SERVIDO')} className="btn-sm btn-outline text-green-600">Servir</button>}
                    <button onClick={()=>cancelar(p.id)} className="btn-sm btn-outline text-red-500">Cancelar</button>
                  </>
                )}
              </div>
            </div>

            <div className="p-4">
              <div className="space-y-2">
                {p.detalles&&p.detalles.length>0?p.detalles.map((d:any,i:number)=>(
                  <div key={i} className="flex items-center justify-between">
                    <div className="flex items-center gap-2">
                      <span className="w-6 h-6 bg-brand-50 dark:bg-brand-900/20 text-brand-500 rounded-md flex items-center justify-center text-xs font-bold">{d.cantidad}</span>
                      <span className="text-sm text-gray-700 dark:text-gray-300">{d.producto?.nombre}</span>
                    </div>
                    <span className="text-sm font-medium text-gray-900 dark:text-white">{money(d.subtotal||(d.precioUnitario||0)*d.cantidad)}</span>
                  </div>
                )):<p className="text-sm text-gray-400">Sin detalles</p>}
              </div>
              {p.notas&&<p className="text-xs text-amber-600 dark:text-amber-400 italic mt-3 pt-2 border-t border-gray-100 dark:border-gray-800">📝 {p.notas}</p>}
              <div className="flex items-center justify-between mt-3 pt-3 border-t border-gray-100 dark:border-gray-800">
                <span className="text-xs text-gray-400">{p.cliente?`${p.cliente.nombre} ${p.cliente.apellido}`:''} · {dateTime(p.fechaCreacion)}</span>
                <span className="text-lg font-bold text-brand-500">{money(p.total)}</span>
              </div>
            </div>
          </div>
        ))}</div>
      )}

      <Modal open={!!editPedido} onClose={()=>{setEditPedido(null);setBuscar('');}} title={`Editar Pedido #${editPedido?.id}`}>
        {editPedido&&(
          <div className="space-y-4">
            <div>
              <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Items actuales</h4>
              {editPedido.detalles?.map((d:any)=>(
                <div key={d.id} className="flex items-center justify-between py-2 border-b border-gray-50 dark:border-gray-800">
                  <span className="text-sm text-gray-700 dark:text-gray-300">{d.cantidad}× {d.producto?.nombre} — {money(d.subtotal)}</span>
                  <button onClick={()=>eliminarItem(d.id)} className="p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded text-red-400 hover:text-red-600"><Trash2 className="w-4 h-4"/></button>
                </div>
              ))}
              {(!editPedido.detalles||editPedido.detalles.length===0)&&<p className="text-sm text-gray-400">Sin items</p>}
              <p className="text-right font-bold text-brand-500 mt-2">Total: {money(editPedido.total)}</p>
            </div>

            <div className="border-t dark:border-gray-800 pt-4">
              <h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Agregar producto</h4>
              <div className="relative mb-3"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"/><input className="input pl-9 text-sm" placeholder="Buscar..." value={buscar} onChange={e=>setBuscar(e.target.value)}/></div>
              <div className="max-h-48 overflow-y-auto space-y-1">
                {prodsFiltrados.map((pr:any)=>(
                  <button key={pr.id} onClick={()=>agregarItem(pr.id)} className="w-full flex items-center justify-between p-2 rounded-lg hover:bg-gray-50 dark:hover:bg-gray-800 text-sm text-left">
                    <span className="text-gray-700 dark:text-gray-300">{pr.nombre}</span>
                    <span className="flex items-center gap-2"><span className="text-brand-500 font-medium">{money(pr.precio)}</span><Plus className="w-4 h-4 text-gray-400"/></span>
                  </button>
                ))}
              </div>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}