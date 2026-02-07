import { useState, useCallback, useMemo, memo, useEffect } from 'react';
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
import { JornadaDiariaView } from '../components/dashboard/JornadaDiariaView';
import HorasExtraView from '../components/dashboard/HorasExtraView';
import MisHorasExtraView from '../components/dashboard/MisHorasExtraView';
import HorasExtraPendientesView from '../components/dashboard/HorasExtraPendientesView';
import { MiPlanillaView } from '../components/dashboard/MiPlanillaView';
import { PlanillaGeneralView } from '../components/dashboard/PlanillaGeneralView';
import { ConfiguracionRentaView } from '../components/dashboard/ConfiguracionRentaView';
import { AguinaldoView } from '../components/dashboard/AguinaldoView';
import { LiquidacionesView } from '../components/dashboard/LiquidacionesView';
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
const MemoizedJornadaDiariaView = memo(JornadaDiariaView);
const MemoizedEvaluacionesView = memo(EvaluacionesView);
const MemoizedHorasExtraView = memo(HorasExtraView);
const MemoizedMisHorasExtraView = memo(MisHorasExtraView);
const MemoizedHorasExtraPendientesView = memo(HorasExtraPendientesView);
const MemoizedMiPlanillaView = memo(MiPlanillaView);
const MemoizedPlanillaGeneralView = memo(PlanillaGeneralView);
const MemoizedConfiguracionRentaView = memo(ConfiguracionRentaView);
const MemoizedAguinaldoView = memo(AguinaldoView);
const MemoizedLiquidacionesView = memo(LiquidacionesView);

export default function DashboardPage() {
  const navigate = useNavigate();
  const [activeView, setActiveView] = useState('inicio');
  const [contentHidden, setContentHidden] = useState(false);
  
  // Memoize user info to prevent unnecessary re-computation
  const userInfo = useMemo(() => authService.getUserInfo(), []);

  const handleLogout = useCallback(() => {
    authService.logout();
    navigate('/login');
  }, [navigate]);

  const viewTitles = useMemo<Record<string, string>>(
    () => ({
      inicio: 'Inicio',
      empleados: 'Empleados',
      mantenimientos: 'Mantenimientos y Consultas',
      'mi-planilla': 'Mi Planilla',
      'mis-solicitudes': 'Mis Solicitudes',
      'mis-permisos': 'Mis Solicitudes',
      'solicitudes-pendientes-permisos': 'Solicitudes Pendientes',
      'gestion-permisos': 'Gestión de Permisos',
      'mis-incapacidades': 'Mis Incapacidades',
      'solicitudes-pendientes-incapacidades': 'Incapacidades Pendientes',
      'gestion-incapacidades': 'Gestión de Incapacidades',
      'dias-feriados': 'Días Feriados',
      'jornada-diaria': 'Jornada Diaria',
      asistencia: 'Asistencia',
      evaluaciones: 'Evaluaciones',
      'horas-extra': 'Horas Extra',
      'mis-horas-extra': 'Mis Horas Extra',
      'horas-extra-pendientes': 'Horas Extra Pendientes',
      'solicitudes-pendientes': 'Solicitudes Pendientes',
      'planilla-general': 'Planilla General',
      'configuracion-renta': 'Configuración de Renta',
      liquidaciones: 'Liquidaciones',
      aguinaldo: 'Aguinaldo',
      reportes: 'Reportes',
    }),
    []
  );

  useEffect(() => {
    const baseTitle = 'Sistema de RH';
    const resolvedTitle =
      userInfo.role === 'ADMIN'
        ? 'Mantenimientos y Consultas'
        : viewTitles[activeView] || 'Panel de Control';
    document.title = `${resolvedTitle} - ${baseTitle}`;
  }, [activeView, userInfo.role, viewTitles]);

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
        return <MemoizedMiPlanillaView />;
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
      case 'jornada-diaria':
        return <MemoizedJornadaDiariaView />;
      case 'asistencia':
        return <MemoizedAsistenciaView />;
      case 'evaluaciones':
        return <MemoizedEvaluacionesView />;
      case 'horas-extra':
        return <MemoizedHorasExtraView />;
      case 'mis-horas-extra':
        return <MemoizedMisHorasExtraView />;
      case 'horas-extra-pendientes':
        return <MemoizedHorasExtraPendientesView />;
      case 'solicitudes-pendientes':
        return <MemoizedPlaceholderView title="Solicitudes Pendientes" description="Revisa y aprueba solicitudes pendientes" />;
      case 'planilla-general':
        return <MemoizedPlanillaGeneralView />;
      case 'configuracion-renta':
        return <MemoizedConfiguracionRentaView />;
      case 'liquidaciones':
        return <MemoizedLiquidacionesView />;
      case 'aguinaldo':
        return <MemoizedAguinaldoView />;
      case 'reportes':
        return <MemoizedPlaceholderView title="Reportes" description="Generación de reportes del sistema" />;
      default:
        return <MemoizedInicioView userRole={userInfo.role} />;
    }
  }, [activeView, userInfo.role]);

  // Intercept view changes to animate content transition
  const handleSetActiveView = useCallback((view: string) => {
    // fade out
    setContentHidden(true);
    setTimeout(() => {
      setActiveView(view);
      // fade in
      setContentHidden(false);
    }, 150);
  }, []);

  return (
    <div className="flex flex-col min-h-screen bg-background">
      {/* Top Navigation Bar */}
      <TopNavbar
        userRole={userInfo.role}
        username={userInfo.nombreCompleto || userInfo.username}
        activeItem={activeView}
        onItemClick={handleSetActiveView}
        onLogout={handleLogout}
      />
      
      {/* Main Content Area */}
      <main className={"flex-1 p-4 md:p-6 transition-opacity duration-150 " + (contentHidden ? 'opacity-0' : 'opacity-100')}>
        {renderView()}
      </main>
    </div>
  );
}

