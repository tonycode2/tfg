import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Modal } from '@/components/Modal';
import type { RespuestaIncapacidad } from '@/services/incapacidadesService';
import { 
  obtenerSolicitudesParaRH,
  obtenerTodasLasSolicitudes,
  obtenerIncapacidadesActivas,
  aprobarPorRH,
  rechazarPorRH,
  cancelarSolicitud
} from '@/services/incapacidadesService';
import { formatearFecha } from '../../lib/utils';
import { FileText, User, Eye, CheckCircle, XCircle, Clock, Filter, Ban, Activity, AlertCircle } from 'lucide-react';
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from '@/components/ui/select';
import { toast } from 'sonner';

const TIPOS_INCAPACIDAD: Record<string, string> = {
  'ENFERMEDAD_COMUN': 'Enfermedad Común',
  'ACCIDENTE_LABORAL': 'Accidente Laboral',
  'ACCIDENTE_TRANSITO': 'Accidente de Tránsito',
  'MATERNIDAD': 'Maternidad',
  'RIESGO_EMBARAZO': 'Riesgo de Embarazo',
  'ENFERMEDAD_PROFESIONAL': 'Enfermedad Profesional',
  'LICENCIA_DE_PATERNIDAD': 'Licencia de paternidad',
};

const ENTIDADES_EMISORAS: Record<string, string> = {
  'CCSS': 'CCSS',
  'INS': 'INS',
  'CLINICA_PRIVADA': 'Clínica Privada',
  'OTRO': 'Otro',
};

const ESTADOS_INCAPACIDAD: Record<string, string> = {
  'PENDIENTE': 'Pendiente',
  'PENDIENTE_RH': 'Pendiente RH',
  'APROBADA_POR_JEFE': 'Aprobada por Jefe',
  'RECHAZADA_POR_JEFE': 'Rechazada por Jefe',
  'RECHAZADA_POR_RH': 'Rechazada por RH',
  'APROBADA': 'Aprobada',
  'CANCELADA': 'Cancelada',
};

const getTipoIncapacidadLabel = (tipo: string) => TIPOS_INCAPACIDAD[tipo] || tipo;
const getEntidadEmisoraLabel = (entidad: string) => ENTIDADES_EMISORAS[entidad] || entidad;
const getEstadoLabel = (estado: string) => ESTADOS_INCAPACIDAD[estado] || estado;

const getEstadoColor = (estado: string): string => {
  switch (estado) {
    case 'PENDIENTE':
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200';
    case 'PENDIENTE_RH':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-200';
    case 'APROBADA_POR_JEFE':
      return 'bg-indigo-100 text-indigo-800 dark:bg-indigo-900 dark:text-indigo-200';
    case 'APROBADA':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-200';
    case 'RECHAZADA_POR_JEFE':
    case 'RECHAZADA_POR_RH':
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-200';
    case 'CANCELADA':
      return 'bg-gray-100 text-gray-800 dark:bg-gray-900 dark:text-gray-200';
    default:
      return 'bg-gray-100 text-gray-800';
  }
};

export default function GestionIncapacidadesView() {
  const [solicitudesPendientes, setSolicitudesPendientes] = useState<RespuestaIncapacidad[]>([]);
  const [todasLasSolicitudes, setTodasLasSolicitudes] = useState<RespuestaIncapacidad[]>([]);
  const [incapacidadesActivas, setIncapacidadesActivas] = useState<RespuestaIncapacidad[]>([]);
  const [vistaActual, setVistaActual] = useState<'pendientes' | 'todas' | 'activas'>('pendientes');
  const [loading, setLoading] = useState(true);
  const [showRevisarModal, setShowRevisarModal] = useState(false);
  const [showDetalleModal, setShowDetalleModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaIncapacidad | null>(null);
  const [comentarios, setComentarios] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  
  // Filtros
  const [filtroEstado, setFiltroEstado] = useState<string>('');
  const [filtroTipo, setFiltroTipo] = useState<string>('');
  const [filtroEmpleado, setFiltroEmpleado] = useState<string>('');

  useEffect(() => {
    cargarDatos();
  }, [vistaActual]);

  const cargarDatos = async () => {
    try {
      setLoading(true);
      if (vistaActual === 'pendientes') {
        const data = await obtenerSolicitudesParaRH();
        setSolicitudesPendientes(data);
      } else if (vistaActual === 'todas') {
        const data = await obtenerTodasLasSolicitudes();
        setTodasLasSolicitudes(data);
      } else {
        const data = await obtenerIncapacidadesActivas();
        setIncapacidadesActivas(data);
      }
      setError(null);
    } catch (err: unknown) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  };

  const solicitudesFiltradas = () => {
    let lista: RespuestaIncapacidad[];
    if (vistaActual === 'pendientes') {
      lista = solicitudesPendientes;
    } else if (vistaActual === 'todas') {
      lista = todasLasSolicitudes;
    } else {
      lista = incapacidadesActivas;
    }
    
    return lista.filter(solicitud => {
      if (filtroEstado && solicitud.estadoSolicitud !== filtroEstado) return false;
      if (filtroTipo && solicitud.tipoIncapacidad !== filtroTipo) return false;
      if (filtroEmpleado) {
        const nombreCompleto = `${solicitud.nombreEmpleado} ${solicitud.primerApellidoEmpleado} ${solicitud.segundoApellidoEmpleado}`.toLowerCase();
        if (!nombreCompleto.includes(filtroEmpleado.toLowerCase())) return false;
      }
      return true;
    });
  };

  const handleRevisar = (solicitud: RespuestaIncapacidad) => {
    setSolicitudSeleccionada(solicitud);
    setComentarios('');
    setShowRevisarModal(true);
  };

  const handleVerDetalle = (solicitud: RespuestaIncapacidad) => {
    setSolicitudSeleccionada(solicitud);
    setShowDetalleModal(true);
  };

  const handleAprobarRH = async () => {
    if (!solicitudSeleccionada) return;

    try {
      setProcesando(true);
      await aprobarPorRH(solicitudSeleccionada.id, { comentarios });
      toast.success('Incapacidad aprobada exitosamente');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarDatos();
    } catch (err: unknown) {
      console.error('Error al aprobar incapacidad:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al aprobar la incapacidad';
      toast.error(errorMessage);
    } finally {
      setProcesando(false);
    }
  };

  const handleRechazarRH = async () => {
    if (!solicitudSeleccionada) return;

    if (!comentarios || comentarios.trim().length < 10) {
      toast.error('Por favor, proporcione comentarios (mínimo 10 caracteres)');
      return;
    }

    if (!confirm('¿Está seguro de que desea rechazar esta incapacidad?')) {
      return;
    }

    try {
      setProcesando(true);
      await rechazarPorRH(solicitudSeleccionada.id, { comentarios });
      toast.success('Incapacidad rechazada');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarDatos();
    } catch (err: unknown) {
      console.error('Error al rechazar incapacidad:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al rechazar la incapacidad';
      toast.error(errorMessage);
    } finally {
      setProcesando(false);
    }
  };

  const handleCancelar = async (solicitud: RespuestaIncapacidad) => {
    if (!confirm('¿Está seguro de que desea cancelar esta incapacidad aprobada?')) {
      return;
    }

    try {
      await cancelarSolicitud(solicitud.id);
      toast.success('Incapacidad cancelada exitosamente');
      cargarDatos();
    } catch (err: unknown) {
      console.error('Error al cancelar incapacidad:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al cancelar la incapacidad';
      toast.error(errorMessage);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Cargando solicitudes...</div>
      </div>
    );
  }

  const solicitudesMostrar = solicitudesFiltradas();

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Gestión de Incapacidades - RH</h2>
        <p className="text-muted-foreground">
          Aprobación final de incapacidades y auditoría completa
        </p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          {error}
        </div>
      )}

      {/* Selector de Vista */}
      <div className="flex gap-2">
        <Button
          variant={vistaActual === 'pendientes' ? 'default' : 'outline'}
          onClick={() => setVistaActual('pendientes')}
          className="gap-2"
        >
          <Clock className="h-4 w-4" />
          Pendientes RH ({solicitudesPendientes.length})
        </Button>
        <Button
          variant={vistaActual === 'activas' ? 'default' : 'outline'}
          onClick={() => setVistaActual('activas')}
          className="gap-2"
        >
          <Activity className="h-4 w-4" />
          Activas ({incapacidadesActivas.length})
        </Button>
        <Button
          variant={vistaActual === 'todas' ? 'default' : 'outline'}
          onClick={() => setVistaActual('todas')}
          className="gap-2"
        >
          <FileText className="h-4 w-4" />
          Todas ({todasLasSolicitudes.length})
        </Button>
      </div>

      {/* Filtros */}
      {(vistaActual === 'todas' || vistaActual === 'activas') && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Filter className="h-4 w-4" />
              Filtros
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-4">
              {vistaActual === 'todas' && (
                <div>
                  <Label htmlFor="filtroEstado">Estado</Label>
                  <Select value={filtroEstado === '' ? '__ALL__' : filtroEstado} onValueChange={(v) => setFiltroEstado(v === '__ALL__' ? '' : String(v))}>
                    <SelectTrigger className="w-full">
                      <SelectValue />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="__ALL__">Todos</SelectItem>
                      <SelectItem value="PENDIENTE">Pendiente</SelectItem>
                      <SelectItem value="PENDIENTE_RH">Pendiente RH</SelectItem>
                      <SelectItem value="APROBADA_POR_JEFE">Aprobada por Jefe</SelectItem>
                      <SelectItem value="APROBADA">Aprobada</SelectItem>
                      <SelectItem value="RECHAZADA_POR_JEFE">Rechazada por Jefe</SelectItem>
                      <SelectItem value="RECHAZADA_POR_RH">Rechazada por RH</SelectItem>
                      <SelectItem value="CANCELADA">Cancelada</SelectItem>
                    </SelectContent>
                  </Select>
                </div>
              )}
              <div>
                <Label htmlFor="filtroTipo">Tipo de Incapacidad</Label>
                <Select value={filtroTipo === '' ? '__ALL__' : filtroTipo} onValueChange={(v) => setFiltroTipo(v === '__ALL__' ? '' : String(v))}>
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__ALL__">Todos</SelectItem>
                    {Object.entries(TIPOS_INCAPACIDAD).map(([key, label]) => (
                      <SelectItem key={key} value={key}>{label}</SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <div>
                <Label htmlFor="filtroEmpleado">Empleado</Label>
                <Input
                  id="filtroEmpleado"
                  type="text"
                  value={filtroEmpleado}
                  onChange={(e) => setFiltroEmpleado(e.target.value)}
                  placeholder="Buscar por nombre..."
                />
              </div>
            </div>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>
            {vistaActual === 'pendientes' ? 'Incapacidades Pendientes de Aprobación' : 
             vistaActual === 'activas' ? 'Incapacidades Activas' : 'Historial Completo'}
          </CardTitle>
          <CardDescription>
            {solicitudesMostrar.length} incapacidad(es) 
            {(filtroEstado || filtroTipo || filtroEmpleado) && ' (filtradas)'}
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left p-3">Empleado</th>
                  <th className="text-left p-3">Tipo</th>
                  <th className="text-left p-3">Fechas</th>
                  <th className="text-center p-3">Días</th>
                  <th className="text-left p-3">Entidad</th>
                  <th className="text-left p-3">Estado</th>
                  {vistaActual === 'pendientes' && <th className="text-left p-3">Aprobador Jefe</th>}
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudesMostrar.length === 0 ? (
                  <tr>
                    <td colSpan={vistaActual === 'pendientes' ? 8 : 7} className="text-center p-8 text-muted-foreground">
                      No hay incapacidades
                    </td>
                  </tr>
                ) : (
                  solicitudesMostrar.map((solicitud) => (
                    <tr key={solicitud.id} className="border-b hover:bg-muted/50">
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <User className="h-4 w-4 text-muted-foreground" />
                          <div>
                            <div className="font-medium text-sm">
                              {solicitud.nombreEmpleado} {solicitud.primerApellidoEmpleado}
                            </div>
                            <div className="text-xs text-muted-foreground">
                              {solicitud.segundoApellidoEmpleado}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3 text-sm">
                        {getTipoIncapacidadLabel(solicitud.tipoIncapacidad)}
                      </td>
                      <td className="p-3">
                        <div className="text-sm">
                          <div>{formatearFecha(solicitud.fechaInicio)}</div>
                          <div className="text-muted-foreground text-xs">hasta</div>
                          <div>{formatearFecha(solicitud.fechaFin)}</div>
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        <span className="font-semibold">{solicitud.diasTotales}</span>
                      </td>
                      <td className="p-3 text-sm">
                        {getEntidadEmisoraLabel(solicitud.entidadEmisora)}
                      </td>
                      <td className="p-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoColor(solicitud.estadoSolicitud)}`}>
                          {getEstadoLabel(solicitud.estadoSolicitud)}
                        </span>
                      </td>
                      {vistaActual === 'pendientes' && (
                        <td className="p-3">
                          {solicitud.nombreAprobadorJefe ? (
                            <div className="text-sm">
                              <div className="font-medium">{solicitud.nombreAprobadorJefe}</div>
                              <div className="text-xs text-muted-foreground">
                                {solicitud.primerApellidoAprobadorJefe}
                              </div>
                            </div>
                          ) : (
                            <span className="text-muted-foreground text-sm">Directo a RH</span>
                          )}
                        </td>
                      )}
                      <td className="p-3 text-center">
                        <div className="flex gap-1 justify-center">
                          {(solicitud.estadoSolicitud === 'APROBADA_POR_JEFE' || 
                            solicitud.estadoSolicitud === 'PENDIENTE_RH') && (
                            <Button
                              variant="default"
                              size="sm"
                              onClick={() => handleRevisar(solicitud)}
                              className="gap-1"
                            >
                              <Eye className="h-3 w-3" />
                              Revisar
                            </Button>
                          )}
                          {solicitud.estadoSolicitud === 'APROBADA' && (
                            <Button
                              variant="destructive"
                              size="sm"
                              onClick={() => handleCancelar(solicitud)}
                              className="gap-1"
                            >
                              <Ban className="h-3 w-3" />
                              Cancelar
                            </Button>
                          )}
                          {(vistaActual === 'todas' || vistaActual === 'activas') && (
                            <Button
                              variant="ghost"
                              size="sm"
                              onClick={() => handleVerDetalle(solicitud)}
                              className="gap-1"
                            >
                              <Eye className="h-3 w-3" />
                              Ver
                            </Button>
                          )}
                        </div>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Modal Revisar Solicitud */}
      <Modal
        isOpen={showRevisarModal}
        onClose={() => {
          setShowRevisarModal(false);
          setSolicitudSeleccionada(null);
          setComentarios('');
        }}
        title="Revisar Incapacidad - Aprobación RH"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">
                Incapacidad #{solicitudSeleccionada.id}
              </h3>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getEstadoColor(solicitudSeleccionada.estadoSolicitud)}`}>
                {getEstadoLabel(solicitudSeleccionada.estadoSolicitud)}
              </span>
            </div>

            {/* Información del empleado */}
            <div className="p-3 bg-muted rounded-lg">
              <Label className="text-muted-foreground text-xs">Empleado</Label>
              <p className="font-medium">
                {solicitudSeleccionada.nombreEmpleado} {solicitudSeleccionada.primerApellidoEmpleado} {solicitudSeleccionada.segundoApellidoEmpleado}
              </p>
              {solicitudSeleccionada.departamentoEmpleado && (
                <p className="text-sm text-muted-foreground">{solicitudSeleccionada.departamentoEmpleado}</p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Tipo de Incapacidad</Label>
                <p className="font-medium">{getTipoIncapacidadLabel(solicitudSeleccionada.tipoIncapacidad)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Días Totales</Label>
                <p className="font-medium">{solicitudSeleccionada.diasTotales} día(s)</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Fecha Inicio</Label>
                <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaInicio)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Fecha Fin</Label>
                <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaFin)}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Entidad Emisora</Label>
                <p className="font-medium">{getEntidadEmisoraLabel(solicitudSeleccionada.entidadEmisora)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">% de Pago</Label>
                <p className="font-medium">{solicitudSeleccionada.porcentajePago}%</p>
              </div>
            </div>

            {solicitudSeleccionada.numeroDocumento && (
              <div>
                <Label className="text-muted-foreground">Número de Documento</Label>
                <p className="font-medium">{solicitudSeleccionada.numeroDocumento}</p>
              </div>
            )}

            {solicitudSeleccionada.observaciones && (
              <div>
                <Label className="text-muted-foreground">Observaciones del Empleado</Label>
                <p className="text-sm p-2 bg-muted rounded">{solicitudSeleccionada.observaciones}</p>
              </div>
            )}

            {solicitudSeleccionada.urlDocumentoAdjunto && (
              <div>
                <Label className="text-muted-foreground">Documento Adjunto</Label>
                <a
                  href={solicitudSeleccionada.urlDocumentoAdjunto}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline flex items-center gap-1 mt-1"
                >
                  <FileText className="h-4 w-4" />
                  Ver documento adjunto
                </a>
              </div>
            )}

            {/* Aprobación del jefe (si aplica) */}
            {solicitudSeleccionada.nombreAprobadorJefe && (
              <div className="p-3 bg-green-50 dark:bg-green-950 rounded-lg border border-green-200 dark:border-green-800">
                <Label className="text-muted-foreground text-xs">Aprobación del Jefe</Label>
                <p className="font-medium">
                  {solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe}
                </p>
                {solicitudSeleccionada.fechaAprobacionJefe && (
                  <p className="text-sm text-muted-foreground">
                    Fecha: {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}
                  </p>
                )}
                {solicitudSeleccionada.comentariosJefe && (
                  <p className="text-sm mt-2 p-2 bg-white dark:bg-gray-900 rounded">
                    "{solicitudSeleccionada.comentariosJefe}"
                  </p>
                )}
              </div>
            )}

            {/* Comentarios de RH */}
            <div>
              <div className="flex justify-between items-center mb-2">
                <Label htmlFor="comentarios">Comentarios RH (opcional para aprobar, requerido para rechazar)</Label>
                <span className="text-xs text-muted-foreground">{comentarios.length}/500</span>
              </div>
              <Textarea
                id="comentarios"
                value={comentarios}
                onChange={(e) => setComentarios(e.target.value)}
                placeholder="Agregue comentarios sobre su decisión..."
                maxLength={500}
                rows={3}
              />
            </div>

            {/* Botones de acción */}
            <div className="flex gap-2 justify-end pt-4 border-t">
              <Button
                variant="outline"
                onClick={() => {
                  setShowRevisarModal(false);
                  setSolicitudSeleccionada(null);
                  setComentarios('');
                }}
                disabled={procesando}
              >
                Cancelar
              </Button>
              <Button
                variant="destructive"
                onClick={handleRechazarRH}
                disabled={procesando}
                className="gap-2"
              >
                <XCircle className="h-4 w-4" />
                Rechazar
              </Button>
              <Button
                onClick={handleAprobarRH}
                disabled={procesando}
                className="gap-2"
              >
                <CheckCircle className="h-4 w-4" />
                Aprobar
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Ver Detalle */}
      <Modal
        isOpen={showDetalleModal}
        onClose={() => {
          setShowDetalleModal(false);
          setSolicitudSeleccionada(null);
        }}
        title="Detalle de Incapacidad"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">
                Incapacidad #{solicitudSeleccionada.id}
              </h3>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getEstadoColor(solicitudSeleccionada.estadoSolicitud)}`}>
                {getEstadoLabel(solicitudSeleccionada.estadoSolicitud)}
              </span>
            </div>

            {/* Información del empleado */}
            <div className="p-3 bg-muted rounded-lg">
              <Label className="text-muted-foreground text-xs">Empleado</Label>
              <p className="font-medium">
                {solicitudSeleccionada.nombreEmpleado} {solicitudSeleccionada.primerApellidoEmpleado} {solicitudSeleccionada.segundoApellidoEmpleado}
              </p>
              {solicitudSeleccionada.departamentoEmpleado && (
                <p className="text-sm text-muted-foreground">{solicitudSeleccionada.departamentoEmpleado}</p>
              )}
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Tipo de Incapacidad</Label>
                <p className="font-medium">{getTipoIncapacidadLabel(solicitudSeleccionada.tipoIncapacidad)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Días Totales</Label>
                <p className="font-medium">{solicitudSeleccionada.diasTotales} día(s)</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Fecha Inicio</Label>
                <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaInicio)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Fecha Fin</Label>
                <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaFin)}</p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Entidad Emisora</Label>
                <p className="font-medium">{getEntidadEmisoraLabel(solicitudSeleccionada.entidadEmisora)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">% de Pago</Label>
                <p className="font-medium">{solicitudSeleccionada.porcentajePago}%</p>
              </div>
            </div>

            {solicitudSeleccionada.numeroDocumento && (
              <div>
                <Label className="text-muted-foreground">Número de Documento</Label>
                <p className="font-medium">{solicitudSeleccionada.numeroDocumento}</p>
              </div>
            )}

            {solicitudSeleccionada.observaciones && (
              <div>
                <Label className="text-muted-foreground">Observaciones</Label>
                <p className="text-sm p-2 bg-muted rounded">{solicitudSeleccionada.observaciones}</p>
              </div>
            )}

            {solicitudSeleccionada.urlDocumentoAdjunto && (
              <div>
                <Label className="text-muted-foreground">Documento Adjunto</Label>
                <a
                  href={solicitudSeleccionada.urlDocumentoAdjunto}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline flex items-center gap-1 mt-1"
                >
                  <FileText className="h-4 w-4" />
                  Ver documento adjunto
                </a>
              </div>
            )}

            {/* Aprobación del jefe */}
            {solicitudSeleccionada.nombreAprobadorJefe && (
              <div className="p-3 bg-blue-50 dark:bg-blue-950 rounded-lg border border-blue-200 dark:border-blue-800">
                <Label className="text-muted-foreground text-xs">Revisión del Jefe</Label>
                <p className="font-medium">
                  {solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe}
                </p>
                {solicitudSeleccionada.fechaAprobacionJefe && (
                  <p className="text-sm text-muted-foreground">
                    Fecha: {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}
                  </p>
                )}
                {solicitudSeleccionada.comentariosJefe && (
                  <p className="text-sm mt-2 p-2 bg-white dark:bg-gray-900 rounded">
                    "{solicitudSeleccionada.comentariosJefe}"
                  </p>
                )}
              </div>
            )}

            {/* Aprobación de RH */}
            {solicitudSeleccionada.nombreAprobadorRH && (
              <div className="p-3 bg-green-50 dark:bg-green-950 rounded-lg border border-green-200 dark:border-green-800">
                <Label className="text-muted-foreground text-xs">Revisión de RH</Label>
                <p className="font-medium">
                  {solicitudSeleccionada.nombreAprobadorRH} {solicitudSeleccionada.primerApellidoAprobadorRH}
                </p>
                {solicitudSeleccionada.fechaAprobacionRH && (
                  <p className="text-sm text-muted-foreground">
                    Fecha: {formatearFecha(solicitudSeleccionada.fechaAprobacionRH)}
                  </p>
                )}
                {solicitudSeleccionada.comentariosRH && (
                  <p className="text-sm mt-2 p-2 bg-white dark:bg-gray-900 rounded">
                    "{solicitudSeleccionada.comentariosRH}"
                  </p>
                )}
              </div>
            )}

            <div className="pt-4 border-t">
              <Button
                variant="outline"
                onClick={() => {
                  setShowDetalleModal(false);
                  setSolicitudSeleccionada(null);
                }}
                className="w-full"
              >
                Cerrar
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
