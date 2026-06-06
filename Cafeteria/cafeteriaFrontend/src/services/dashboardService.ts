import api from '../api/axiosConfig';
export const dashboardService = {
  resumen: () => api.get('/dashboard/resumen'),
  ventas: () => api.get('/dashboard/ventas'),
  topProductos: (n=10) => api.get(`/dashboard/top-productos?limit=${n}`),
  stockBajo: (n=10) => api.get(`/dashboard/stock-bajo?umbral=${n}`),
  ventasPorMesero: () => api.get('/dashboard/ventas-por-mesero'),
};
