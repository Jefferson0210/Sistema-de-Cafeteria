import api from '../api/axiosConfig';

export const pagoService = {
  registrar: (data: { facturaId: number; monto: number; metodoPago: string; referencia?: string }) =>
    api.post('/pagos', data),
  efectivo: (facturaId: number, monto: number) =>
    api.post(`/pagos/efectivo?facturaId=${facturaId}&monto=${monto}`),
  tarjeta: (facturaId: number, monto: number, referencia?: string) =>
    api.post(`/pagos/tarjeta?facturaId=${facturaId}&monto=${monto}${referencia ? `&referencia=${referencia}` : ''}`),
  porFactura: (facturaId: number) => api.get(`/pagos/factura/${facturaId}`),
  totalPagado: (facturaId: number) => api.get(`/pagos/factura/${facturaId}/total`),
  pendiente: (facturaId: number) => api.get(`/pagos/factura/${facturaId}/pendiente`),
  estaPagada: (facturaId: number) => api.get(`/pagos/factura/${facturaId}/pagada`),
};
