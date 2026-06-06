import React, { useState, useEffect } from 'react';
import { Users, Search, Plus, Edit2, UserCheck, UserX, KeyRound } from 'lucide-react';
import { userService } from '../../services/userService';
import Modal from '../../components/ui/Modal';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import { roleColor, roleLabel } from '../../utils/roles';
import toast from 'react-hot-toast';
import api from '../../api/axiosConfig';

export default function AdminUsuarios() {
  const [users,setUsers]=useState<any[]>([]);
  const [loading,setLoading]=useState(true);
  const [filtroRol,setFiltroRol]=useState('');
  const [search,setSearch]=useState('');
  const [modal,setModal]=useState(false);
  const [editing,setEditing]=useState<any>(null);
  const [f,setF]=useState({username:'',email:'',password:'',nombre:'',apellido:'',telefono:'',rol:'CLIENTE'});

  const load=()=>userService.listar().then(r=>setUsers(r.data.data||[])).catch(()=>{}).finally(()=>setLoading(false));
  useEffect(()=>{load();},[]);

  const filtered=users.filter(u=>{
    if(filtroRol&&!(u.roles||[]).includes(filtroRol))return false;
    if(search){const s=search.toLowerCase();if(!`${u.nombre} ${u.apellido} ${u.username} ${u.email}`.toLowerCase().includes(s))return false;}
    return true;
  });

  const openNew=()=>{setEditing(null);setF({username:'',email:'',password:'',nombre:'',apellido:'',telefono:'',rol:'CLIENTE'});setModal(true);};
  const openEdit=(u:any)=>{setEditing(u);setF({username:u.username,email:u.email,password:'',nombre:u.nombre,apellido:u.apellido||'',telefono:u.telefono||'',rol:(u.roles||[])[0]||'CLIENTE'});setModal(true);};

  const save=async()=>{
    try{
      if(editing) await userService.actualizar(editing.id,f);
      else await userService.crear(f);
      toast.success(editing?'Usuario actualizado':'Usuario creado');setModal(false);load();
    }catch(e:any){toast.error(e.response?.data?.message||'Error');}
  };

  const toggleActive=async(u:any)=>{
    try{
      if(u.activo)await userService.desactivar(u.id);else await userService.activar(u.id);
      toast.success(u.activo?'Desactivado':'Activado');load();
    }catch{toast.error('Error');}
  };

  const resetPw=async(u:any)=>{
    const newPw=prompt(`Nueva contraseña para ${u.username} (mín 8 car):`);
    if(!newPw||newPw.length<8){if(newPw)toast.error("Mínimo 8 caracteres");return;}
    try{await api.put(`/usuarios/${u.id}/password`,{passwordNueva:newPw});toast.success(`Contraseña de ${u.username} actualizada`);}catch{toast.error("Error al resetear");}
  };

  if(loading) return <LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Usuarios</h1><p className="text-gray-500 text-sm mt-1">{users.length} usuarios</p></div>
        <button onClick={openNew} className="btn-gold"><Plus className="w-4 h-4"/>Nuevo Usuario</button>
      </div>
      <div className="card p-4 flex flex-wrap gap-3">
        <div className="relative flex-1 min-w-[200px]"><Search className="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"/><input className="input pl-9" placeholder="Buscar..." value={search} onChange={e=>setSearch(e.target.value)}/></div>
        <div className="flex gap-2 flex-wrap">{['','ADMIN','MESERO','CAJERO','CLIENTE'].map(r=>(
          <button key={r} onClick={()=>setFiltroRol(r)} className={`btn-sm ${filtroRol===r?'btn-gold':'btn-outline'}`}>{r||'Todos'}</button>
        ))}</div>
      </div>
      {filtered.length===0?<EmptyState title="Sin usuarios" icon={Users}/>:(
        <div className="table-container"><table className="w-full">
          <thead><tr><th className="th">Nombre</th><th className="th">Username</th><th className="th">Email</th><th className="th">Teléfono</th><th className="th">Roles</th><th className="th">Estado</th><th className="th">Acciones</th></tr></thead>
          <tbody>{filtered.map((u:any)=>(
            <tr key={u.id} className="hover:bg-gray-50/50 dark:hover:bg-gray-800/50">
              <td className="td font-medium text-gray-900 dark:text-white">{u.nombre} {u.apellido}</td>
              <td className="td text-sm font-mono">{u.username}</td>
              <td className="td text-sm text-gray-500">{u.email}</td>
              <td className="td text-sm">{u.telefono||'—'}</td>
              <td className="td"><div className="flex gap-1 flex-wrap">{(u.roles||[]).map((r:string)=><span key={r} className={`badge ${roleColor[r]||''}`}>{r}</span>)}</div></td>
              <td className="td"><span className={`badge ${u.activo?'bg-green-100 text-green-700':'bg-red-100 text-red-700'}`}>{u.activo?'Activo':'Inactivo'}</span></td>
              <td className="td"><div className="flex gap-1">
                <button onClick={()=>openEdit(u)} className="btn-ghost btn-xs"><Edit2 className="w-3.5 h-3.5"/></button>
                <button onClick={()=>resetPw(u)} className="btn-ghost btn-xs text-amber-500" title="Resetear contraseña"><KeyRound className="w-3.5 h-3.5"/></button>
                <button onClick={()=>toggleActive(u)} className={`btn-ghost btn-xs ${u.activo?'text-red-500':'text-green-500'}`}>{u.activo?<UserX className="w-3.5 h-3.5"/>:<UserCheck className="w-3.5 h-3.5"/>}</button>
              </div></td>
            </tr>
          ))}</tbody>
        </table></div>
      )}
      <Modal open={modal} onClose={()=>setModal(false)} title={editing?'Editar Usuario':'Nuevo Usuario'}>
        <div className="space-y-4">
          <div className="grid grid-cols-2 gap-3"><div><label className="label">Nombre</label><input className="input" value={f.nombre} onChange={e=>setF(p=>({...p,nombre:e.target.value}))}/></div><div><label className="label">Apellido</label><input className="input" value={f.apellido} onChange={e=>setF(p=>({...p,apellido:e.target.value}))}/></div></div>
          <div><label className="label">Username</label><input className="input" value={f.username} onChange={e=>setF(p=>({...p,username:e.target.value}))} disabled={!!editing}/></div>
          <div><label className="label">Email</label><input className="input" type="email" value={f.email} onChange={e=>setF(p=>({...p,email:e.target.value}))}/></div>
          {!editing&&<div><label className="label">Contraseña</label><input className="input" type="password" value={f.password} onChange={e=>setF(p=>({...p,password:e.target.value}))}/></div>}
          <div><label className="label">Teléfono</label><input className="input" value={f.telefono} onChange={e=>setF(p=>({...p,telefono:e.target.value}))}/></div>
          <div><label className="label">Rol</label><select className="input" value={f.rol} onChange={e=>setF(p=>({...p,rol:e.target.value}))}><option value="CLIENTE">Cliente</option><option value="MESERO">Mesero</option><option value="CAJERO">Cajero</option><option value="ADMIN">Administrador</option></select></div>
          <div className="flex gap-3 pt-2"><button onClick={()=>setModal(false)} className="btn-outline flex-1">Cancelar</button><button onClick={save} className="btn-gold flex-1">Guardar</button></div>
        </div>
      </Modal>
    </div>
  );
}
