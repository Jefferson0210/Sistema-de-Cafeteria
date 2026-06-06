import api from '../api/axiosConfig';
export const userService = {
  listar: () => api.get('/usuarios'),
  obtener: (id:number) => api.get(`/usuarios/${id}`),
  porRol: (rol:string) => api.get(`/usuarios/rol/${rol}`),
  activos: () => api.get('/usuarios/activos'),
  crear: (d:any) => api.post('/usuarios',d),
  actualizar: (id:number,d:any) => api.put(`/usuarios/${id}`,d),
  activar: (id:number) => api.put(`/usuarios/${id}/activar`),
  desactivar: (id:number) => api.put(`/usuarios/${id}/desactivar`),
  cambiarPassword: (id:number,actual:string,nueva:string) => api.put(`/usuarios/${id}/password`,{passwordActual:actual,passwordNueva:nueva}),
  subirFoto: (id:number,archivo:File) => { const fd=new FormData();fd.append('archivo',archivo);return api.post(`/usuarios/${id}/foto`,fd,{headers:{'Content-Type':'multipart/form-data'}}); },
  me: () => api.get('/auth/me'),
};
