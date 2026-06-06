import React from 'react';
import { Link } from 'react-router-dom';
import { ShoppingCart, UtensilsCrossed, Sun, Moon } from 'lucide-react';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { useTheme } from '../../context/ThemeContext';
import { roleHome } from '../../utils/roles';

export default function PublicNavbar() {
  const { user, role, setShowAuth } = useAuth();
  const { count, setIsOpen } = useCart();
  const { dark, toggle } = useTheme();

  return (
    <nav className="fixed top-0 left-0 right-0 z-50 bg-white/95 dark:bg-gray-900/95 backdrop-blur-md border-b border-gray-100 dark:border-gray-800 shadow-sm" style={{paddingTop:'env(safe-area-inset-top)'}}>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 h-14 flex items-center justify-between">
        <Link to="/" className="flex items-center gap-2 group">
          <div className="w-8 h-8 bg-gradient-to-br from-brand-500 to-brand-600 rounded-xl flex items-center justify-center group-hover:scale-105 transition-transform">
            <UtensilsCrossed className="w-4 h-4 text-white"/>
          </div>
          <div><span className="font-display font-bold text-gray-900 dark:text-white text-base">Cafetería</span> <span className="text-brand-500 font-display font-bold text-base">UIDE</span></div>
        </Link>

        <div className="flex items-center gap-1">
          <button onClick={toggle} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-500 dark:text-gray-400">
            {dark ? <Sun className="w-4 h-4"/> : <Moon className="w-4 h-4"/>}
          </button>
          <button onClick={()=>setIsOpen(true)} className="relative p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg">
            <ShoppingCart className="w-4 h-4 text-gray-600 dark:text-gray-400"/>
            {count>0 && <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-brand-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">{count}</span>}
          </button>
          {user ? (
            <Link to={roleHome[role]} className="ml-1 text-xs font-semibold bg-brand-500 text-white px-3 py-1.5 rounded-lg">{user.nombre}</Link>
          ) : (
            <button onClick={()=>setShowAuth(true)} className="ml-1 text-xs font-semibold bg-brand-500 text-white px-3 py-1.5 rounded-lg">Iniciar Sesión</button>
          )}
        </div>
      </div>
    </nav>
  );
}
