import api from '../api/axiosConfig';
export const dashboardService = {
  resumen: () => api.get('/dashboard/resumen'),
  ventas: (desde?:string,hasta?:string) => {
    if(desde&&hasta) return api.get(`/dashboard/ventas?desde=${desde}&hasta=${hasta}`);
    return api.get('/dashboard/ventas');
  },
  topProductos: (n=10) => api.get(`/dashboard/top-productos?limit=${n}`),
  stockBajo: (n=10) => api.get(`/dashboard/stock-bajo?umbral=${n}`),
  ventasPorMesero: () => api.get('/dashboard/ventas-por-mesero'),
};