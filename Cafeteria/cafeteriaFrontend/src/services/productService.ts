import api from '../api/axiosConfig';

export const productService = {
  listar: () => api.get('/productos'),
  disponibles: () => api.get('/productos/disponibles'),
  obtener: (id: number) => api.get(`/productos/${id}`),
  porCategoria: (id: number) => api.get(`/productos/categoria/${id}`),
  buscar: (q: string) => api.get(`/productos/buscar?nombre=${q}`),
  crear: (d: any) => api.post('/productos', d),
  actualizar: (id: number, d: any) => api.put(`/productos/${id}`, d),
  eliminar: (id: number) => api.delete(`/productos/${id}`),
  agregarStock: (id: number, cantidad: number) => api.put(`/productos/${id}/stock/agregar?cantidad=${cantidad}`),
  desactivar: (id: number) => api.put(`/productos/${id}/desactivar`),
  subirImagen: (id: number, archivo: File) => {
    const formData = new FormData();
    formData.append('archivo', archivo);
    return api.post(`/productos/${id}/imagen`, formData, {
      headers: { 'Content-Type': 'multipart/form-data' }
    });
  },
};