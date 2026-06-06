import React from 'react';
import Modal from './Modal';
import { AlertTriangle } from 'lucide-react';
interface Props { open:boolean; onClose:()=>void; onConfirm:()=>void; title:string; message:string; confirmText?:string; danger?:boolean; }
export default function ConfirmDialog({open,onClose,onConfirm,title,message,confirmText='Confirmar',danger=false}:Props) {
  return (
    <Modal open={open} onClose={onClose} title={title} size="max-w-sm">
      <div className="text-center">
        <AlertTriangle className={`w-12 h-12 mx-auto mb-3 ${danger?'text-red-400':'text-amber-400'}`}/>
        <p className="text-gray-600 text-sm mb-6">{message}</p>
        <div className="flex gap-3">
          <button onClick={onClose} className="btn-outline flex-1">Cancelar</button>
          <button onClick={()=>{onConfirm();onClose();}} className={`flex-1 ${danger?'btn-danger':'btn-primary'}`}>{confirmText}</button>
        </div>
      </div>
    </Modal>
  );
}
