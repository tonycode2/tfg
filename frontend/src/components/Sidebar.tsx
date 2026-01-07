import { memo, useMemo } from 'react';
import { cn } from '../lib/utils';
import type { Role } from '../services/authService';
import type { ReactElement } from 'react';

interface MenuItem {
  id: string;
  label: string;
  icon: ReactElement;
  roles: Role[];
}

interface SidebarProps {
  userRole: Role;
  activeItem: string;
  onItemClick: (id: string) => void;
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
        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
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

export const Sidebar = memo(function Sidebar({ userRole, activeItem, onItemClick }: SidebarProps) {
  // Memoize filtered menu items based on user role
  const filteredItems = useMemo(
    () => menuItems.filter((item) => item.roles.includes(userRole)),
    [userRole]
  );

  // Memoize role display text
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
    <aside className="w-64 bg-card border-r border-border min-h-screen">
      <div className="p-6 border-b border-border">
        <h1 className="text-xl font-bold text-foreground">Sistema RH</h1>
        <p className="text-sm text-muted-foreground mt-1">{roleText}</p>
      </div>
      
      <nav className="p-4" aria-label="Menú principal">
        <ul className="space-y-2">
          {filteredItems.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => onItemClick(item.id)}
                className={cn(
                  'w-full flex items-center gap-3 px-4 py-3 rounded-lg text-sm font-medium transition-colors',
                  activeItem === item.id
                    ? 'bg-primary text-primary-foreground'
                    : 'text-muted-foreground hover:bg-accent hover:text-accent-foreground'
                )}
                aria-current={activeItem === item.id ? 'page' : undefined}
              >
                {item.icon}
                <span>{item.label}</span>
              </button>
            </li>
          ))}
        </ul>
      </nav>
    </aside>
  );
});
