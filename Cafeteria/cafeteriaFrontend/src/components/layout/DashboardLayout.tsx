import React, { useState } from 'react';
import { Outlet } from 'react-router-dom';
import Sidebar, { NavItem } from './Sidebar';
import TopBar from './TopBar';
import type { Rol } from '../../types';

export default function DashboardLayout({items,role}:{items:NavItem[];role:Rol}) {
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="min-h-screen bg-gray-50 dark:bg-gray-950">
      {sidebarOpen && (
        <div className="fixed inset-0 bg-black/50 z-40 lg:hidden" onClick={()=>setSidebarOpen(false)}/>
      )}
      <Sidebar items={items} role={role} mobileOpen={sidebarOpen} onClose={()=>setSidebarOpen(false)}/>
      <TopBar onMenuClick={()=>setSidebarOpen(true)}/>
      <main className="lg:ml-60 pt-16 p-4 sm:p-6 min-h-screen">
        <Outlet/>
      </main>
    </div>
  );
}
