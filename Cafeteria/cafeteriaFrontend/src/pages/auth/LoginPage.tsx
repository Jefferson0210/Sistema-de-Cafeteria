import React, { useState } from 'react';
import { useNavigate, Navigate } from 'react-router-dom';
import { Eye, EyeOff, UtensilsCrossed } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { roleHome, getPrimaryRole } from '../../utils/roles';
import toast from 'react-hot-toast';

export default function LoginPage() {
  const { user, login } = useAuth();
  const navigate = useNavigate();
  const [username,setUsername] = useState('');
  const [password,setPassword] = useState('');
  const [showPw,setShowPw] = useState(false);
  const [loading,setLoading] = useState(false);
  const [error,setError] = useState('');

  if(user) return <Navigate to={roleHome[getPrimaryRole(user.roles)]} replace/>;

  const handleSubmit = async (e:React.FormEvent) => {
    e.preventDefault(); setError(''); setLoading(true);
    if(!username.trim()||!password.trim()) { setError('Completa todos los campos'); setLoading(false); return; }
    try {
      const u = await login(username, password);
      toast.success(`¡Bienvenido, ${u.nombre}!`);
      navigate(roleHome[getPrimaryRole(u.roles)]);
    } catch(err:any) { setError(err.response?.data?.message || 'Credenciales incorrectas'); }
    setLoading(false);
  };

  return (
    <div className="min-h-screen flex">
      {/* Left panel */}
      <div className="hidden lg:flex lg:w-1/2 bg-gradient-to-br from-brand-500 via-brand-600 to-navy-500 items-center justify-center p-12 relative overflow-hidden">
        <div className="absolute inset-0 opacity-10"><div className="absolute top-20 -left-20 w-80 h-80 bg-gold-400 rounded-full blur-3xl"/><div className="absolute bottom-20 right-10 w-96 h-96 bg-white rounded-full blur-3xl"/></div>
        <div className="relative text-center">
          <div className="w-20 h-20 bg-white/10 backdrop-blur rounded-3xl flex items-center justify-center mx-auto mb-8"><UtensilsCrossed className="w-10 h-10 text-white"/></div>
          <h1 className="font-display text-5xl font-extrabold text-white leading-tight">Cafetería<br/><span className="text-gold-400">UIDE</span></h1>
          <p className="text-white/70 mt-6 text-lg max-w-md mx-auto leading-relaxed">Sistema de gestión de cafetería universitaria</p>
          <div className="flex gap-4 mt-10 justify-center">
            {['☕','🥐','🧃','🍰'].map((e,i)=><span key={i} className="text-4xl opacity-70 hover:opacity-100 hover:scale-125 transition-all cursor-default">{e}</span>)}
          </div>
        </div>
      </div>

      {/* Right panel */}
      <div className="flex-1 flex items-center justify-center p-6 bg-gray-50 dark:bg-gray-950">
        <div className="w-full max-w-md">
          <div className="lg:hidden flex items-center gap-3 mb-10 justify-center">
            <div className="w-12 h-12 bg-gradient-to-br from-brand-500 to-brand-600 rounded-2xl flex items-center justify-center"><UtensilsCrossed className="w-6 h-6 text-white"/></div>
            <div><span className="font-display font-bold text-xl text-gray-900 dark:text-white">Cafetería</span> <span className="text-brand-500 font-display font-bold text-xl">UIDE</span></div>
          </div>

          <h2 className="font-display text-3xl font-bold text-gray-900 dark:text-white mb-2">Iniciar Sesión</h2>
          <p className="text-gray-500 mb-8">Ingresa tus credenciales para acceder al sistema</p>

          {error && <div className="bg-red-50 border border-red-200 text-red-700 rounded-xl px-4 py-3 text-sm mb-5">{error}</div>}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label className="label">Usuario o Email</label>
              <input className="input !py-3" placeholder="tu.usuario" value={username} onChange={e=>setUsername(e.target.value)} autoFocus/>
            </div>
            <div>
              <label className="label">Contraseña</label>
              <div className="relative">
                <input className="input !py-3 pr-11" type={showPw?'text':'password'} placeholder="••••••••" value={password} onChange={e=>setPassword(e.target.value)}/>
                <button type="button" onClick={()=>setShowPw(!showPw)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">{showPw?<EyeOff className="w-4 h-4"/>:<Eye className="w-4 h-4"/>}</button>
              </div>
            </div>
            <button type="submit" disabled={loading} className="btn-gold w-full !py-3.5 !text-base">{loading?'Ingresando...':'Iniciar Sesión'}</button>
          </form>

          {import.meta.env.DEV && (
            <details className="mt-8">
              <summary className="text-xs text-gray-400 cursor-pointer hover:text-gray-500">Credenciales de prueba</summary>
              <div className="mt-2 bg-gray-100 rounded-xl p-3 space-y-1 text-xs text-gray-500">
                {[['ADMIN','carlos.mendoza'],['MESERO','ana.torres'],['CAJERO','maria.suarez'],['CLIENTE','juan.perez']].map(([r,u])=>(
                  <button key={u} onClick={()=>{setUsername(u);setPassword('Uide2024*');}} className="block hover:text-brand-500 w-full text-left">
                    <span className="font-mono font-medium">{r}</span> → {u} / Uide2024*
                  </button>
                ))}
              </div>
            </details>
          )}
        </div>
      </div>
    </div>
  );
}
