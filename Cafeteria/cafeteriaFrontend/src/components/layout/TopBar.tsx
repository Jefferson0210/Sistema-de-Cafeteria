import React from 'react';
import { LogOut, User, ShoppingCart, Sun, Moon, Menu } from 'lucide-react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useCart } from '../../context/CartContext';
import { useTheme } from '../../context/ThemeContext';

export default function TopBar({onMenuClick}:{onMenuClick?:()=>void}) {
  const { user, logout, role } = useAuth();
  const { count, setIsOpen } = useCart();
  const { dark, toggle } = useTheme();
  const navigate = useNavigate();

  return (
    <header className="fixed top-0 right-0 left-0 lg:left-60 h-auto bg-white dark:bg-gray-900 border-b border-gray-200 dark:border-gray-800 z-30 flex items-center justify-between px-4 sm:px-6" style={{paddingTop:'max(0.75rem, env(safe-area-inset-top))',paddingBottom:'0.75rem'}}>
      <div className="flex items-center gap-3">
        <button onClick={onMenuClick} className="lg:hidden p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-500">
          <Menu className="w-5 h-5"/>
        </button>
        <p className="text-gray-900 dark:text-white font-semibold text-sm">{user?.nombre} {user?.apellido}</p>
      </div>
      <div className="flex items-center gap-1.5">
        <button onClick={toggle} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-500 dark:text-gray-400">
          {dark ? <Sun className="w-5 h-5"/> : <Moon className="w-5 h-5"/>}
        </button>
        {role === 'CLIENTE' && (
          <button onClick={()=>setIsOpen(true)} className="relative p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg">
            <ShoppingCart className="w-5 h-5 text-gray-500 dark:text-gray-400"/>
            {count > 0 && <span className="absolute -top-0.5 -right-0.5 w-4 h-4 bg-brand-500 text-white text-[10px] font-bold rounded-full flex items-center justify-center">{count}</span>}
          </button>
        )}
        <Link to={`/${role.toLowerCase()}/perfil`} className="p-2 hover:bg-gray-100 dark:hover:bg-gray-800 rounded-lg text-gray-500 dark:text-gray-400"><User className="w-5 h-5"/></Link>
        <button onClick={()=>{logout();navigate('/login');}} className="p-2 hover:bg-red-50 dark:hover:bg-red-900/20 rounded-lg text-gray-400 hover:text-red-500"><LogOut className="w-5 h-5"/></button>
      </div>
    </header>
  );
}
