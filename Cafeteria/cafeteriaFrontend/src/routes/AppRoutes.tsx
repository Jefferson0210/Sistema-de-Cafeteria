import React from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { LayoutDashboard, Package, Tags, Armchair, ClipboardList, FileText, Users, ChefHat, Receipt, PlusCircle, Coffee, Heart, CalendarDays, History, UserCircle } from 'lucide-react';

import DashboardLayout from '../components/layout/DashboardLayout';
import { ProtectedRoute, RoleRoute } from './ProtectedRoute';
import { useAuth } from '../context/AuthContext';
import { roleHome } from '../utils/roles';
import type { NavItem } from '../components/layout/Sidebar';

// Pages
import LoginPage from '../pages/auth/LoginPage';
import PublicMenuPage from '../pages/cliente/PublicMenuPage';
import CheckoutPage from '../pages/cliente/CheckoutPage';

// Admin
import AdminDashboard from '../pages/admin/AdminDashboard';
import AdminProductos from '../pages/admin/AdminProductos';
import AdminCategorias from '../pages/admin/AdminCategorias';
import AdminMesas from '../pages/admin/AdminMesas';
import AdminPedidos from '../pages/admin/AdminPedidos';
import AdminFacturas from '../pages/admin/AdminFacturas';
import AdminUsuarios from '../pages/admin/AdminUsuarios';

// Mesero
import MeseroDashboard from '../pages/mesero/MeseroDashboard';
import MeseroMesas from '../pages/mesero/MeseroMesas';
import MeseroPedidos from '../pages/mesero/MeseroPedidos';
import MeseroNuevoPedido from '../pages/mesero/MeseroNuevoPedido';

// Cajero
import CajeroDashboard from '../pages/cajero/CajeroDashboard';
import CajeroPedidosServidos from '../pages/cajero/CajeroPedidosServidos';
import CajeroFacturas from '../pages/cajero/CajeroFacturas';
import CajeroFacturaManual from '../pages/cajero/CajeroFacturaManual';

// Cliente
import ClienteMenu from '../pages/cliente/ClienteMenu';
import ClientePedidos from '../pages/cliente/ClientePedidos';
import ClienteFavoritos from '../pages/cliente/ClienteFavoritos';
import ClienteReservas from '../pages/cliente/ClienteReservas';

// Common
import PerfilPage from '../pages/common/PerfilPage';

const adminNav:NavItem[] = [
  {to:'/admin/dashboard',label:'Dashboard',icon:LayoutDashboard},
  {to:'/admin/productos',label:'Productos',icon:Package},
  {to:'/admin/categorias',label:'Categorías',icon:Tags},
  {to:'/admin/mesas',label:'Mesas',icon:Armchair},
  {to:'/admin/pedidos',label:'Pedidos',icon:ClipboardList},
  {to:'/admin/facturas',label:'Facturas',icon:FileText},
  {to:'/admin/usuarios',label:'Usuarios',icon:Users},
  {to:'/admin/perfil',label:'Mi Perfil',icon:UserCircle},
];
const meseroNav:NavItem[] = [
  {to:'/mesero/dashboard',label:'Dashboard',icon:LayoutDashboard},
  {to:'/mesero/mesas',label:'Mesas',icon:Armchair},
  {to:'/mesero/pedidos',label:'Pedidos',icon:ClipboardList},
  {to:'/mesero/nuevo-pedido',label:'Nuevo Pedido',icon:PlusCircle},
  {to:'/mesero/perfil',label:'Mi Perfil',icon:UserCircle},
];
const cajeroNav:NavItem[] = [
  {to:'/cajero/dashboard',label:'Dashboard',icon:LayoutDashboard},
  {to:'/cajero/pedidos-servidos',label:'Pedidos Servidos',icon:ChefHat},
  {to:'/cajero/facturas',label:'Facturas',icon:FileText},
  {to:'/cajero/factura-manual',label:'Factura Manual',icon:Receipt},
  {to:'/cajero/perfil',label:'Mi Perfil',icon:UserCircle},
];
const clienteNav:NavItem[] = [
  {to:'/cliente/menu',label:'Menú',icon:Coffee},
  {to:'/cliente/mis-pedidos',label:'Mis Pedidos',icon:History},
  {to:'/cliente/favoritos',label:'Favoritos',icon:Heart},
  {to:'/cliente/reservas',label:'Reservas',icon:CalendarDays},
  {to:'/cliente/perfil',label:'Mi Perfil',icon:UserCircle},
];

function SmartHome() {
  const { user, loading, role } = useAuth();
  if (loading) return null;
  if (user) return <Navigate to={roleHome[role]} replace/>;
  return <PublicMenuPage/>;
}

export default function AppRoutes() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage/>}/>
      <Route path="/" element={<SmartHome/>}/>

      {/* ADMIN */}
      <Route element={<RoleRoute roles={['ADMIN']}><DashboardLayout items={adminNav} role="ADMIN"/></RoleRoute>}>
        <Route path="/admin/dashboard" element={<AdminDashboard/>}/>
        <Route path="/admin/productos" element={<AdminProductos/>}/>
        <Route path="/admin/categorias" element={<AdminCategorias/>}/>
        <Route path="/admin/mesas" element={<AdminMesas/>}/>
        <Route path="/admin/pedidos" element={<AdminPedidos/>}/>
        <Route path="/admin/facturas" element={<AdminFacturas/>}/>
        <Route path="/admin/usuarios" element={<AdminUsuarios/>}/>
        <Route path="/admin/perfil" element={<PerfilPage/>}/>
      </Route>

      {/* MESERO */}
      <Route element={<RoleRoute roles={['MESERO']}><DashboardLayout items={meseroNav} role="MESERO"/></RoleRoute>}>
        <Route path="/mesero/dashboard" element={<MeseroDashboard/>}/>
        <Route path="/mesero/mesas" element={<MeseroMesas/>}/>
        <Route path="/mesero/pedidos" element={<MeseroPedidos/>}/>
        <Route path="/mesero/nuevo-pedido" element={<MeseroNuevoPedido/>}/>
        <Route path="/mesero/perfil" element={<PerfilPage/>}/>
      </Route>

      {/* CAJERO */}
      <Route element={<RoleRoute roles={['CAJERO']}><DashboardLayout items={cajeroNav} role="CAJERO"/></RoleRoute>}>
        <Route path="/cajero/dashboard" element={<CajeroDashboard/>}/>
        <Route path="/cajero/pedidos-servidos" element={<CajeroPedidosServidos/>}/>
        <Route path="/cajero/facturas" element={<CajeroFacturas/>}/>
        <Route path="/cajero/factura-manual" element={<CajeroFacturaManual/>}/>
        <Route path="/cajero/perfil" element={<PerfilPage/>}/>
      </Route>

      {/* CLIENTE */}
      <Route element={<ProtectedRoute><DashboardLayout items={clienteNav} role="CLIENTE"/></ProtectedRoute>}>
        <Route path="/cliente/menu" element={<ClienteMenu/>}/>
        <Route path="/cliente/mis-pedidos" element={<ClientePedidos/>}/>
        <Route path="/cliente/favoritos" element={<ClienteFavoritos/>}/>
        <Route path="/cliente/reservas" element={<ClienteReservas/>}/>
        <Route path="/cliente/checkout" element={<CheckoutPage/>}/>
        <Route path="/cliente/perfil" element={<PerfilPage/>}/>
      </Route>

      <Route path="*" element={<Navigate to="/" replace/>}/>
    </Routes>
  );
}