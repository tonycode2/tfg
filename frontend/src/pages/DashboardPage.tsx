import { useState, useEffect, useMemo, useCallback, memo } from 'react';
import { Sidebar } from '@/components/Sidebar';
import { DashboardHeader } from '@/components/DashboardHeader';
import { InicioView } from '@/components/dashboard/InicioView';
import { PlaceholderView } from '@/components/dashboard/PlaceholderView';
import { MantenimientosView } from '@/components/dashboard/MantenimientosView';
import { EmpleadosView } from '@/components/dashboard/EmpleadosView';
import { authService } from '@/services/authService';
import type { Role } from '@/services/authService';

// Memoized PlaceholderView to prevent unnecessary re-renders
const MemoizedPlaceholderView = memo(PlaceholderView);
const MemoizedInicioView = memo(InicioView);
const MemoizedEmpleadosView = memo(EmpleadosView);
const MemoizedMantenimientosView = memo(MantenimientosView);

export default function DashboardPage() {
  const [activeView, setActiveView] = useState('inicio');
  
  // Memoize user info since it doesn't change after initial load
  const userInfo = useMemo(() => authService.getUserInfo(), []);

  useEffect(() => {
    document.title = 'Dashboard - Sistema de RH';
  }, []);

  // Memoize the view rendering to avoid recreating components
  const renderView = useCallback(() => {
    switch (activeView) {
      case 'inicio':
        return <MemoizedInicioView userRole={userInfo.role} />;
      case 'mi-planilla':
        return (
          <MemoizedPlaceholderView
            title="Mi Planilla"
            description="Consulta tu información de planilla y pagos"
          />
        );
      case 'mis-solicitudes':
        return (
          <MemoizedPlaceholderView
            title="Mis Solicitudes"
            description="Gestiona tus solicitudes de vacaciones, permisos y más"
          />
        );
      case 'asistencia':
        return (
          <MemoizedPlaceholderView
            title="Asistencia"
            description="Control de asistencia de empleados"
          />
        );
      case 'horas-extra':
        return (
          <MemoizedPlaceholderView
            title="Horas Extra"
            description="Gestión de horas extra y solicitudes"
          />
        );
      case 'solicitudes-pendientes':
        return (
          <MemoizedPlaceholderView
            title="Solicitudes Pendientes"
            description="Aprueba o rechaza solicitudes de tus empleados"
          />
        );
      case 'planilla-general':
        return (
          <MemoizedPlaceholderView
            title="Planilla General"
            description="Administra la planilla de todos los empleados"
          />
        );
      case 'liquidaciones':
        return (
          <MemoizedPlaceholderView
            title="Liquidaciones"
            description="Procesa liquidaciones de empleados"
          />
        );
      case 'aguinaldo':
        return (
          <MemoizedPlaceholderView
            title="Aguinaldo"
            description="Gestión de aguinaldos y bonificaciones"
          />
        );
      case 'reportes':
        return (
          <MemoizedPlaceholderView
            title="Reportes"
            description="Genera reportes y análisis del sistema"
          />
        );
      case 'empleados':
        return <MemoizedEmpleadosView />;
      case 'mantenimientos':
        return <MemoizedMantenimientosView />;
      default:
        return <MemoizedInicioView userRole={userInfo.role} />;
    }
  }, [activeView, userInfo.role]);

  return (
    <div className="flex min-h-screen bg-background">
      <Sidebar
        userRole={userInfo.role}
        activeItem={activeView}
        onItemClick={setActiveView}
      />
      
      <div className="flex-1 flex flex-col">
        <DashboardHeader username={userInfo.username} />
        
        <main className="flex-1 p-6">
          {renderView()}
        </main>
      </div>
    </div>
  );
}

