import { memo, useMemo, useState } from 'react';
import { cn } from '../lib/utils';
import { Button } from './ui/button';
import { Popover, PopoverTrigger, PopoverContent } from './ui/popover';
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
    id: 'mis-permisos',
    label: 'Mis Solicitudes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'mis-incapacidades',
    label: 'Mis Incapacidades',
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
    id: 'mis-horas-extra',
    label: 'Mis Horas Extra',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
      </svg>
    ),
    roles: ['HR', 'JEFE', 'EMPLEADO'],
  },
  {
    id: 'horas-extra-pendientes',
    label: 'Horas Extra Pendientes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2" />
      </svg>
    ),
    roles: ['JEFE', 'HR'],
  },
  {
    id: 'solicitudes-pendientes-permisos',
    label: 'Solicitudes Pendientes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4" />
      </svg>
    ),
    roles: ['JEFE'],
  },
  {
    id: 'solicitudes-pendientes-incapacidades',
    label: 'Incapacidades Pendientes',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
      </svg>
    ),
    roles: ['JEFE'],
  },
  {
    id: 'evaluaciones',
    label: 'Evaluaciones',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m-6-8h6M5 7h14v10H5z" />
      </svg>
    ),
    roles: ['JEFE'],
  },
  {
    id: 'gestion-permisos',
    label: 'Gestión de Permisos',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-3 7h3m-3 4h3m-6-4h.01M9 16h.01" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'gestion-incapacidades',
    label: 'Gestión de Incapacidades',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 14l-7 7m0 0l-7-7m7 7V3" />
      </svg>
    ),
    roles: ['HR'],
  },
  {
    id: 'dias-feriados',
    label: 'Días Feriados',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
      </svg>
    ),
    roles: ['HR'],
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
    id: 'configuracion-renta',
    label: 'Config. Renta',
    icon: (
      <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
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
    roles: ['HR'],
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

// NavItem removed: replaced by simplified text-only buttons and popovers

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
  const [misServiciosOpen, setMisServiciosOpen] = useState(false);
  const [rhOpen, setRhOpen] = useState(false);
  const [administrativoOpen, setAdministrativoOpen] = useState(false);

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
      <header className="sticky top-0 z-30 w-full bg-card border-b border-border overflow-visible">
        <div className="flex items-center justify-between h-16 px-4 md:px-6 pr-4">
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
              <img
                src={theme === 'light' ? '/logo.png' : '/logo_invertido.png'}
                alt="Logo Sastrería Gerson Andre"
                className="h-10 w-auto hidden sm:block"
              />
              <div className="w-8 h-8 bg-primary rounded-lg flex items-center justify-center sm:hidden">
                <span className="text-primary-foreground font-bold text-sm">GA</span>
              </div>
              <span className="font-semibold text-foreground hidden sm:block">Gerson Andre</span>
            </div>
          </div>

          {/* Center: Simplified navigation (hidden on mobile) */}
          <nav className="hidden md:flex items-center gap-4">
            {userRole === 'ADMIN' ? (
              <button
                onClick={() => onItemClick('mantenimientos')}
                className={cn(
                  'rounded-lg px-3 py-2 transition-colors',
                  activeItem === 'mantenimientos'
                    ? 'bg-primary text-primary-foreground'
                    : 'text-foreground hover:bg-accent'
                )}
              >
                Mantenimientos y Consultas
              </button>
            ) : (
              <>
                <button
                  onClick={() => onItemClick('inicio')}
                  className={cn(
                    'rounded-lg px-3 py-2 transition-colors',
                    activeItem === 'inicio'
                      ? 'bg-primary text-primary-foreground'
                      : 'text-foreground hover:bg-accent'
                  )}
                >
                  Inicio
                </button>

                {/* Mis Servicios dropdown */}
                <Popover open={misServiciosOpen} onOpenChange={setMisServiciosOpen}>
                  <PopoverTrigger asChild>
                    <button className="rounded-lg px-3 py-2 text-foreground hover:bg-accent flex items-center gap-2">
                      <span>Mis Servicios</span>
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                      </svg>
                    </button>
                  </PopoverTrigger>
                  <PopoverContent align="start" className="p-2">
                    <div className="flex flex-col">
                      <button
                        onClick={() => {
                          setMisServiciosOpen(false);
                          setTimeout(() => onItemClick('mi-planilla'), 120);
                        }}
                        className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'mi-planilla' && 'bg-primary text-primary-foreground')}
                      >
                        Mi Planilla
                      </button>
                      <button
                        onClick={() => {
                          setMisServiciosOpen(false);
                          setTimeout(() => onItemClick('mis-permisos'), 120);
                        }}
                        className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'mis-permisos' && 'bg-primary text-primary-foreground')}
                      >
                        Mis Solicitudes
                      </button>
                      <button
                        onClick={() => {
                          setMisServiciosOpen(false);
                          setTimeout(() => onItemClick('mis-incapacidades'), 120);
                        }}
                        className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'mis-incapacidades' && 'bg-primary text-primary-foreground')}
                      >
                        Mis Incapacidades
                      </button>
                      <button
                        onClick={() => {
                          setMisServiciosOpen(false);
                          setTimeout(() => onItemClick('mis-horas-extra'), 120);
                        }}
                        className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'mis-horas-extra' && 'bg-primary text-primary-foreground')}
                      >
                        Mis Horas Extra
                      </button>
                    </div>
                  </PopoverContent>
                </Popover>

                {/* RH dropdown - visible only for HR users */}
                {userRole === 'HR' && (
                  <Popover open={rhOpen} onOpenChange={setRhOpen}>
                    <PopoverTrigger asChild>
                      <button className="rounded-lg px-3 py-2 text-foreground hover:bg-accent flex items-center gap-2">
                        <span>RH</span>
                        <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                    </PopoverTrigger>
                    <PopoverContent align="start" className="p-2">
                      <div className="flex flex-col">
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('gestion-permisos'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'gestion-permisos' && 'bg-primary text-primary-foreground')}
                        >
                          Gestión de Permisos
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('gestion-incapacidades'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'gestion-incapacidades' && 'bg-primary text-primary-foreground')}
                        >
                          Gestión de Incapacidades
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('dias-feriados'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'dias-feriados' && 'bg-primary text-primary-foreground')}
                        >
                          Días Feriados
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('planilla-general'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'planilla-general' && 'bg-primary text-primary-foreground')}
                        >
                          Planilla General
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('configuracion-renta'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'configuracion-renta' && 'bg-primary text-primary-foreground')}
                        >
                          Configuración de Renta
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('liquidaciones'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'liquidaciones' && 'bg-primary text-primary-foreground')}
                        >
                          Liquidaciones
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('aguinaldo'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'aguinaldo' && 'bg-primary text-primary-foreground')}
                        >
                          Aguinaldo
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('reportes'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'reportes' && 'bg-primary text-primary-foreground')}
                        >
                          Reportes
                        </button>
                        <button
                          onClick={() => {
                            setRhOpen(false);
                            setTimeout(() => onItemClick('empleados'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'empleados' && 'bg-primary text-primary-foreground')}
                        >
                          Empleados
                        </button>
                      </div>
                    </PopoverContent>
                  </Popover>
                )}

                {/* Administrativo dropdown - visible only for JEFE and ADMIN (hide for EMPLEADO and HR) */}
                {(userRole === 'JEFE' || userRole === 'ADMIN') && (
                  <Popover open={administrativoOpen} onOpenChange={setAdministrativoOpen}>
                    <PopoverTrigger asChild>
                      <button className="rounded-lg px-3 py-2 text-foreground hover:bg-accent flex items-center gap-2"> 
                        <span>Administrativo</span>
                        <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 9l-7 7-7-7" />
                        </svg>
                      </button>
                    </PopoverTrigger>
                    <PopoverContent align="start" className="p-2">
                      <div className="flex flex-col">
                        <button
                          onClick={() => {
                            setAdministrativoOpen(false);
                            setTimeout(() => onItemClick('horas-extra-pendientes'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'horas-extra-pendientes' && 'bg-primary text-primary-foreground')}
                        >
                          Horas Extra Pendientes
                        </button>
                        <button
                          onClick={() => {
                            setAdministrativoOpen(false);
                            setTimeout(() => onItemClick('solicitudes-pendientes-permisos'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'solicitudes-pendientes-permisos' && 'bg-primary text-primary-foreground')}
                        >
                          Solicitudes Pendientes
                        </button>
                        <button
                          onClick={() => {
                            setAdministrativoOpen(false);
                            setTimeout(() => onItemClick('solicitudes-pendientes-incapacidades'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'solicitudes-pendientes-incapacidades' && 'bg-primary text-primary-foreground')}
                        >
                          Incapacidades Pendientes
                        </button>
                        <button
                          onClick={() => {
                            setAdministrativoOpen(false);
                            setTimeout(() => onItemClick('evaluaciones'), 120);
                          }}
                          className={cn('text-foreground text-left px-2 py-2 rounded hover:bg-accent', activeItem === 'evaluaciones' && 'bg-primary text-primary-foreground')}
                        >
                          Evaluaciones
                        </button>
                      </div>
                    </PopoverContent>
                  </Popover>
                )}

                {/* Asistencia (to the side of dropdowns) */}
                <button
                  onClick={() => onItemClick('asistencia')}
                  className={cn(
                    'rounded-lg px-3 py-2 transition-colors',
                    activeItem === 'asistencia'
                      ? 'bg-primary text-primary-foreground'
                      : 'text-foreground hover:bg-accent'
                  )}
                >
                  Asistencia
                </button>
              </>
            )}
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
