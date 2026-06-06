import axios from 'axios';
const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || '/api' });
api.interceptors.request.use(cfg => {
  const raw = localStorage.getItem('uide_user');
  if(raw){ try{ const {token}=JSON.parse(raw); if(token) cfg.headers.Authorization=`Bearer ${token}`; }catch{} }
  return cfg;
});
api.interceptors.response.use(r=>r, err=>{
  if(err.response?.status===401){ localStorage.removeItem('uide_user'); window.location.href='/login'; }
  return Promise.reject(err);
});
export default api;
