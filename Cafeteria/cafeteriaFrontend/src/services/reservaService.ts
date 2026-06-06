import api from '../api/axiosConfig';
export const reservaService = {
  listar: () => api.get('/reservas'),
  porUsuario: (id:number) => api.get(`/reservas/usuario/${id}`),
  crear: (d:any,userId:number) => api.post(`/reservas?usuarioId=${userId}`,d),
  confirmar: (id:number) => api.put(`/reservas/${id}/confirmar`),
  cancelar: (id:number) => api.put(`/reservas/${id}/cancelar`),
};
