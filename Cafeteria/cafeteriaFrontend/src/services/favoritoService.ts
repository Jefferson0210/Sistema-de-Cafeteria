import api from '../api/axiosConfig';
export const favoritoService = {
  listar: (userId:number) => api.get(`/favoritos/usuario/${userId}`),
  agregar: (userId:number,prodId:number) => api.post('/favoritos',{usuarioId:userId,productoId:prodId}),
  eliminar: (id:number) => api.delete(`/favoritos/${id}`),
  toggle: (userId:number,prodId:number) => api.post(`/favoritos/toggle`,{usuarioId:userId,productoId:prodId}),
  existe: (userId:number,prodId:number) => api.get(`/favoritos/existe/${prodId}?usuarioId=${userId}`),
};
