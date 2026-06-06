import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { MapPin, ClipboardList, CheckCircle, Send } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { mesaService } from '../../services/mesaService';
import { pedidoService } from '../../services/pedidoService';
import { Mesa } from '../../types';
import { money } from '../../utils/format';
import toast from 'react-hot-toast';

export default function CheckoutPage() {
  const { items,subtotal,iva,total,clearCart } = useCart();
  const { user } = useAuth();
  const navigate = useNavigate();
  const [mesas,setMesas]=useState<Mesa[]>([]);
  const [mesaId,setMesaId]=useState<number|null>(null);
  const [notas,setNotas]=useState('');
  const [loading,setLoading]=useState(false);
  const [success,setSuccess]=useState(false);

  useEffect(()=>{
    if(!user){navigate('/');return;}
    if(items.length===0&&!success){navigate('/cliente/menu');return;}
    mesaService.disponibles().then(r=>setMesas(r.data.data||[])).catch(()=>{});
  },[]);

  const handleOrder=async()=>{
    if(!mesaId){toast.error('Selecciona una mesa');return;}
    setLoading(true);
    try{
      await pedidoService.crear({mesaId,clienteId:user!.id,notas,items:items.map(i=>({productoId:i.producto.id,cantidad:i.cantidad,notas:i.notas||''}))});
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
          <div className="card p-6"><h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-4"><MapPin className="w-5 h-5 text-brand-500"/>Selecciona Mesa</h3>
            <div className="grid grid-cols-2 sm:grid-cols-3 gap-3">{mesas.map(m=>(
              <button key={m.id} onClick={()=>setMesaId(m.id)} className={`p-3 rounded-xl border text-left transition-all ${mesaId===m.id?'border-brand-500 bg-brand-50 dark:bg-brand-900/20 ring-1 ring-brand-500':'border-gray-200 dark:border-gray-700 hover:border-gray-300'}`}>
                <p className="font-bold text-gray-900 dark:text-white">Mesa {m.numeroMesa}</p><p className="text-xs text-gray-500 mt-0.5">{m.ubicacion} · {m.capacidad}p</p>
              </button>
            ))}</div>
            {mesas.length===0&&<p className="text-gray-400 text-sm text-center py-4">No hay mesas disponibles</p>}
          </div>
          <div className="card p-6"><h3 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2 mb-3"><ClipboardList className="w-5 h-5 text-brand-500"/>Notas</h3><textarea className="input resize-none h-20" placeholder="Alergias, preferencias..." value={notas} onChange={e=>setNotas(e.target.value)}/></div>
        </div>
        <div className="card p-6 sticky top-24 h-fit">
          <h3 className="font-semibold text-gray-900 dark:text-white mb-4">Resumen</h3>
          <div className="space-y-2 mb-4">{items.map(i=><div key={i.producto.id} className="flex justify-between text-sm"><span className="text-gray-600 dark:text-gray-400">{i.cantidad}× {i.producto.nombre}</span><span className="font-medium text-gray-900 dark:text-white">{money(i.producto.precio*i.cantidad)}</span></div>)}</div>
          <div className="border-t dark:border-gray-800 pt-3 space-y-1.5">
            <div className="flex justify-between text-sm text-gray-500"><span>Subtotal</span><span>{money(subtotal)}</span></div>
            <div className="flex justify-between text-sm text-gray-500"><span>IVA 15%</span><span>{money(iva)}</span></div>
            <div className="flex justify-between font-bold text-lg pt-1"><span className="text-gray-900 dark:text-white">Total</span><span className="text-brand-500">{money(total)}</span></div>
          </div>
          <button onClick={handleOrder} disabled={loading||!mesaId} className="btn-gold w-full mt-4"><Send className="w-4 h-4"/>{loading?'Procesando...':'Confirmar Pedido'}</button>
        </div>
      </div>
    </div>
  );
}
