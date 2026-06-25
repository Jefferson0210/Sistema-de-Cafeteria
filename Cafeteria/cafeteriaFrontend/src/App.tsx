import React from 'react';
import { Toaster } from 'react-hot-toast';
import AppRoutes from './routes/AppRoutes';
import CartDrawer from './components/ui/CartDrawer';
import AuthModal from './components/ui/AuthModal';

export default function App() {
  return (
    <>
<Toaster position="bottom-center" toastOptions={{duration:3000,style:{borderRadius:'12px',background:'#fff',color:'#1f2937',boxShadow:'0 4px 12px rgba(0,0,0,0.1)',border:'1px solid #f3f4f6',fontSize:'14px'}}}/>      <AuthModal/>
      <AppRoutes/>
    </>
  );
}
