# Navegación Horizontal Superior - Implementation Plan

## Goal
Reemplazar la barra lateral de navegación por una barra horizontal superior con iconos que se expanden al hacer hover, mostrando el nombre del item.

## Prerequisites
Make sure that the user is currently on the `feature/horizontal-navigation` branch before beginning implementation.
If not, move them to the correct branch. If the branch does not exist, create it from main.

---

### Step-by-Step Instructions

---

### Step 1: Crear componente TopNavbar

- [x] Create the new file `frontend/src/components/TopNavbar.tsx`
- [x] Copy and paste the code below:

```tsx
import { memo, useMemo, useState } from 'react';
import { cn } from '../lib/utils';
import { Button } from './ui/button';
import { useTheme } from '../hooks/useTheme';
import type { Role } from '../services/authService';
import type { ReactElement } from 'react';

interface MenuItem {
  id: string;
  label: string;
  icon: ReactElement;
  roles: Role[];
}

interface TopNavbarProps {
  userRole: Role;
  username: string;
  activeItem: string;
  onItemClick: (id: string) => void;
  onLogout: () => void;
}

const menuItems: MenuItem[] = [
  {
    id: 'inicio',
    label: 'Inicio',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 12l2-2m0 0l7-7 7 7M5 10v10a1 1 0 001 1h3m10-11l2 2m-2-2v10a1 1 0 01-1 1h-3m-6 0a1 1 0 001-1v-4a1 1 0 011-1h2a1 1 0 011 1v4a1 1 0 001 1m-6 0h6" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'mi-planilla',
    label: 'Mi Planilla',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 7h6m0 10v-3m-3 3h.01M9 17h.01M9 14h.01M12 14h.01M15 11h.01M12 11h.01M9 11h.01M7 21h10a2 2 0 002-2V5a2 2 0 00-2-2H7a2 2 0 00-2 2v14a2 2 0 002 2z" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'mis-solicitudes',
    label: 'Mis Solicitudes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'asistencia',
    label: 'Asistencia',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'horas-extra',
    label: 'Horas Extra',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v3m0 0v3m0-3h3m-3 0H9m12 0a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    roles: ['JEFE'],
  },
  {
    id: 'solicitudes-pendientes',
    label: 'Solicitudes Pendientes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
    ),
    roles: ['HR', 'JEFE'],
  },
  {
    id: 'planilla-general',
    label: 'Planilla General',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 9V7a2 2 0 00-2-2H5a2 2 0 00-2 2v6a2 2 0 002 2h2m2 4h10a2 2 0 002-2v-6a2 2 0 00-2-2H9a2 2 0 00-2 2v6a2 2 0 002 2zm7-5a2 2 0 11-4 0 2 2 0 014 0z" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'liquidaciones',
    label: 'Liquidaciones',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 14l6-6m-5.5.5h.01m4.99 5h.01M19 21V5a2 2 0 00-2-2H7a2 2 0 00-2 2v16l3.5-2 3.5 2 3.5-2 3.5 2z" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'aguinaldo',
    label: 'Aguinaldo',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'reportes',
    label: 'Reportes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 17v-2m3 2v-4m3 4v-6m2 10H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
    roles: ['HR', 'JEFE'],
  },
  {
    id: 'empleados',
    label: 'Empleados',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4.354a4 4 0 110 5.292M15 21H3v-1a6 6 0 0112 0v1zm0 0h6v-1a6 6 0 00-9-5.197M13 7a4 4 0 11-8 0 4 4 0 018 0z" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'mantenimientos',
    label: 'Mantenimientos y Consultas',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 7v10c0 2.21 3.582 4 8 4s8-1.79 8-4V7M4 7c0 2.21 3.582 4 8 4s8-1.79 8-4M4 7c0-2.21 3.582-4 8-4s8 1.79 8 4m0 5c0 2.21-3.582 4-8 4s-8-1.79-8-4" />
      </svg>
    ),
    roles: ['ADMIN'],
  },
];

// NavItem component with hover expand effect
const NavItem = memo(function NavItem({
  item,
  isActive,
  onClick,
}: {
  item: MenuItem;
  isActive: boolean;
  onClick: () => void;
}) {
  return (
    <button
      onClick={onClick}
      className={cn(
        'group relative flex items-center gap-2 rounded-lg px-3 py-2 transition-all duration-200 ease-in-out',
        'hover:bg-accent',
        isActive
          ? 'bg-primary text-primary-foreground hover:bg-primary/90'
          : 'text-muted-foreground hover:text-accent-foreground'
      )}
    >
      {/* Icon - always visible */}
      <span className="flex-shrink-0">{item.icon}</span>
      
      {/* Label - hidden by default, visible on hover with animation */}
      <span
        className={cn(
          'whitespace-nowrap overflow-hidden transition-all duration-200 ease-in-out',
          'max-w-0 opacity-0 group-hover:max-w-[200px] group-hover:opacity-100',
          isActive && 'max-w-[200px] opacity-100'
        )}
      >
        {item.label}
      </span>
    </button>
  );
});

// Mobile hamburger menu
const MobileMenu = memo(function MobileMenu({
  isOpen,
  items,
  activeItem,
  onItemClick,
  onClose,
}: {
  isOpen: boolean;
  items: MenuItem[];
  activeItem: string;
  onItemClick: (id: string) => void;
  onClose: () => void;
}) {
  if (!isOpen) return null;

  return (
    <>
      {/* Backdrop */}
      <div
        className="fixed inset-0 bg-black/50 z-40 md:hidden"
        onClick={onClose}
      />
      
      {/* Slide-in menu */}
      <div className="fixed top-0 left-0 h-full w-64 bg-card border-r border-border z-50 md:hidden animate-in slide-in-from-left duration-200">
        <div className="flex items-center justify-between p-4 border-b border-border">
          <div className="flex items-center gap-2">
            <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
              <span className="text-primary-foreground font-bold text-sm">GA</span>
            </div>
            <span className="font-semibold text-foreground">Gerson Andre</span>
          </div>
          <Button variant="ghost" size="icon" onClick={onClose}>
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </Button>
        </div>
        
        <nav className="p-4 space-y-2">
          {items.map((item) => (
            <button
              key={item.id}
              onClick={() => {
                onItemClick(item.id);
                onClose();
              }}
              className={cn(
                'w-full flex items-center gap-3 rounded-lg px-3 py-2 transition-colors',
                activeItem === item.id
                  ? 'bg-primary text-primary-foreground'
                  : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
              )}
            >
              {item.icon}
              <span>{item.label}</span>
            </button>
          ))}
        </nav>
      </div>
    </>
  );
});

export const TopNavbar = memo(function TopNavbar({
  userRole,
  username,
  activeItem,
  onItemClick,
  onLogout,
}: TopNavbarProps) {
  const { theme, toggleTheme } = useTheme();
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);

  const filteredItems = useMemo(
    () => menuItems.filter((item) => item.roles.includes(userRole)),
    [userRole]
  );

  const roleText = useMemo(() => {
    switch (userRole) {
      case 'ADMIN':
        return 'Administrador';
      case 'HR':
        return 'Recursos Humanos';
      case 'JEFE':
        return 'Jefe de Departamento';
      case 'EMPLEADO':
        return 'Empleado';
      default:
        return 'Usuario';
    }
  }, [userRole]);

  return (
    <>
      <header className="sticky top-0 z-30 w-full bg-card border-b border-border">
        <div className="flex items-center justify-between h-16 px-4 md:px-6">
          {/* Left: Logo + Mobile hamburger */}
          <div className="flex items-center gap-4">
            {/* Mobile hamburger button */}
            <Button
              variant="ghost"
              size="icon"
              className="md:hidden"
              onClick={() => setMobileMenuOpen(true)}
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 6h16M4 12h16M4 18h16" />
              </svg>
            </Button>

            {/* Logo */}
            <div className="flex items-center gap-2">
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center">
                <span className="text-primary-foreground font-bold text-sm">GA</span>
              </div>
              <span className="font-semibold text-foreground hidden sm:block">Gerson Andre</span>
            </div>
          </div>

          {/* Center: Navigation items (hidden on mobile) */}
          <nav className="hidden md:flex items-center gap-1">
            {filteredItems.map((item) => (
              <NavItem
                key={item.id}
                item={item}
                isActive={activeItem === item.id}
                onClick={() => onItemClick(item.id)}
              />
            ))}
          </nav>

          {/* Right: User info + actions */}
          <div className="flex items-center gap-2">
            {/* User info - hidden on small screens */}
            <div className="hidden lg:flex flex-col items-end mr-2">
              <span className="text-sm font-medium text-foreground">{username}</span>
              <span className="text-xs text-muted-foreground">{roleText}</span>
            </div>

            {/* Theme toggle */}
            <Button
              variant="ghost"
              size="icon"
              onClick={toggleTheme}
              title={theme === 'light' ? 'Cambiar a modo oscuro' : 'Cambiar a modo claro'}
            >
              {theme === 'light' ? (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M20.354 15.354A9 9 0 018.646 3.646 9.003 9.003 0 0012 21a9.003 9.003 0 008.354-5.646z" />
                </svg>
              ) : (
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v1m0 16v1m9-9h-1M4 12H3m15.364 6.364l-.707-.707M6.343 6.343l-.707-.707m12.728 0l-.707.707M6.343 17.657l-.707.707M16 12a4 4 0 11-8 0 4 4 0 018 0z" />
                </svg>
              )}
            </Button>

            {/* Logout button */}
            <Button
              variant="ghost"
              size="icon"
              onClick={onLogout}
              title="Cerrar sesión"
            >
              <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
              </svg>
            </Button>

            {/* User avatar (visible on mobile when name is hidden) */}
            <div className="w-8 h-8 bg-accent rounded-full flex items-center justify-center lg:hidden">
              <span className="text-accent-foreground font-medium text-sm">
                {username.charAt(0).toUpperCase()}
              </span>
            </div>
          </div>
        </div>
      </header>

      {/* Mobile menu drawer */}
      <MobileMenu
        isOpen={mobileMenuOpen}
        items={filteredItems}
        activeItem={activeItem}
        onItemClick={onItemClick}
        onClose={() => setMobileMenuOpen(false)}
      />
    </>
  );
});
```

### Step 1 Verification Checklist
- [ ] File `TopNavbar.tsx` created in `frontend/src/components/`
- [ ] No TypeScript errors in the file
- [ ] All imports resolve correctly

### Step 1 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

## Step 2: Modificar DashboardPage para usar TopNavbar

- [x] Open `frontend/src/pages/DashboardPage.tsx`
- [x] Replace the entire file content with the code below:

```tsx
import { useState, useCallback, useMemo, memo } from 'react';
import { useNavigate } from 'react-router-dom';
import { TopNavbar } from '../components/TopNavbar';
import { EmpleadosView } from '../components/dashboard/EmpleadosView';
import { MantenimientosView } from '../components/dashboard/MantenimientosView';
import { InicioView } from '../components/dashboard/InicioView';
import { PlaceholderView } from '../components/dashboard/PlaceholderView';
import { authService } from '../services/authService';

// Memoized view components to prevent unnecessary re-renders
const MemoizedEmpleadosView = memo(EmpleadosView);
const MemoizedMantenimientosView = memo(MantenimientosView);
const MemoizedInicioView = memo(InicioView);
const MemoizedPlaceholderView = memo(PlaceholderView);

export default function DashboardPage() {
  const navigate = useNavigate();
  const [activeView, setActiveView] = useState('inicio');
  
  // Memoize user info to prevent unnecessary re-computation
  const userInfo = useMemo(() => authService.getUserInfo(), []);

  const handleLogout = useCallback(() => {
    authService.logout();
    navigate('/login');
  }, [navigate]);

  // Render the appropriate view based on active menu item
  const renderView = useCallback(() => {
    // Admin only sees maintenance view
    if (userInfo.role === 'ADMIN') {
      return <MemoizedMantenimientosView />;
    }

    switch (activeView) {
      case 'inicio':
        return <MemoizedInicioView userRole={userInfo.role} />;
      case 'empleados':
        return <MemoizedEmpleadosView />;
      case 'mantenimientos':
        return <MemoizedMantenimientosView />;
      case 'mi-planilla':
        return <MemoizedPlaceholderView title="Mi Planilla" description="Consulta tu información de planilla personal" />;
      case 'mis-solicitudes':
        return <MemoizedPlaceholderView title="Mis Solicitudes" description="Gestiona tus solicitudes de permisos y vacaciones" />;
      case 'asistencia':
        return <MemoizedPlaceholderView title="Asistencia" description="Registro y consulta de asistencia" />;
      case 'horas-extra':
        return <MemoizedPlaceholderView title="Horas Extra" description="Gestión de horas extra del departamento" />;
      case 'solicitudes-pendientes':
        return <MemoizedPlaceholderView title="Solicitudes Pendientes" description="Revisa y aprueba solicitudes pendientes" />;
      case 'planilla-general':
        return <MemoizedPlaceholderView title="Planilla General" description="Gestión de planilla de todos los empleados" />;
      case 'liquidaciones':
        return <MemoizedPlaceholderView title="Liquidaciones" description="Cálculo y gestión de liquidaciones" />;
      case 'aguinaldo':
        return <MemoizedPlaceholderView title="Aguinaldo" description="Cálculo y gestión del aguinaldo" />;
      case 'reportes':
        return <MemoizedPlaceholderView title="Reportes" description="Generación de reportes del sistema" />;
      default:
        return <MemoizedInicioView userRole={userInfo.role} />;
    }
  }, [activeView, userInfo.role]);

  return (
    <div className="flex flex-col min-h-screen bg-background">
      {/* Top Navigation Bar */}
      <TopNavbar
        userRole={userInfo.role}
        username={userInfo.username}
        activeItem={activeView}
        onItemClick={setActiveView}
        onLogout={handleLogout}
      />
      
      {/* Main Content Area */}
      <main className="flex-1 p-4 md:p-6">
        {renderView()}
      </main>
    </div>
  );
}
```

### Step 2 Verification Checklist
- [ ] No TypeScript/build errors
- [ ] Navigate to dashboard in browser
- [ ] Verify horizontal navbar appears at the top
- [ ] Verify menu items show only icons, and expand with label on hover
- [ ] Verify active item stays expanded and highlighted
- [ ] Verify clicking items changes the view
- [ ] Verify user info displays on larger screens
- [ ] Verify theme toggle works
- [ ] Verify logout redirects to login

### Step 2 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

## Step 3: Eliminar componentes obsoletos

- [x] Delete the file `frontend/src/components/Sidebar.tsx`
- [x] Delete the file `frontend/src/components/DashboardHeader.tsx`

### Step 3 Verification Checklist
- [ ] Files deleted successfully
- [ ] Run `npm run build` - no errors about missing imports
- [ ] Application still works correctly

### Step 3 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

## Step 4: Verificación responsiva

- [ ] Open browser DevTools (F12)
- [ ] Toggle device toolbar (Ctrl+Shift+M)
- [ ] Test the following breakpoints:

| Breakpoint | Expected Behavior |
|------------|-------------------|
| Desktop (>1024px) | Full navbar with icons, hover expand labels, user info visible |
| Tablet (768-1024px) | Navbar visible, user info hidden, avatar shows |
| Mobile (<768px) | Hamburger menu visible, clicking opens slide-in drawer |

- [ ] Test hamburger menu:
  - [ ] Click hamburger icon - drawer slides in from left
  - [ ] Click menu item - view changes and drawer closes
  - [ ] Click backdrop - drawer closes
  - [ ] Click X button - drawer closes

### Step 4 Verification Checklist
- [ ] All breakpoints behave as expected
- [ ] Hamburger menu works correctly on mobile
- [ ] Navigation items are accessible and functional at all sizes
- [ ] Theme toggle works at all breakpoints

### Step 4 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to complete final testing, then stage and commit the change.

---

## Summary

### Files Created
| File | Description |
|------|-------------|
| `frontend/src/components/TopNavbar.tsx` | New horizontal navigation component with hover-expand icons |

### Files Modified
| File | Description |
|------|-------------|
| `frontend/src/pages/DashboardPage.tsx` | Changed layout from sidebar to top navbar |

### Files Deleted
| File | Reason |
|------|--------|
| `frontend/src/components/Sidebar.tsx` | Replaced by TopNavbar |
| `frontend/src/components/DashboardHeader.tsx` | Functionality merged into TopNavbar |

### Key Features Implemented
1. **Icon-only navigation** with smooth hover expand animation showing labels
2. **Active item** stays expanded and highlighted with primary color
3. **Mobile responsive** with hamburger menu and slide-in drawer
4. **User info section** with role badge (hidden on smaller screens)
5. **Theme toggle** integrated in navbar
6. **Logout button** integrated in navbar
7. **Role-based filtering** - each role sees only their permitted menu items
