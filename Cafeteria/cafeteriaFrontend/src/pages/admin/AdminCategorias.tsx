import React, { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, Tags } from 'lucide-react';
import { categoryService } from '../../services/categoryService';
import { Category } from '../../types';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import toast from 'react-hot-toast';

export default function AdminCategorias() {
  const [cats,setCats]=useState<Category[]>([]);
  const [loading,setLoading]=useState(true);
  const [modal,setModal]=useState(false);
  const [editing,setEditing]=useState<Category|null>(null);
  const [f,setF]=useState({name:'',descripcion:'',activo:true});

  const load=()=>categoryService.listar().then(r=>setCats(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[]);

  const openNew=()=>{setEditing(null);setF({name:'',descripcion:'',activo:true});setModal(true);};
  const openEdit=(c:Category)=>{setEditing(c);setF({name:c.name,descripcion:c.descripcion,activo:c.activo});setModal(true);};

  const save=async()=>{
    try{if(editing)await categoryService.actualizar(editing.categoryId,f);else await categoryService.crear(f);
    toast.success(editing?'Actualizada':'Creada');setModal(false);load();}catch(e:any){toast.error(e.response?.data?.message||'Error');}
  };

  const remove=async(id:number)=>{try{await categoryService.eliminar(id);toast.success('Eliminada');load();}catch{toast.error('Error');}};

  if(loading)return<LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div className="flex items-center justify-between"><div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Categorías</h1><p className="text-gray-500 text-sm mt-1">{cats.length} categorías</p></div>
      <button onClick={openNew} className="btn-gold"><Plus className="w-4 h-4"/>Nueva</button></div>
      {cats.length===0?<EmptyState title="Sin categorías" icon={Tags}/>:(
        <div className="grid sm:grid-cols-2 lg:grid-cols-3 gap-4">{cats.map(c=>(
          <div key={c.categoryId} className="card p-5 hover:shadow-md transition-shadow">
            <div className="flex items-start justify-between mb-2">
              <h3 className="font-semibold text-gray-900 dark:text-white">{c.name}</h3>
              <span className={`badge ${c.activo?'bg-green-100 text-green-700':'bg-gray-100 dark:bg-gray-800 text-gray-500'}`}>{c.activo?'Activa':'Inactiva'}</span>
            </div>
            <p className="text-sm text-gray-500 mb-4">{c.descripcion||'Sin descripción'}</p>
            <div className="flex gap-2"><button onClick={()=>openEdit(c)} className="btn-outline btn-sm flex-1"><Edit2 className="w-3.5 h-3.5"/>Editar</button><button onClick={()=>remove(c.categoryId)} className="btn-ghost btn-sm text-red-500"><Trash2 className="w-3.5 h-3.5"/></button></div>
          </div>
        ))}</div>
      )}
      <Modal open={modal} onClose={()=>setModal(false)} title={editing?'Editar Categoría':'Nueva Categoría'}>
        <div className="space-y-4">
          <div><label className="label">Nombre</label><input className="input" value={f.name} onChange={e=>setF(p=>({...p,name:e.target.value}))}/></div>
          <div><label className="label">Descripción</label><textarea className="input resize-none h-20" value={f.descripcion} onChange={e=>setF(p=>({...p,descripcion:e.target.value}))}/></div>
          <label className="flex items-center gap-2"><input type="checkbox" className="w-4 h-4 rounded border-gray-300 text-brand-500" checked={f.activo} onChange={e=>setF(p=>({...p,activo:e.target.checked}))}/><span className="text-sm">Activa</span></label>
          <div className="flex gap-3 pt-2"><button onClick={()=>setModal(false)} className="btn-outline flex-1">Cancelar</button><button onClick={save} className="btn-gold flex-1">Guardar</button></div>
        </div>
      </Modal>
    </div>
  );
}
