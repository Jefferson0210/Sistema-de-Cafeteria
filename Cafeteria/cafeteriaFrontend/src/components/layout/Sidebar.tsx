import React from 'react';
import { NavLink } from 'react-router-dom';
import { UtensilsCrossed, X } from 'lucide-react';
import type { Rol } from '../../types';
import { useAuth } from '../../context/AuthContext';
import { roleLabel } from '../../utils/roles';

export interface NavItem { to:string; label:string; icon:any; }

interface Props { items:NavItem[]; role:Rol; mobileOpen:boolean; onClose:()=>void; }

export default function Sidebar({items,role,mobileOpen,onClose}:Props) {
  const { user } = useAuth();

  return (
    <aside className={`fixed left-0 top-0 h-screen bg-sidebar z-50 flex flex-col w-60 transition-transform duration-300 lg:translate-x-0 ${mobileOpen ? 'translate-x-0' : '-translate-x-full'}`} style={{paddingTop:'env(safe-area-inset-top)'}}>
      {/* Logo */}
      <div className="h-14 flex items-center justify-between px-4 border-b border-sidebar-border">
        <div className="flex items-center gap-3">
          <div className="w-9 h-9 bg-gradient-to-br from-gold-400 to-gold-500 rounded-xl flex items-center justify-center flex-shrink-0">
            <UtensilsCrossed className="w-5 h-5 text-white"/>
          </div>
          <div><p className="text-white font-display font-bold text-sm leading-tight">Cafetería</p><p className="text-gold-400 text-[10px] font-semibold tracking-wider">UIDE</p></div>
        </div>
        <button onClick={onClose} className="lg:hidden p-1.5 hover:bg-sidebar-hover rounded-lg text-gray-400">
          <X className="w-5 h-5"/>
        </button>
      </div>

      {/* User info */}
      {user && (
        <div className="px-4 py-3 border-b border-sidebar-border">
          <div className="flex items-center gap-2.5">
            <div className="w-8 h-8 rounded-full bg-brand-500 flex items-center justify-center text-white text-xs font-bold flex-shrink-0">
              {user.nombre[0]}{user.apellido[0]}
            </div>
            <div className="min-w-0">
              <p className="text-white text-xs font-medium truncate">{user.nombre} {user.apellido}</p>
              <p className="text-gray-500 text-[10px]">{roleLabel[role]||role}</p>
            </div>
          </div>
        </div>
      )}

      {/* Nav */}
      <nav className="flex-1 py-3 px-2 space-y-0.5 overflow-y-auto">
        {items.map(item => (
          <NavLink key={item.to} to={item.to} onClick={onClose} className={({isActive}) =>
            `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm transition-all ${isActive ? 'bg-sidebar-active text-white font-medium' : 'text-gray-400 hover:bg-sidebar-hover hover:text-gray-200'}`
          }>
            <item.icon className="w-[18px] h-[18px] flex-shrink-0"/>
            <span className="truncate">{item.label}</span>
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}