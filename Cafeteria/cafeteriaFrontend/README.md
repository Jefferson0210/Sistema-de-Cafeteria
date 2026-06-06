# Cafetería UIDE — Frontend React + TypeScript

## Stack Tecnológico
- React 18 + TypeScript + Vite
- Tailwind CSS (colores UIDE)
- React Router DOM v6 (rutas protegidas por rol)
- Axios (interceptores JWT)
- Lucide React (iconos)
- React Hot Toast (notificaciones)

## Instalación
```bash
npm install
npm run dev
```
Abrir http://localhost:3000 — Backend en http://localhost:8080

## Credenciales de Prueba
| Rol | Username | Password | Ruta |
|-----|----------|----------|------|
| ADMIN | carlos.mendoza | Uide2024* | /admin/dashboard |
| MESERO | ana.torres | Uide2024* | /mesero/pedidos |
| CAJERO | maria.suarez | Uide2024* | /cajero/facturas |
| CLIENTE | juan.perez | Uide2024* | /cliente/menu |

## Arquitectura

### Rutas por Rol
- **Público**: `/` (menú), `/login`
- **ADMIN**: `/admin/dashboard|productos|categorias|mesas|pedidos|facturas|usuarios|perfil`
- **MESERO**: `/mesero/dashboard|mesas|pedidos|nuevo-pedido|perfil`
- **CAJERO**: `/cajero/dashboard|pedidos-servidos|facturas|factura-manual|perfil`
- **CLIENTE**: `/cliente/menu|mis-pedidos|favoritos|reservas|checkout|perfil`

### Estructura
```
src/
├── api/axiosConfig.ts          # Axios + interceptores JWT
├── components/
│   ├── layout/                 # Sidebar, TopBar, DashboardLayout, PublicNavbar, Footer
│   └── ui/                     # Modal, StatusBadge, StatCard, AuthModal, CartDrawer, etc.
├── context/
│   ├── AuthContext.tsx          # Sesión + roles
│   └── CartContext.tsx          # Carrito (funciona sin login)
├── pages/
│   ├── auth/LoginPage.tsx
│   ├── admin/                  # 7 pantallas admin
│   ├── mesero/                 # 4 pantallas mesero
│   ├── cajero/                 # 4 pantallas cajero
│   ├── cliente/                # 6 pantallas cliente
│   └── common/PerfilPage.tsx   # Perfil + cambio de contraseña
├── routes/
│   ├── AppRoutes.tsx           # Router principal
│   └── ProtectedRoute.tsx      # Guards por rol
├── services/                   # 9 servicios API
├── types/index.ts
└── utils/                      # format, roles
```

### Funcionalidades por Rol

**ADMIN**: Dashboard KPIs, CRUD productos/categorías/mesas, gestión pedidos/facturas/usuarios
**MESERO**: Dashboard turno, mapa mesas, crear pedidos (seleccionar cliente+mesa+productos), cambiar estados
**CAJERO**: Dashboard caja, facturar pedidos servidos, factura manual con descuento, historial facturas
**CLIENTE**: Menú público (sin login), carrito, checkout (requiere login), mis pedidos, favoritos, reservas

### Diseño
- Colores UIDE: Brand #910048, Gold #EAAA00, Navy #002D72
- Sidebar oscuro con navegación por rol
- Cards, tablas, badges de estado con colores semánticos
- Responsive, fonts Poppins + Inter
- Footer con horarios, quiénes somos, contacto
