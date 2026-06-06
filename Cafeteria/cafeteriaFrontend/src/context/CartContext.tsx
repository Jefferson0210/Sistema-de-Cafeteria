import React, { createContext, useContext, useState, ReactNode } from 'react';
import { CartItem, Producto } from '../types';

interface CartCtx {
  items:CartItem[]; addItem:(p:Producto,qty?:number,notas?:string)=>void;
  removeItem:(id:number)=>void; updateQty:(id:number,q:number)=>void;
  clearCart:()=>void; subtotal:number; iva:number; total:number; count:number;
  isOpen:boolean; setIsOpen:(v:boolean)=>void;
}
const Ctx = createContext<CartCtx>({} as CartCtx);
export const useCart = () => useContext(Ctx);

export function CartProvider({children}:{children:ReactNode}) {
  const [items,setItems] = useState<CartItem[]>([]);
  const [isOpen,setIsOpen] = useState(false);

  const addItem = (p:Producto, qty=1, notas='') => {
    setItems(prev => {
      const ex = prev.find(i=>i.producto.id===p.id);
      if(ex) return prev.map(i=>i.producto.id===p.id?{...i,cantidad:i.cantidad+qty}:i);
      return [...prev,{producto:p,cantidad:qty,notas}];
    });
  };
  const removeItem = (id:number) => setItems(p=>p.filter(i=>i.producto.id!==id));
  const updateQty = (id:number,q:number) => { if(q<=0) return removeItem(id); setItems(p=>p.map(i=>i.producto.id===id?{...i,cantidad:q}:i)); };
  const clearCart = () => setItems([]);
  const subtotal = items.reduce((s,i)=>s+i.producto.precio*i.cantidad,0);
  const iva = subtotal*0.15;
  const total = subtotal+iva;
  const count = items.reduce((s,i)=>s+i.cantidad,0);

  return <Ctx.Provider value={{items,addItem,removeItem,updateQty,clearCart,subtotal,iva,total,count,isOpen,setIsOpen}}>{children}</Ctx.Provider>;
}
