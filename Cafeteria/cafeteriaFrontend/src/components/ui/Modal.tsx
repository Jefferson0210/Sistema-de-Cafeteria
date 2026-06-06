import React, { ReactNode } from 'react';
import { X } from 'lucide-react';
interface Props { open:boolean; onClose:()=>void; title:string; children:ReactNode; size?:string; }
export default function Modal({open,onClose,title,children,size='max-w-lg'}:Props) {
  if(!open) return null;
  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center p-4" onClick={onClose}>
      <div className="absolute inset-0 bg-black/40 backdrop-blur-sm"/>
      <div className={`relative w-full ${size} bg-white dark:bg-gray-900 rounded-2xl shadow-2xl max-h-[90vh] flex flex-col border border-gray-100 dark:border-gray-800`} onClick={e=>e.stopPropagation()}>
        <div className="flex items-center justify-between px-6 py-4 border-b border-gray-100 dark:border-gray-800">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white">{title}</h3>
          <button onClick={onClose} className="p-1.5 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-400"><X className="w-5 h-5"/></button>
        </div>
        <div className="px-6 py-4 overflow-y-auto flex-1">{children}</div>
      </div>
    </div>
  );
}
