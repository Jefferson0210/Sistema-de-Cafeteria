import React, { useState, useEffect } from 'react';
import { FileText, Eye, Download, Mail } from 'lucide-react';
import { facturaService } from '../../services/facturaService';
import { Factura } from '../../types';
import { money, dateTime } from '../../utils/format';
import StatusBadge from '../../components/ui/StatusBadge';
import LoadingSpinner from '../../components/ui/LoadingSpinner';
import EmptyState from '../../components/ui/EmptyState';
import Modal from '../../components/ui/Modal';
import toast from 'react-hot-toast';

export default function AdminFacturas() {
  const [facturas,setFacturas]=useState<Factura[]>([]);
  const [loading,setLoading]=useState(true);
  const [detail,setDetail]=useState<Factura|null>(null);

  useEffect(()=>{facturaService.listar().then(r=>setFacturas((r.data.data||[]).reverse())).catch(()=>{}).finally(()=>setLoading(false));},[]);

  const descargarPdf=async(id:number,num:string)=>{
    try{const res=await facturaService.descargarPdf(id);const url=window.URL.createObjectURL(new Blob([res.data]));const a=document.createElement('a');a.href=url;a.download=`factura-${num}.pdf`;a.click();window.URL.revokeObjectURL(url);toast.success('PDF descargado');}catch{toast.error('Error al descargar PDF');}
  };
  const enviarEmail=async(id:number)=>{const email=prompt('Email:');if(!email)return;try{await facturaService.enviarEmail(id,email);toast.success('Enviada');}catch{toast.error('Error');}};

  if(loading)return<LoadingSpinner/>;
  return(
    <div className="space-y-6">
      <div><h1 className="text-2xl font-display font-bold text-gray-900 dark:text-white">Facturas</h1><p className="text-gray-500 text-sm mt-1">{facturas.length} facturas</p></div>
      {facturas.length===0?<EmptyState title="Sin facturas" icon={FileText}/>:(
        <div className="space-y-3">{facturas.map(f=>(
          <div key={f.id} className="card p-4">
            <div className="flex items-center justify-between flex-wrap gap-2">
              <div><div className="flex items-center gap-2 mb-1"><span className="font-mono font-semibold text-sm text-gray-900 dark:text-white">{f.numeroFactura}</span><StatusBadge status={f.estado}/></div><p className="text-sm text-gray-500">{f.cliente?`${f.cliente.nombre} ${f.cliente.apellido}`:'Sin cliente'}</p></div>
              <p className="text-lg font-bold text-gray-900 dark:text-white">{money(f.total)}</p>
              <div className="flex gap-1">
                <button onClick={()=>setDetail(f)} className="btn-ghost btn-xs"><Eye className="w-3.5 h-3.5"/></button>
                <button onClick={()=>descargarPdf(f.id,f.numeroFactura)} className="btn-ghost btn-xs"><Download className="w-3.5 h-3.5"/></button>
              </div>
            </div>
          </div>
        ))}</div>
      )}
      <Modal open={!!detail} onClose={()=>setDetail(null)} title={`Factura ${detail?.numeroFactura||''}`}>
        {detail&&<div className="space-y-4">
          <div className="grid grid-cols-2 gap-3 text-sm">
            <div><span className="text-gray-500">Cliente:</span><p className="font-medium text-gray-900 dark:text-white">{detail.cliente?`${detail.cliente.nombre} ${detail.cliente.apellido}`:'—'}</p></div>
            <div><span className="text-gray-500">Cajero:</span><p className="font-medium text-gray-900 dark:text-white">{detail.cajero?`${detail.cajero.nombre} ${detail.cajero.apellido}`:'—'}</p></div>
          </div>
          {(detail as any).detalles&&(detail as any).detalles.length>0&&(
            <div><h4 className="text-sm font-semibold text-gray-700 dark:text-gray-300 mb-2">Productos</h4>
            <div className="space-y-2">{(detail as any).detalles.map((d:any,i:number)=>(
              <div key={i} className="flex items-center justify-between py-1.5 border-b border-gray-50 dark:border-gray-800 last:border-0">
                <div><p className="text-sm font-medium text-gray-900 dark:text-white">{d.producto?.nombre||'Producto'}</p><p className="text-xs text-gray-400">{d.cantidad} x {money(d.precioUnitario||d.producto?.precio||0)}</p></div>
                <span className="text-sm font-semibold">{money((d.precioUnitario||d.producto?.precio||0)*d.cantidad)}</span>
              </div>
            ))}</div></div>
          )}
          <div className="border-t border-gray-100 dark:border-gray-800 pt-3 space-y-1.5">
            <div className="flex justify-between text-sm"><span className="text-gray-500">Subtotal</span><span>{money(detail.subtotal)}</span></div>
            <div className="flex justify-between text-sm"><span className="text-gray-500">IVA</span><span>{money(detail.iva)}</span></div>
            <div className="flex justify-between text-lg font-bold pt-1"><span>Total</span><span className="text-brand-500">{money(detail.total)}</span></div>
          </div>
          <div className="flex gap-2">
            <button onClick={()=>descargarPdf(detail.id,detail.numeroFactura)} className="btn-gold flex-1"><Download className="w-4 h-4"/>PDF</button>
            <button onClick={()=>enviarEmail(detail.id)} className="btn-outline flex-1"><Mail className="w-4 h-4"/>Email</button>
          </div>
        </div>}
      </Modal>
    </div>
  );
}
