import React from 'react';
const cfg:Record<string,string> = {
  PENDIENTE:'bg-amber-100 text-amber-700', EN_PREPARACION:'bg-blue-100 text-blue-700',
  SERVIDO:'bg-emerald-100 text-emerald-700', PAGADO:'bg-green-100 text-green-700',
  CANCELADO:'bg-red-100 text-red-700', CANCELADA:'bg-red-100 text-red-700',
  DISPONIBLE:'bg-green-100 text-green-700', OCUPADA:'bg-red-100 text-red-700',
  RESERVADA:'bg-blue-100 text-blue-700', CONFIRMADA:'bg-green-100 text-green-700',
  COMPLETADA:'bg-gray-100 text-gray-600',
};
export default function StatusBadge({status,className=''}:{status:string;className?:string}) {
  const label = status.replace(/_/g,' ');
  return <span className={`badge ${cfg[status]||'bg-gray-100 text-gray-600'} ${className}`}>{label}</span>;
}
