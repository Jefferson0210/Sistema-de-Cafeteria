import api from '../api/axiosConfig';
export const facturaService = {
  listar: () => api.get('/facturas'),
  obtener: (id:number) => api.get(`/facturas/${id}`),
  porCliente: (id:number) => api.get(`/facturas/cliente/${id}`),
  porCajero: (id:number) => api.get(`/facturas/cajero/${id}`),
  porEstado: (e:string) => api.get(`/facturas/estado/${e}`),
  porNumero: (n:string) => api.get(`/facturas/numero/${n}`),
  desdePedido: (pedidoId:number,cajeroId:number) => api.post(`/facturas/desde-pedido/${pedidoId}?cajeroId=${cajeroId}`),
  manual: (d:any) => api.post('/facturas/manual',d),
  cambiarEstado: (id:number,estado:string) => api.put(`/facturas/${id}/estado?nuevoEstado=${estado}`),
  descargarPdf: (id:number) => api.get(`/facturas/${id}/pdf`,{responseType:'blob'}),
  enviarEmail: (id:number,email:string) => api.post(`/facturas/${id}/enviar-email?email=${email}`),
};
