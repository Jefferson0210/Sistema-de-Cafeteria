import React from 'react';
import { X, Plus, Minus, Trash2, ShoppingBag } from 'lucide-react';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { money } from '../../utils/format';

export default function CartDrawer() {
  const { items, isOpen, setIsOpen, removeItem, updateQty, subtotal, iva, total, clearCart } = useCart();
  const { user, setShowAuth } = useAuth();
  const navigate = useNavigate();

  if(!isOpen) return null;

  const handleCheckout = () => {
    if(!user) { setIsOpen(false); setShowAuth(true); return; }
    setIsOpen(false); navigate('/cliente/checkout');
  };

  return (
    <div className="fixed inset-0 z-[90]" onClick={()=>setIsOpen(false)}>
      <div className="absolute inset-0 bg-black/30 backdrop-blur-sm"/>
      <div className="absolute right-0 top-0 bottom-0 w-full max-w-md bg-white dark:bg-gray-900 flex flex-col shadow-2xl border-l border-gray-100 dark:border-gray-800" onClick={e=>e.stopPropagation()}>
        <div className="flex items-center justify-between px-5 h-16 border-b border-gray-100 dark:border-gray-800">
          <h2 className="font-semibold text-gray-900 dark:text-white flex items-center gap-2"><ShoppingBag className="w-5 h-5 text-brand-500"/>Tu Pedido ({items.length})</h2>
          <button onClick={()=>setIsOpen(false)} className="p-1.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg"><X className="w-5 h-5 text-gray-400"/></button>
        </div>

        <div className="flex-1 overflow-y-auto p-5 space-y-3">
          {items.length===0 ? (
            <div className="text-center py-20"><ShoppingBag className="w-16 h-16 text-gray-200 dark:text-gray-700 mx-auto mb-4"/><p className="text-gray-400 text-lg font-medium">Carrito vacío</p></div>
          ) : items.map(item=>(
            <div key={item.producto.id} className="flex gap-3 p-3 bg-gray-50 dark:bg-gray-800 rounded-xl">
              <div className="w-14 h-14 bg-brand-50 dark:bg-brand-900/30 rounded-xl flex items-center justify-center flex-shrink-0"><span className="text-2xl">🍽️</span></div>
              <div className="flex-1 min-w-0">
                <h4 className="text-sm font-medium text-gray-900 dark:text-white truncate">{item.producto.nombre}</h4>
                <p className="text-brand-500 font-bold text-sm">{money(item.producto.precio)}</p>
                <div className="flex items-center gap-2 mt-1.5">
                  <button onClick={()=>updateQty(item.producto.id,item.cantidad-1)} className="w-6 h-6 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-md flex items-center justify-center hover:bg-gray-50 dark:hover:bg-gray-600"><Minus className="w-3 h-3"/></button>
                  <span className="text-xs font-bold w-5 text-center">{item.cantidad}</span>
                  <button onClick={()=>updateQty(item.producto.id,item.cantidad+1)} className="w-6 h-6 bg-white dark:bg-gray-700 border border-gray-200 dark:border-gray-600 rounded-md flex items-center justify-center hover:bg-gray-50 dark:hover:bg-gray-600"><Plus className="w-3 h-3"/></button>
                  <button onClick={()=>removeItem(item.producto.id)} className="ml-auto p-1 hover:bg-red-50 dark:hover:bg-red-900/20 rounded text-gray-300 hover:text-red-500"><Trash2 className="w-3.5 h-3.5"/></button>
                </div>
              </div>
            </div>
          ))}
        </div>

        {items.length>0 && (
          <div className="border-t border-gray-100 dark:border-gray-800 p-5 space-y-2.5">
            <div className="flex justify-between text-sm text-gray-500"><span>Subtotal</span><span>{money(subtotal)}</span></div>
            <div className="flex justify-between text-sm text-gray-500"><span>IVA (15%)</span><span>{money(iva)}</span></div>
            <div className="flex justify-between text-lg font-bold text-gray-900 dark:text-white"><span>Total</span><span className="text-brand-500">{money(total)}</span></div>
            <button onClick={handleCheckout} className="btn-gold w-full !py-3 mt-2">{user?'Realizar Pedido':'Inicia sesión para pedir'}</button>
            <button onClick={clearCart} className="w-full text-center text-xs text-gray-400 hover:text-gray-600 mt-1">Vaciar carrito</button>
          </div>
        )}
      </div>
    </div>
  );
}
