import type { Rol } from '../types';
export const roleHome:Record<Rol,string> = { ADMIN:'/admin/dashboard', MESERO:'/mesero/pedidos', CAJERO:'/cajero/facturas', CLIENTE:'/cliente/menu' };
export const getPrimaryRole = (roles:string[]):Rol => {
  for(const r of ['ADMIN','MESERO','CAJERO','CLIENTE'] as Rol[]) if(roles.includes(r)) return r;
  return 'CLIENTE';
};
export const roleLabel:Record<string,string> = { ADMIN:'Administrador', MESERO:'Mesero', CAJERO:'Cajero', CLIENTE:'Cliente' };
export const roleColor:Record<string,string> = { ADMIN:'bg-purple-100 text-purple-700', MESERO:'bg-blue-100 text-blue-700', CAJERO:'bg-emerald-100 text-emerald-700', CLIENTE:'bg-amber-100 text-amber-700' };
