import React, { useState, useEffect } from 'react';
import { Search, Plus } from 'lucide-react';
import { productService } from '../../services/productService';
import { categoryService } from '../../services/categoryService';
import { Producto, Category } from '../../types';
import { useCart } from '../../context/CartContext';
import { money } from '../../utils/format';
import PublicNavbar from '../../components/layout/PublicNavbar';
import Footer from '../../components/layout/Footer';


import toast from 'react-hot-toast';

export default function PublicMenuPage() {
  const [productos,setProductos]=useState<Producto[]>([]);
  const [categorias,setCategorias]=useState<Category[]>([]);
  const [loading,setLoading]=useState(true);
  const [activeCat,setActiveCat]=useState<number|null>(null);
  const [search,setSearch]=useState('');
  const { addItem } = useCart();

  useEffect(()=>{
    Promise.all([productService.disponibles(),categoryService.activas()])
    .then(([p,c])=>{setProductos(p.data.data||[]);setCategorias(c.data.data||[]);})
    .catch(()=>{}).finally(()=>setLoading(false));
  },[]);

  const filtered=productos.filter(p=>{
    if(activeCat&&p.category?.categoryId!==activeCat) return false;
    if(search&&!p.nombre.toLowerCase().includes(search.toLowerCase())) return false;
    return true;
  });

  const handleAdd=(p:Producto)=>{ addItem(p); toast.success(`${p.nombre} agregado`,{icon:'🛒',style:{borderRadius:'12px',background:'#1e293b',color:'#fff'}}); };

  const catEmoji:Record<string,string>={desayuno:'🌅',almuerzo:'🍛',snack:'🥟',bebida:'🧃',postre:'🍰',especialidad:'⭐'};
  const getEmoji=(n:string)=>{const l=n.toLowerCase();for(const[k,v]of Object.entries(catEmoji))if(l.includes(k))return v;return'🍽️';};

  return(
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      <PublicNavbar/>
      
      

      {/* Hero */}
      <div className="relative bg-gradient-to-br from-brand-500 via-brand-600 to-navy-500 pt-32 pb-20 overflow-hidden">
        <div className="absolute inset-0 opacity-20"><div className="absolute top-10 left-20 w-80 h-80 bg-gold-400 rounded-full blur-3xl"/><div className="absolute bottom-0 right-10 w-96 h-96 bg-white rounded-full blur-3xl"/></div>
        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 text-center">
          <p className="text-gold-400 text-sm font-semibold uppercase tracking-[0.25em] mb-3">Universidad Internacional del Ecuador</p>
          <h1 className="font-display text-4xl sm:text-5xl lg:text-6xl font-extrabold text-white leading-tight mb-5">Sabores que inspiran<br/><span className="text-gold-400">tu día</span></h1>
          <p className="text-white/70 text-lg max-w-xl mx-auto mb-8">Platos ecuatorianos auténticos preparados con ingredientes frescos</p>
          <div className="relative max-w-lg mx-auto">
            <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-gray-400"/>
            <input className="w-full bg-white rounded-2xl pl-12 pr-4 py-4 text-gray-900 placeholder:text-gray-400 focus:outline-none focus:ring-2 focus:ring-gold-400 shadow-xl text-lg" placeholder="¿Qué se te antoja hoy?" value={search} onChange={e=>setSearch(e.target.value)}/>
          </div>
        </div>
      </div>

      {/* Categories */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 -mt-6 relative z-10">
        <div className="flex gap-2 overflow-x-auto pb-2 scrollbar-hide">
          <button onClick={()=>setActiveCat(null)} className={`flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-medium whitespace-nowrap transition-all shadow-sm ${!activeCat?'bg-brand-500 text-white shadow-brand-500/30':'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'}`}>🍽️ Todos</button>
          {categorias.map(c=>(
            <button key={c.categoryId} onClick={()=>setActiveCat(c.categoryId)} className={`flex items-center gap-2 px-5 py-2.5 rounded-full text-sm font-medium whitespace-nowrap transition-all shadow-sm ${activeCat===c.categoryId?'bg-brand-500 text-white shadow-brand-500/30':'bg-white text-gray-600 hover:bg-gray-50 border border-gray-200'}`}>
              {getEmoji(c.name)} {c.name.replace(/_[a-f0-9]+$/i,'')}
            </button>
          ))}
        </div>
      </div>

      {/* Products */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 py-10">
        {loading?(
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">{[...Array(8)].map((_,i)=><div key={i} className="card animate-pulse"><div className="h-44 bg-gray-100"/><div className="p-4 space-y-3"><div className="h-3 bg-gray-100 rounded w-1/3"/><div className="h-4 bg-gray-100 rounded w-3/4"/></div></div>)}</div>
        ):filtered.length===0?(
          <div className="text-center py-20"><p className="text-gray-400 text-xl font-display">No se encontraron productos</p></div>
        ):(
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-5">{filtered.map(p=>(
            <div key={p.id} className="card group hover:shadow-lg transition-all duration-300 hover:-translate-y-1 flex flex-col">
              <div className="relative h-44 bg-gradient-to-br from-gray-50 to-gray-100 dark:from-gray-800 dark:to-gray-900 flex items-center justify-center overflow-hidden">
                {p.imagenUrl ? (
                  <img src={p.imagenUrl} alt={p.nombre} className="w-full h-full object-cover group-hover:scale-110 transition-transform duration-500"/>
                ) : (
                  <span className="text-5xl group-hover:scale-125 transition-transform duration-500">{getEmoji(p.category?.name||'')}</span>
                )}
                {p.stock<=5&&p.stock>0&&<span className="absolute top-2.5 left-2.5 badge bg-amber-100 text-amber-700 text-[10px]">Últimas {p.stock}</span>}
                {p.stock<=0&&<span className="absolute top-2.5 left-2.5 badge bg-red-100 text-red-700 text-[10px]">Agotado</span>}
              </div>
              <div className="p-4 flex flex-col flex-1">
                <p className="text-[10px] uppercase tracking-widest text-brand-500 font-semibold mb-1">{(p.category?.name||'').replace(/_[a-f0-9]+$/i,'')}</p>
                <h3 className="font-semibold text-gray-900 dark:text-white text-sm leading-tight mb-1 line-clamp-2">{p.nombre}</h3>
                <p className="text-gray-400 text-xs line-clamp-2 mb-3">{p.descripcion}</p>
                <div className="flex flex-col gap-2 mt-auto">
                  <span className="text-xl font-bold text-gray-900 dark:text-white">{money(p.precio)}</span>
                  <button onClick={()=>handleAdd(p)} disabled={p.stock<=0} className="w-full flex items-center justify-center gap-1.5 bg-gold-400 hover:bg-gold-500 disabled:bg-gray-200 disabled:text-gray-400 text-gray-900 text-xs font-semibold py-2.5 rounded-xl transition-all active:scale-95">
                    <Plus className="w-3.5 h-3.5"/>Agregar
                  </button>
                </div>
              </div>
            </div>
          ))}</div>
        )}
      </div>

      <Footer/>
    </div>
  );
}