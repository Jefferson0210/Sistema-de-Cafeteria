import api from '../api/axiosConfig';
export const mesaService = {
  listar: () => api.get('/mesas'),
  disponibles: () => api.get('/mesas/disponibles'),
  obtener: (id:number) => api.get(`/mesas/${id}`),
  crear: (d:any) => api.post('/mesas',d),
  actualizar: (id:number,d:any) => api.put(`/mesas/${id}`,d),
  eliminar: (id:number) => api.delete(`/mesas/${id}`),
  cambiarEstado: (id:number,estado:string) => api.put(`/mesas/${id}/estado?estado=${estado}`),
};
