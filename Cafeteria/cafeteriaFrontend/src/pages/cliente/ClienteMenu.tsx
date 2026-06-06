import React, { useState, useEffect } from 'react';
import { Search, Plus, Heart, X } from 'lucide-react';
import { productService } from '../../services/productService';
import { categoryService } from '../../services/categoryService';
import { favoritoService } from '../../services/favoritoService';
import { useCart } from '../../context/CartContext';
import { useAuth } from '../../context/AuthContext';
import { Producto, Category } from '../../types';
import { money } from '../../utils/format';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function ClienteMenu() {
  const { user } = useAuth();
  const { addItem } = useCart();
  const [prods,setProds]=useState<Producto[]>([]);
  const [cats,setCats]=useState<Category[]>([]);
  const [loading,setLoading]=useState(true);
  const [cat,setCat]=useState<number|null>(null);
  const [q,setQ]=useState('');
  const [detail,setDetail]=useState<Producto|null>(null);

  useEffect(()=>{Promise.all([productService.disponibles(),categoryService.activas()]).then(([p,c])=>{setProds(p.data.data||[]);setCats(c.data.data||[]);}).catch(()=>{}).finally(()=>setLoading(false));},[]);

  const filtered=prods.filter(p=>{if(cat&&p.category?.categoryId!==cat)return false;if(q&&!p.nombre.toLowerCase().includes(q.toLowerCase()))return false;return true;});
  const handleAdd=(p:Producto)=>{addItem(p);toast.success(`${p.nombre} agregado`,{icon:'🛒'});};
  const handleFav=async(pid:number)=>{if(!user)return;try{await favoritoService.agregar(user.id,pid);toast.success('Agregado a favoritos',{icon:'❤️'});}catch{toast.error('Ya está en favoritos');}};

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Menú</h1><p className="text-gray-500 text-sm mt-1">Explora nuestros platos</p></div>
      <div className="relative"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"/><input className="input pl-9" placeholder="Buscar..." value={q} onChange={e=>setQ(e.target.value)}/></div>
      {/* Horizontal scroll categories */}
      <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
        <button onClick={()=>setCat(null)} className={`btn-sm whitespace-nowrap ${!cat?'btn-gold':'btn-outline'}`}>Todos</button>
        {cats.map(c=><button key={c.categoryId} onClick={()=>setCat(c.categoryId)} className={`btn-sm whitespace-nowrap ${cat===c.categoryId?'btn-gold':'btn-outline'}`}>{c.name.replace(/_[a-f0-9]+$/i,'')}</button>)}
      </div>
      <div className="grid grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">{filtered.map(p=>(
        <div key={p.id} className="card group hover:shadow-md transition-all flex flex-col">
          <div className="h-36 bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-900 flex items-center justify-center overflow-hidden cursor-pointer" onClick={()=>setDetail(p)}>
            {p.imagenUrl?<img src={p.imagenUrl} alt={p.nombre} className="w-full h-full object-cover group-hover:scale-110 transition-transform"/>:<span className="text-4xl">🍽️</span>}
          </div>
          <div className="p-4 flex flex-col flex-1 cursor-pointer" onClick={()=>setDetail(p)}>
            <p className="text-[10px] uppercase tracking-widest text-brand-500 font-semibold">{(p.category?.name||'').replace(/_[a-f0-9]+$/i,'')}</p>
            <h3 className="font-medium text-gray-900 dark:text-white text-sm mt-1 line-clamp-1">{p.nombre}</h3>
            <p className="text-xs text-gray-400 line-clamp-2 mt-1 mb-3">{p.descripcion}</p>
          </div>
          <div className="px-4 pb-4 flex flex-col gap-2 mt-auto">
            <div className="flex items-center justify-between">
              <span className="text-lg font-bold text-gray-900 dark:text-white">{money(p.precio)}</span>
              <button onClick={e=>{e.stopPropagation();handleFav(p.id);}} className="p-1.5 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg text-gray-300 hover:text-red-500"><Heart className="w-4 h-4"/></button>
            </div>
            <button onClick={e=>{e.stopPropagation();handleAdd(p);}} disabled={p.stock<=0} className="w-full flex items-center justify-center gap-1.5 bg-gold-400 hover:bg-gold-500 disabled:bg-gray-200 disabled:text-gray-400 text-gray-900 text-xs font-semibold py-2.5 rounded-xl transition-all active:scale-95">
              <Plus className="w-3.5 h-3.5"/>{p.stock<=0?'Agotado':'Agregar'}
            </button>
          </div>
        </div>
      ))}</div>

      {/* Product detail modal */}
      <Modal open={!!detail} onClose={()=>setDetail(null)} title={detail?.nombre||''}>
        {detail&&(
          <div className="space-y-4">
            {detail.imagenUrl&&<img src={detail.imagenUrl} alt={detail.nombre} className="w-full h-48 object-cover rounded-xl"/>}
            <p className="text-[11px] uppercase tracking-widest text-brand-500 font-semibold">{(detail.category?.name||'').replace(/_[a-f0-9]+$/i,'')}</p>
            <p className="text-sm text-gray-600 dark:text-gray-300 leading-relaxed">{detail.descripcion}</p>
            <div className="flex items-center justify-between pt-2 border-t border-gray-100 dark:border-gray-800">
              <span className="text-2xl font-bold text-gray-900 dark:text-white">{money(detail.precio)}</span>
              <span className="text-sm text-gray-400">Stock: {detail.stock}</span>
            </div>
            <button onClick={()=>{handleAdd(detail);setDetail(null);}} disabled={detail.stock<=0} className="btn-gold w-full !py-3">
              <Plus className="w-4 h-4"/>{detail.stock<=0?'Agotado':'Agregar al carrito'}
            </button>
          </div>
        )}
      </Modal>
    </div>
  );
}
