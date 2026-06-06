import React, { useState } from 'react';
import { User, Lock, Save, Camera, Trash2, Mail, Phone, Shield, Palette } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useTheme } from '../../context/ThemeContext';
import { roleLabel, roleColor } from '../../utils/roles';
import api from '../../api/axiosConfig';
import toast from 'react-hot-toast';

export default function PerfilPage() {
  const { user, role } = useAuth();
  const { dark, toggle } = useTheme();
  const [tab,setTab]=useState<'perfil'|'seguridad'|'apariencia'>('perfil');
  const [telefono,setTelefono]=useState(user?.telefono||'');
  const [pw,setPw]=useState({actual:'',nueva:'',confirmar:''});
  const [saving,setSaving]=useState(false);
  const [fotoPreview,setFotoPreview]=useState<string|null>(user?.fotoUrl||null);

  const saveTelefono=async()=>{
    setSaving(true);
    try{
      await api.put(`/usuarios/${user?.id}/perfil`,{nombre:user?.nombre,apellido:user?.apellido,email:user?.email,telefono});
      const stored=localStorage.getItem('uide_user');
      if(stored){const u=JSON.parse(stored);u.telefono=telefono;localStorage.setItem('uide_user',JSON.stringify(u));}
      toast.success('Teléfono actualizado');
    }catch(e:any){toast.error(e.response?.data?.message||'Error');}
    setSaving(false);
  };

  const savePw=async()=>{
    if(pw.nueva!==pw.confirmar){toast.error('Las contraseñas no coinciden');return;}
    if(pw.nueva.length<8){toast.error('Mínimo 8 caracteres');return;}
    setSaving(true);
    try{
      await api.put(`/usuarios/${user?.id}/password`,{passwordActual:pw.actual,passwordNueva:pw.nueva});
      toast.success('Contraseña actualizada');setPw({actual:'',nueva:'',confirmar:''});
    }catch(e:any){toast.error(e.response?.data?.message||'Contraseña actual incorrecta');}
    setSaving(false);
  };

  const handleFoto=async(e:React.ChangeEvent<HTMLInputElement>)=>{
    const file=e.target.files?.[0];if(!file)return;
    const reader=new FileReader();
    reader.onloadend=()=>setFotoPreview(reader.result as string);
    reader.readAsDataURL(file);
    const fd=new FormData();fd.append('archivo',file);
    try{
      await api.post(`/usuarios/${user?.id}/foto`,fd,{headers:{'Content-Type':'multipart/form-data'}});
      toast.success('Foto actualizada');
    }catch{toast.error('Error al subir foto');}
  };

  const deleteFoto=async()=>{
    try{
      await api.put(`/usuarios/${user?.id}/perfil`,{nombre:user?.nombre,apellido:user?.apellido,email:user?.email,telefono,fotoUrl:null});
      setFotoPreview(null);
      const stored=localStorage.getItem('uide_user');
      if(stored){const u=JSON.parse(stored);u.fotoUrl=null;localStorage.setItem('uide_user',JSON.stringify(u));}
      toast.success('Foto eliminada');
    }catch{toast.error('Error al eliminar foto');}
  };

  return(
    <div className="max-w-3xl space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Mi Perfil</h1><p className="text-gray-500 text-sm mt-1">Gestiona tu cuenta</p></div>

      <div className="card p-6">
        <div className="flex items-center gap-5">
          <div className="relative">
            {fotoPreview?(
              <img src={fotoPreview} alt="Foto" className="w-20 h-20 rounded-full object-cover border-2 border-brand-500"/>
            ):(
              <div className="w-20 h-20 rounded-full bg-gradient-to-br from-brand-500 to-navy-500 flex items-center justify-center text-white text-2xl font-bold">{user?.nombre?.[0]}{user?.apellido?.[0]}</div>
            )}
            <label className="absolute -bottom-1 -right-1 w-7 h-7 bg-gold-400 rounded-full flex items-center justify-center text-white shadow-lg hover:bg-gold-500 cursor-pointer">
              <Camera className="w-3.5 h-3.5"/><input type="file" accept="image/*" className="hidden" onChange={handleFoto}/>
            </label>
          </div>
          <div className="flex-1">
            <h2 className="text-xl font-bold text-gray-900 dark:text-white">{user?.nombre} {user?.apellido}</h2>
            <p className="text-gray-500 text-sm">@{user?.username}</p>
            <span className={`badge mt-1 ${roleColor[role]||''}`}>{roleLabel[role]||role}</span>
          </div>
          {fotoPreview&&(
            <button onClick={deleteFoto} className="p-2 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg text-gray-400 hover:text-red-500 transition-colors" title="Eliminar foto">
              <Trash2 className="w-4 h-4"/>
            </button>
          )}
        </div>
      </div>

      <div className="flex gap-1 bg-gray-100 dark:bg-gray-800 p-1 rounded-xl">
        {([['perfil','Datos',User],['seguridad','Seguridad',Lock],['apariencia','Apariencia',Palette]] as const).map(([k,l,I])=>(
          <button key={k} onClick={()=>setTab(k)} className={`flex-1 flex items-center justify-center gap-2 py-2.5 rounded-lg text-sm font-medium transition-all ${tab===k?'bg-white dark:bg-gray-700 text-gray-900 dark:text-white shadow-sm':'text-gray-500 hover:text-gray-700'}`}><I className="w-4 h-4"/>{l}</button>
        ))}
      </div>

      {tab==='perfil'&&(
        <div className="card p-6 space-y-5">
          <div className="grid sm:grid-cols-2 gap-4">
            <div><label className="label">Nombre</label><input className="input bg-gray-100 dark:bg-gray-800/50 cursor-not-allowed" value={user?.nombre||''} disabled/></div>
            <div><label className="label">Apellido</label><input className="input bg-gray-100 dark:bg-gray-800/50 cursor-not-allowed" value={user?.apellido||''} disabled/></div>
          </div>
          <div><label className="label"><Mail className="w-3.5 h-3.5 inline mr-1"/>Email</label><input className="input bg-gray-100 dark:bg-gray-800/50 cursor-not-allowed" value={user?.email||''} disabled/></div>
          <div><label className="label"><Phone className="w-3.5 h-3.5 inline mr-1"/>Teléfono</label><input className="input" value={telefono} onChange={e=>setTelefono(e.target.value)} placeholder="0999999999"/></div>
          <p className="text-xs text-gray-400">Solo puedes editar tu número de teléfono. Para cambiar nombre o email contacta al administrador.</p>
          <button onClick={saveTelefono} disabled={saving} className="btn-gold"><Save className="w-4 h-4"/>Guardar</button>
        </div>
      )}

      {tab==='seguridad'&&(
        <div className="card p-6 space-y-5">
          <div><label className="label">Contraseña actual</label><input className="input" type="password" value={pw.actual} onChange={e=>setPw(p=>({...p,actual:e.target.value}))}/></div>
          <div><label className="label">Nueva contraseña</label><input className="input" type="password" value={pw.nueva} onChange={e=>setPw(p=>({...p,nueva:e.target.value}))}/></div>
          <div><label className="label">Confirmar</label><input className="input" type="password" value={pw.confirmar} onChange={e=>setPw(p=>({...p,confirmar:e.target.value}))}/></div>
          <button onClick={savePw} disabled={saving} className="btn-gold"><Shield className="w-4 h-4"/>Cambiar Contraseña</button>
        </div>
      )}

      {tab==='apariencia'&&(
        <div className="card p-6">
          <div className="flex items-center justify-between">
            <div><h3 className="font-medium text-gray-900 dark:text-white">Modo oscuro</h3><p className="text-sm text-gray-500">Cambia la apariencia de la app</p></div>
            <button onClick={toggle} className={`w-14 h-7 rounded-full transition-colors relative ${dark?'bg-brand-500':'bg-gray-300'}`}>
              <div className={`w-5 h-5 bg-white rounded-full shadow absolute top-1 transition-all ${dark?'left-8':'left-1'}`}/>
            </button>
          </div>
        </div>
      )}
    </div>
  );
}
