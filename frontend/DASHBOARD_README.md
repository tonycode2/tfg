# Dashboard del Sistema de Recursos Humanos

## 📋 Descripción

Dashboard completo para el Sistema de Gestión de RH con diferentes vistas según el rol del usuario.

## 🎭 Roles y Permisos

### 👤 Empleado
- Ver inicio con resumen personal
- Consultar mi planilla
- Gestionar mis solicitudes (vacaciones, permisos, etc.)
- Ver estado de solicitudes

### 👔 Jefe de Departamento
Incluye todo lo de Empleado, más:
- Ver asistencia de empleados
- Solicitar horas extra para empleados
- Revisar solicitudes pendientes de sus empleados
- Ver reportes de su departamento

### 👨‍💼 HR (Recursos Humanos) y Admin
Incluyen todo lo de Jefe, más:
- Gestionar planilla general
- Procesar liquidaciones
- Administrar aguinaldo
- Aprobar solicitudes de horas extra
- Acceso a todos los reportes del sistema

## 🚀 Características Implementadas

### Menú Lateral (Sidebar)
- Navegación dinámica según el rol del usuario
- Iconos representativos para cada sección
- Indicador visual de la sección activa

### Vista de Inicio
- Tarjetas estadísticas personalizadas según el rol
- Actividad reciente
- Próximos eventos y fechas importantes
- Tendencias y métricas clave

### Theme Toggle
- Botón en la esquina superior derecha
- Cambio entre tema claro y oscuro
- Preferencia guardada en localStorage

### Header del Dashboard
- Avatar del usuario
- Nombre de usuario
- Botón de cerrar sesión

## 🧪 Modo Demo

Actualmente el sistema está en modo demo para pruebas sin backend:

1. **Login**: En la página de login puedes seleccionar cualquier rol para probar
2. **Usuario**: Ingresa cualquier nombre de usuario
3. **Contraseña**: Ingresa cualquier contraseña
4. **Rol**: Selecciona el rol que deseas probar (Empleado, Jefe, HR, Admin)

El selector de rol será removido cuando se conecte al backend real.

## 📁 Estructura de Componentes

```
src/
├── components/
│   ├── Sidebar.tsx              # Menú lateral de navegación
│   ├── DashboardHeader.tsx      # Encabezado del dashboard
│   ├── StatsCard.tsx            # Tarjetas de estadísticas
│   ├── ThemeToggle.tsx          # Botón de cambio de tema
│   └── dashboard/
│       ├── InicioView.tsx       # Vista principal de inicio
│       └── PlaceholderView.tsx  # Vista placeholder para módulos en desarrollo
├── hooks/
│   └── useTheme.tsx             # Hook para manejo de tema
├── pages/
│   ├── DashboardPage.tsx        # Página principal del dashboard
│   └── LoginPage.tsx            # Página de inicio de sesión
└── services/
    └── authService.ts           # Servicio de autenticación
```

## 🔄 Próximos Pasos

Las siguientes secciones están pendientes de desarrollo (mostrarán vistas placeholder):

1. **Mi Planilla** - Consulta de planilla personal
2. **Mis Solicitudes** - Gestión de solicitudes
3. **Asistencia** - Control de asistencia
4. **Horas Extra** - Gestión de horas extra
5. **Solicitudes Pendientes** - Aprobación de solicitudes
6. **Planilla General** - Administración de planilla
7. **Liquidaciones** - Procesamiento de liquidaciones
8. **Aguinaldo** - Gestión de aguinaldos
9. **Reportes** - Generación de reportes

Estos módulos se implementarán progresivamente a medida que se desarrollen los endpoints del backend correspondientes.

## 🎨 Temas

El sistema incluye soporte completo para tema claro y oscuro:
- Los colores se adaptan automáticamente
- La preferencia se guarda localmente
- Transición suave entre temas

## 💡 Notas Técnicas

- **Framework**: React + TypeScript
- **Routing**: React Router v6
- **UI Components**: Shadcn/ui (Radix UI)
- **Estilos**: Tailwind CSS
- **Íconos**: SVG inline (Heroicons style)
