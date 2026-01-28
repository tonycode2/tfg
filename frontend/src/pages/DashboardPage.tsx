import { useState, useCallback, useMemo, memo } from 'react';
import { useNavigate } from 'react-router-dom';
import { TopNavbar } from '../components/TopNavbar';
import { EmpleadosView } from '../components/dashboard/EmpleadosView';
import EvaluacionesView from '../components/dashboard/EvaluacionesView';
import { MantenimientosView } from '../components/dashboard/MantenimientosView';
import { InicioView } from '../components/dashboard/InicioView';
import { PlaceholderView } from '../components/dashboard/PlaceholderView';
import { AsistenciaView } from '../components/dashboard/AsistenciaView';
import PermisosView from '../components/dashboard/PermisosView';
import PermisosSolicitudesPendientesView from '../components/dashboard/PermisosSolicitudesPendientesView';
import PermisosRHView from '../components/dashboard/PermisosRHView';
import IncapacidadesView from '../components/dashboard/IncapacidadesView';
import IncapacidadesPendientesView from '../components/dashboard/IncapacidadesPendientesView';
import GestionIncapacidadesView from '../components/dashboard/GestionIncapacidadesView';
import { DiasFeriadosView } from '../components/dashboard/DiasFeriadosView';
import HorasExtraView from '../components/dashboard/HorasExtraView';
import { authService } from '../services/authService';

// Memoized view components to prevent unnecessary re-renders
const MemoizedEmpleadosView = memo(EmpleadosView);
const MemoizedMantenimientosView = memo(MantenimientosView);
const MemoizedInicioView = memo(InicioView);
const MemoizedPlaceholderView = memo(PlaceholderView);
const MemoizedAsistenciaView = memo(AsistenciaView);
const MemoizedPermisosView = memo(PermisosView);
const MemoizedPermisosSolicitudesPendientesView = memo(PermisosSolicitudesPendientesView);
const MemoizedPermisosRHView = memo(PermisosRHView);
const MemoizedIncapacidadesView = memo(IncapacidadesView);
const MemoizedIncapacidadesPendientesView = memo(IncapacidadesPendientesView);
const MemoizedGestionIncapacidadesView = memo(GestionIncapacidadesView);
const MemoizedDiasFeriadosView = memo(DiasFeriadosView);
const MemoizedEvaluacionesView = memo(EvaluacionesView);
const MemoizedHorasExtraView = memo(HorasExtraView);

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
      case 'mis-permisos':
        return <MemoizedPermisosView />;
      case 'solicitudes-pendientes-permisos':
        return <MemoizedPermisosSolicitudesPendientesView />;
      case 'gestion-permisos':
        return <MemoizedPermisosRHView />;
      case 'mis-incapacidades':
        return <MemoizedIncapacidadesView />;
      case 'solicitudes-pendientes-incapacidades':
        return <MemoizedIncapacidadesPendientesView />;
      case 'gestion-incapacidades':
        return <MemoizedGestionIncapacidadesView />;
      case 'dias-feriados':
        return <MemoizedDiasFeriadosView />;
      case 'asistencia':
        return <MemoizedAsistenciaView />;
      case 'evaluaciones':
        return <MemoizedEvaluacionesView />;
      case 'horas-extra':
        return <MemoizedHorasExtraView />;
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
        username={userInfo.nombreCompleto || userInfo.username}
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

