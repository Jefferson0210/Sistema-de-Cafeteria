import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import type { Rol } from '../types';
import { roleHome } from '../utils/roles';

export function ProtectedRoute({children}:{children:React.ReactNode}) {
  const {user,loading} = useAuth();
  if(loading) return null;
  if(!user) return <Navigate to="/login" replace/>;
  return <>{children}</>;
}

export function RoleRoute({children,roles}:{children:React.ReactNode;roles:Rol[]}) {
  const {user,loading,role} = useAuth();
  if(loading) return null;
  if(!user) return <Navigate to="/login" replace/>;
  if(!roles.includes(role)) return <Navigate to={roleHome[role]} replace/>;
  return <>{children}</>;
}
