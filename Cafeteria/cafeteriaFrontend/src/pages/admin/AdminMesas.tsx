import React, { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, Armchair } from 'lucide-react';
import { mesaService } from '../../services/mesaService';
import { Mesa } from '../../types';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import StatusBadge from '../../components/ui/StatusBadge';
import toast from 'react-hot-toast';

export default function AdminMesas() {
  const [mesas,setMesas]=useState<Mesa[]>([]);
  const [loading,setLoading]=useState(true);
  const [modal,setModal]=useState(false);
  const [editing,setEditing]=useState<Mesa|null>(null);
  const [f,setF]=useState({numeroMesa:0,capacidad:4,ubicacion:'',activo:true});

  const load=()=>mesaService.listar().then(r=>setMesas(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[]);

  const openNew=()=>{setEditing(null);setF({numeroMesa:0,capacidad:4,ubicacion:'',activo:true});setModal(true);};
  const openEdit=(m:Mesa)=>{setEditing(m);setF({numeroMesa:m.numeroMesa,capacidad:m.capacidad,ubicacion:m.ubicacion,activo:m.activo});setModal(true);};

  const save=async()=>{
    try{if(editing)await mesaService.actualizar(editing.id,f);else await mesaService.crear(f);
    toast.success(editing?'Actualizada':'Creada');setModal(false);load();}catch(e:any){toast.error(e.response?.data?.message||'Error');}
  };

  if(loading)return<LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div className="flex items-center justify-between"><div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mesas</h1><p className="text-gray-500 text-sm mt-1">{mesas.length} mesas</p></div>
      <button onClick={openNew} className="btn-gold"><Plus className="w-4 h-4"/>Nueva Mesa</button></div>
      <div className="grid sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">{mesas.map(m=>(
        <div key={m.id} className="card p-5 hover:shadow-md transition-shadow">
          <div className="flex items-center gap-3 mb-3">
            <div className="w-12 h-12 bg-navy-50 rounded-xl flex items-center justify-center"><Armchair className="w-6 h-6 text-navy-500"/></div>
            <div><p className="font-bold text-gray-900 text-lg">Mesa {m.numeroMesa}</p><p className="text-xs text-gray-500">{m.ubicacion}</p></div>
          </div>
          <div className="flex items-center justify-between mb-3"><span className="text-sm text-gray-500">{m.capacidad} personas</span><StatusBadge status={m.estado||'DISPONIBLE'}/></div>
          <button onClick={()=>openEdit(m)} className="btn-outline btn-sm w-full"><Edit2 className="w-3.5 h-3.5"/>Editar</button>
        </div>
      ))}</div>
      <Modal open={modal} onClose={()=>setModal(false)} title={editing?'Editar Mesa':'Nueva Mesa'}>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3">
            <div><label className="label">Número</label><input className="input" type="number" value={f.numeroMesa} onChange={e=>setF(p=>({...p,numeroMesa:+e.target.value}))}/></div>
            <div><label className="label">Capacidad</label><input className="input" type="number" value={f.capacidad} onChange={e=>setF(p=>({...p,capacidad:+e.target.value}))}/></div>
          </div>
          <div><label className="label">Ubicación</label><input className="input" value={f.ubicacion} onChange={e=>setF(p=>({...p,ubicacion:e.target.value}))}/></div>
          <label className="flex items-center gap-2"><input type="checkbox" className="w-4 h-4 rounded border-gray-300 text-brand-500" checked={f.activo} onChange={e=>setF(p=>({...p,activo:e.target.checked}))}/><span className="text-sm">Activa</span></label>
          <div className="flex gap-3 pt-2"><button onClick={()=>setModal(false)} className="btn-outline flex-1">Cancelar</button><button onClick={save} className="btn-gold flex-1">Guardar</button></div>
        </div>
      </Modal>
    </div>
  );
}
