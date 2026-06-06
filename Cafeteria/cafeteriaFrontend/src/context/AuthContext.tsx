import React, { createContext, useContext, useState, useEffect, ReactNode, useCallback } from 'react';
import { User, Rol } from '../types';
import { authService } from '../services/authService';
import { getPrimaryRole } from '../utils/roles';

interface AuthCtx {
  user: User|null; loading:boolean; login:(u:string,p:string)=>Promise<User>;
  registro:(d:any)=>Promise<User>; logout:()=>void;
  role:Rol; hasRole:(r:string)=>boolean; showAuth:boolean; setShowAuth:(v:boolean)=>void;
}
const Ctx = createContext<AuthCtx>({} as AuthCtx);
export const useAuth = () => useContext(Ctx);

export function AuthProvider({children}:{children:ReactNode}) {
  const [user,setUser] = useState<User|null>(null);
  const [loading,setLoading] = useState(true);
  const [showAuth,setShowAuth] = useState(false);

  useEffect(()=>{ const s=localStorage.getItem('uide_user'); if(s) try{setUser(JSON.parse(s))}catch{}; setLoading(false); },[]);

  const saveUser = (d:any):User => {
    const u:User = {id:d.id,username:d.username,email:d.email,nombre:d.nombre,apellido:d.apellido,roles:d.roles||[],token:d.token,telefono:d.telefono};
    localStorage.setItem('uide_user',JSON.stringify(u)); setUser(u); setShowAuth(false); return u;
  };

  const login = async (username:string,password:string) => {
    const res = await authService.login(username,password);
    return saveUser(res.data.data);
  };

  const registro = async (data:any) => {
    const res = await authService.registro(data);
    return saveUser(res.data.data);
  };

  const logout = useCallback(()=>{ localStorage.removeItem('uide_user'); setUser(null); },[]);
  const hasRole = (r:string) => user?.roles?.includes(r) ?? false;
  const role = user ? getPrimaryRole(user.roles) : 'CLIENTE';

  return <Ctx.Provider value={{user,loading,login,registro,logout,role,hasRole,showAuth,setShowAuth}}>{children}</Ctx.Provider>;
}
