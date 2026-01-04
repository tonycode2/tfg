import { useState, useEffect } from 'react';
import { Sidebar } from '@/components/Sidebar';
import { DashboardHeader } from '@/components/DashboardHeader';
import { InicioView } from '@/components/dashboard/InicioView';
import { PlaceholderView } from '@/components/dashboard/PlaceholderView';
import { MantenimientosView } from '@/components/dashboard/MantenimientosView';
import { authService } from '@/services/authService';
import type { Role } from '@/services/authService';

export default function DashboardPage() {
  const [activeView, setActiveView] = useState('inicio');
  const [userInfo, setUserInfo] = useState<{ username: string; role: Role }>({
    username: 'Usuario',
    role: 'EMPLEADO',
  });

  useEffect(() => {
    // Obtener información del usuario desde el JWT
    const info = authService.getUserInfo();
    setUserInfo(info);
  }, []);

  const renderView = () => {
    switch (activeView) {
      case 'inicio':
        return <InicioView userRole={userInfo.role} />;
      case 'mi-planilla':
        return (
          <PlaceholderView
            title="Mi Planilla"
            description="Consulta tu información de planilla y pagos"
          />
        );
      case 'mis-solicitudes':
        return (
          <PlaceholderView
            title="Mis Solicitudes"
            description="Gestiona tus solicitudes de vacaciones, permisos y más"
          />
        );
      case 'asistencia':
        return (
          <PlaceholderView
            title="Asistencia"
            description="Control de asistencia de empleados"
          />
        );
      case 'horas-extra':
        return (
          <PlaceholderView
            title="Horas Extra"
            description="Gestión de horas extra y solicitudes"
          />
        );
      case 'solicitudes-pendientes':
        return (
          <PlaceholderView
            title="Solicitudes Pendientes"
            description="Aprueba o rechaza solicitudes de tus empleados"
          />
        );
      case 'planilla-general':
        return (
          <PlaceholderView
            title="Planilla General"
            description="Administra la planilla de todos los empleados"
          />
        );
      case 'liquidaciones':
        return (
          <PlaceholderView
            title="Liquidaciones"
            description="Procesa liquidaciones de empleados"
          />
        );
      case 'aguinaldo':
        return (
          <PlaceholderView
            title="Aguinaldo"
            description="Gestión de aguinaldos y bonificaciones"
          />
        );
      case 'reportes':
        return (
          <PlaceholderView
            title="Reportes"
            description="Genera reportes y análisis del sistema"
          />
        );
      case 'mantenimientos':
        return <MantenimientosView />;
      default:
        return <InicioView userRole={userInfo.role} />;
    }
  };

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

