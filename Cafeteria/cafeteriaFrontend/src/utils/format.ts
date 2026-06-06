export const money = (v:number) => `$${(v||0).toFixed(2)}`;
export const shortDate = (v?:string) => v ? new Date(v).toLocaleDateString('es-EC',{day:'2-digit',month:'short',year:'numeric'}) : '—';
export const dateTime = (v?:string) => v ? new Date(v).toLocaleString('es-EC',{day:'2-digit',month:'short',hour:'2-digit',minute:'2-digit'}) : '—';
