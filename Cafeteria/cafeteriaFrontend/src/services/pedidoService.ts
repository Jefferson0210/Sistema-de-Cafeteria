import api from '../api/axiosConfig';
export const pedidoService = {
  listar: () => api.get('/pedidos'),
  obtener: (id:number) => api.get(`/pedidos/${id}`),
  porCliente: (id:number) => api.get(`/pedidos/cliente/${id}`),
  porMesero: (id:number) => api.get(`/pedidos/mesero/${id}`),
  porMesa: (id:number) => api.get(`/pedidos/mesa/${id}`),
  porEstado: (e:string) => api.get(`/pedidos/estado/${e}`),
  pendientes: () => api.get('/pedidos/pendientes'),
  crear: (d:any) => api.post('/pedidos',d),
  cambiarEstado: (id:number,estado:string) => api.put(`/pedidos/${id}/estado?nuevoEstado=${estado}`),
  agregarItem: (id:number,item:any) => api.post(`/pedidos/${id}/items`,item),
  eliminarItem: (pid:number,iid:number) => api.delete(`/pedidos/${pid}/items/${iid}`),
};
