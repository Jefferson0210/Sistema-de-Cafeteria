import api from '../api/axiosConfig';
export const categoryService = {
  listar: () => api.get('/categorias'),
  activas: () => api.get('/categorias/activas'),
  obtener: (id:number) => api.get(`/categorias/${id}`),
  crear: (d:any) => api.post('/categorias',d),
  actualizar: (id:number,d:any) => api.put(`/categorias/${id}`,d),
  eliminar: (id:number) => api.delete(`/categorias/${id}`),
};
