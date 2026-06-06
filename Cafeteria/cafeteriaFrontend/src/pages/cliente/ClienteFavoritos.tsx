import React, { useState, useEffect } from 'react';
import { Heart, Trash2, Plus } from 'lucide-react';
import { favoritoService } from '../../services/favoritoService';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { money } from '../../utils/format';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import toast from 'react-hot-toast';

export default function ClienteFavoritos() {
  const { user } = useAuth();
  const { addItem } = useCart();
  const [favs,setFavs]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);

  const load=()=>{if(user)favoritoService.listar(user.id).then(r=>setFavs(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));};
  useEffect(()=>{load();},[user]);

// línea 20
const remove = async (favId: number) => {
  if (!user) return;
  try {
    await favoritoService.eliminar(favId);
    toast.success('Eliminado de favoritos');
    load();
  } catch { toast.error('Error'); }
};
  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mis Favoritos</h1></div>
      {favs.length===0?<EmptyState title="Sin favoritos" description="Agrega productos a favoritos desde el menú" icon={Heart}/>:(
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">{favs.map((f:any)=>(
          <div key={f.id} className="card p-4 hover:shadow-md transition-shadow">
            <div className="flex items-center gap-3 mb-3"><div className="w-12 h-12 bg-brand-50 rounded-xl flex items-center justify-center text-2xl">🍽️</div><div className="flex-1 min-w-0"><h3 className="font-medium text-gray-900 text-sm truncate">{f.producto?.nombre}</h3><p className="text-brand-500 font-bold">{money(f.producto?.precio||0)}</p></div></div>
            <div className="flex gap-2"><button onClick={()=>{addItem(f.producto);toast.success('Agregado');}} className="btn-gold btn-sm flex-1"><Plus className="w-3 h-3"/>Agregar</button><button onClick={()=>remove(f.id)} className="btn-ghost btn-sm text-red-500"><Trash2 className="w-3.5 h-3.5"/></button></div>
          </div>
        ))}</div>
      )}
    </div>
  );
}
