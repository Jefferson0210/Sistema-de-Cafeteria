import React, { useState } from 'react';
import { X, Eye, EyeOff, UtensilsCrossed } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useNavigate } from 'react-router-dom';
import { roleHome, getPrimaryRole } from '../../utils/roles';
import toast from 'react-hot-toast';

export default function AuthModal() {
  const { showAuth, setShowAuth, login, registro } = useAuth();
  const navigate = useNavigate();
  const [isLogin, setIsLogin] = useState(true);
  const [loading, setLoading] = useState(false);
  const [showPw, setShowPw] = useState(false);
  const [f, setF] = useState({username:'',email:'',password:'',confirmPassword:'',nombre:'',apellido:'',telefono:''});

  if(!showAuth) return null;
  const set = (k:string,v:string) => setF(p=>({...p,[k]:v}));

  const handleSubmit = async (e:React.FormEvent) => {
    e.preventDefault(); setLoading(true);
    try {
      if(isLogin) { const u = await login(f.username, f.password); toast.success(`¡Bienvenido, ${u.nombre}!`); navigate(roleHome[getPrimaryRole(u.roles)]); }
      else { if(f.password!==f.confirmPassword){toast.error('Las contraseñas no coinciden');setLoading(false);return;} const u = await registro(f); toast.success('¡Cuenta creada!'); navigate(roleHome[getPrimaryRole(u.roles)]); }
    } catch(err:any) { toast.error(err.response?.data?.message || 'Error'); }
    setLoading(false);
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4" onClick={()=>setShowAuth(false)}>
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm"/>
      <div className="relative w-full max-w-md bg-white dark:bg-gray-900 rounded-2xl shadow-2xl border border-gray-100 dark:border-gray-800" onClick={e=>e.stopPropagation()}>
        <button onClick={()=>setShowAuth(false)} className="absolute top-4 right-4 p-1.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-400"><X className="w-5 h-5"/></button>
        <div className="px-8 pt-8 pb-2 text-center">
          <div className="w-14 h-14 bg-gradient-to-br from-brand-500 to-brand-600 rounded-2xl flex items-center justify-center mx-auto mb-4"><UtensilsCrossed className="w-7 h-7 text-white"/></div>
          <h2 className="font-display text-2xl font-bold text-gray-900 dark:text-white">{isLogin ? 'Bienvenido' : 'Crear Cuenta'}</h2>
          <p className="text-gray-500 text-sm mt-1">{isLogin ? 'Ingresa a tu cuenta' : 'Regístrate para hacer pedidos'}</p>
        </div>
        <form onSubmit={handleSubmit} className="px-8 py-5 space-y-3.5">
          {!isLogin && <div className="grid grid-cols-2 gap-3"><div><label className="label">Nombre</label><input className="input" value={f.nombre} onChange={e=>set('nombre',e.target.value)} required/></div><div><label className="label">Apellido</label><input className="input" value={f.apellido} onChange={e=>set('apellido',e.target.value)} required/></div></div>}
          <div><label className="label">{isLogin?'Usuario o email':'Username'}</label><input className="input" value={f.username} onChange={e=>set('username',e.target.value)} required/></div>
          {!isLogin && <div><label className="label">Email</label><input className="input" type="email" value={f.email} onChange={e=>set('email',e.target.value)} required/></div>}
          {!isLogin && <div><label className="label">Teléfono</label><input className="input" value={f.telefono} onChange={e=>set('telefono',e.target.value)}/></div>}
          <div><label className="label">Contraseña</label><div className="relative"><input className="input pr-10" type={showPw?'text':'password'} value={f.password} onChange={e=>set('password',e.target.value)} required/><button type="button" onClick={()=>setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400">{showPw?<EyeOff className="w-4 h-4"/>:<Eye className="w-4 h-4"/>}</button></div></div>
          {!isLogin && <div><label className="label">Confirmar</label><input className="input" type="password" value={f.confirmPassword} onChange={e=>set('confirmPassword',e.target.value)} required/></div>}
          <button type="submit" disabled={loading} className="btn-gold w-full !py-3">{loading?'Cargando...':isLogin?'Iniciar Sesión':'Registrarse'}</button>
        </form>
        <div className="px-8 pb-8 text-center"><p className="text-gray-500 text-sm">{isLogin?'¿No tienes cuenta?':'¿Ya tienes cuenta?'}{' '}<button onClick={()=>setIsLogin(!isLogin)} className="text-brand-500 font-semibold">{isLogin?'Regístrate':'Inicia sesión'}</button></p></div>
      </div>
    </div>
  );
}
