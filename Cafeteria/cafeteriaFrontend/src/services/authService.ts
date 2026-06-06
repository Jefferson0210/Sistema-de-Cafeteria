import api from '../api/axiosConfig';
export const authService = {
  login: (usernameOrEmail:string, password:string) => api.post('/auth/login',{usernameOrEmail,password}),
  registro: (d:any) => api.post('/auth/registro',d),
  verificarUsername: (u:string) => api.get(`/auth/verificar/username/${u}`),
  verificarEmail: (e:string) => api.get(`/auth/verificar/email?email=${e}`),
  logout: () => api.post('/auth/logout'),
};
