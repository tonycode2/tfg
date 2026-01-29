import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Modal } from '@/components/Modal';
import type { RespuestaPermiso } from '@/services/permisosService';
import { 
  obtenerSolicitudesParaRH,
  aprobarPorRH,
  rechazarPorRH,
  cancelarSolicitud,
  obtenerTodasLasSolicitudes
} from '@/services/permisosService';
import { obtenerSaldoEmpleado, ejecutarAcumulacionManual } from '@/services/vacacionesService';
import {
  getEstadoPermisoColor,
  getEstadoPermisoLabel,
  getTipoPermisoLabel,
  formatearFecha,
  formatearHoras
} from '../../lib/utils';
import { FileText, User, Eye, CheckCircle, XCircle, Clock, Filter, Ban, Palmtree } from 'lucide-react';
import { Select, SelectTrigger, SelectContent, SelectItem, SelectValue } from '@/components/ui/select';

export default function PermisosRHView() {
  const [solicitudesPendientes, setSolicitudesPendientes] = useState<RespuestaPermiso[]>([]);
  const [todasLasSolicitudes, setTodasLasSolicitudes] = useState<RespuestaPermiso[]>([]);
  const [vistaActual, setVistaActual] = useState<'pendientes' | 'todas'>('pendientes');
  const [loading, setLoading] = useState(true);
  const [showRevisarModal, setShowRevisarModal] = useState(false);
  const [showDetalleModal, setShowDetalleModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaPermiso | null>(null);
  const [comentarios, setComentarios] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saldoEmpleado, setSaldoEmpleado] = useState<number | null>(null);
  const [ejecutandoAcumulacion, setEjecutandoAcumulacion] = useState(false);
  
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
      } else {
        const data = await obtenerTodasLasSolicitudes();
        setTodasLasSolicitudes(data);
      }
      setError(null);
    } catch (err: any) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  };

  const solicitudesFiltradas = () => {
    const lista = vistaActual === 'pendientes' ? solicitudesPendientes : todasLasSolicitudes;
    
    return lista.filter(solicitud => {
      if (filtroEstado && solicitud.estadoSolicitud !== filtroEstado) return false;
      if (filtroTipo && solicitud.tipoPermiso !== filtroTipo) return false;
      if (filtroEmpleado) {
        const nombreCompleto = `${solicitud.nombreEmpleado} ${solicitud.primerApellidoEmpleado} ${solicitud.segundApellidoEmpleado}`.toLowerCase();
        if (!nombreCompleto.includes(filtroEmpleado.toLowerCase())) return false;
      }
      return true;
    });
  };

  const handleEjecutarAcumulacion = async () => {
    if (!confirm('¿Está seguro de ejecutar la acumulación de vacaciones? Esto agregará 1 día de vacaciones a todos los empleados activos.')) {
      return;
    }

    try {
      setEjecutandoAcumulacion(true);
      await ejecutarAcumulacionManual();
      alert('Acumulación de vacaciones ejecutada exitosamente. Se ha agregado 1 día a todos los empleados activos.');
    } catch (err: any) {
      console.error('Error al ejecutar acumulación:', err);
      alert(err.response?.data?.message || err.message || 'Error al ejecutar la acumulación de vacaciones');
    } finally {
      setEjecutandoAcumulacion(false);
    }
  };

  const handleRevisar = async (solicitud: RespuestaPermiso) => {
    setSolicitudSeleccionada(solicitud);
    setComentarios('');
    setSaldoEmpleado(null);
    setShowRevisarModal(true);
    
    // Si es una solicitud de vacaciones, cargar el saldo del empleado
    if (solicitud.tipoPermiso === 'VACACIONES') {
      try {
        const saldo = await obtenerSaldoEmpleado(solicitud.idEmpleado);
        setSaldoEmpleado(saldo.diasDisponibles);
      } catch (err) {
        console.error('Error al cargar saldo de vacaciones:', err);
      }
    }
  };

  const handleVerDetalle = async (solicitud: RespuestaPermiso) => {
    setSolicitudSeleccionada(solicitud);
    setSaldoEmpleado(null);
    setShowDetalleModal(true);
    
    // Si es una solicitud de vacaciones, cargar el saldo del empleado
    if (solicitud.tipoPermiso === 'VACACIONES') {
      try {
        const saldo = await obtenerSaldoEmpleado(solicitud.idEmpleado);
        setSaldoEmpleado(saldo.diasDisponibles);
      } catch (err) {
        console.error('Error al cargar saldo de vacaciones:', err);
      }
    }
  };

  const handleAprobarRH = async () => {
    if (!solicitudSeleccionada) return;

    try {
      setProcesando(true);
      await aprobarPorRH(solicitudSeleccionada.id, { comentarios });
      alert('Solicitud aprobada exitosamente. Se ha enviado notificación al empleado.');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarDatos();
    } catch (err: any) {
      console.error('Error al aprobar solicitud:', err);
      alert(err.response?.data?.message || 'Error al aprobar la solicitud');
    } finally {
      setProcesando(false);
    }
  };

  const handleRechazarRH = async () => {
    if (!solicitudSeleccionada) return;

    if (!comentarios || comentarios.trim().length < 10) {
      alert('Por favor, proporcione comentarios (mínimo 10 caracteres)');
      return;
    }

    if (!confirm('¿Está seguro de que desea rechazar esta solicitud?')) {
      return;
    }

    try {
      setProcesando(true);
      await rechazarPorRH(solicitudSeleccionada.id, { comentarios });
      alert('Solicitud rechazada');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarDatos();
    } catch (err: any) {
      console.error('Error al rechazar solicitud:', err);
      alert(err.response?.data?.message || 'Error al rechazar la solicitud');
    } finally {
      setProcesando(false);
    }
  };

  const handleCancelar = async (solicitud: RespuestaPermiso) => {
    if (!confirm('¿Está seguro de que desea cancelar esta solicitud aprobada?')) {
      return;
    }

    try {
      await cancelarSolicitud(solicitud.id);
      alert('Solicitud cancelada exitosamente');
      cargarDatos();
    } catch (err: any) {
      console.error('Error al cancelar solicitud:', err);
      alert(err.response?.data?.message || 'Error al cancelar la solicitud');
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
        <h2 className="text-3xl font-bold tracking-tight">Gestión de Permisos - RH</h2>
        <p className="text-muted-foreground">
          Aprobación final de solicitudes y auditoría completa
        </p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      {/* Selector de Vista */}
      <div className="flex gap-2 justify-between items-center">
        <div className="flex gap-2">
          <Button
            variant={vistaActual === 'pendientes' ? 'default' : 'outline'}
            onClick={() => setVistaActual('pendientes')}
            className="gap-2"
          >
            <Clock className="h-4 w-4" />
            Pendientes ({solicitudesPendientes.length})
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
        <Button
          variant="outline"
          onClick={handleEjecutarAcumulacion}
          disabled={ejecutandoAcumulacion}
          className="gap-2 bg-green-50 hover:bg-green-100 dark:bg-green-950 dark:hover:bg-green-900 border-green-200 dark:border-green-800"
        >
          <Palmtree className="h-4 w-4" />
          {ejecutandoAcumulacion ? 'Procesando...' : 'Acumular Vacaciones'}
        </Button>
      </div>

      {/* Filtros */}
      {vistaActual === 'todas' && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Filter className="h-4 w-4" />
              Filtros
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-3 gap-4">
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
              <div>
                <Label htmlFor="filtroTipo">Tipo de Permiso</Label>
                <Select value={filtroTipo === '' ? '__ALL__' : filtroTipo} onValueChange={(v) => setFiltroTipo(v === '__ALL__' ? '' : String(v))}>
                  <SelectTrigger className="w-full">
                    <SelectValue />
                  </SelectTrigger>
                  <SelectContent>
                    <SelectItem value="__ALL__">Todos</SelectItem>
                    <SelectItem value="PERSONAL">Personal</SelectItem>
                    <SelectItem value="LUTO">Luto</SelectItem>
                    <SelectItem value="SIN_GOCE_SALARIO">Sin Goce de Salario</SelectItem>
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
            {vistaActual === 'pendientes' ? 'Solicitudes Pendientes de Aprobación' : 'Historial Completo'}
          </CardTitle>
          <CardDescription>
            {solicitudesMostrar.length} solicitud(es) 
            {vistaActual === 'todas' && (filtroEstado || filtroTipo || filtroEmpleado) && ' (filtradas)'}
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
                  <th className="text-left p-3">Estado</th>
                  {vistaActual === 'pendientes' && <th className="text-left p-3">Aprobador Jefe</th>}
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudesMostrar.length === 0 ? (
                  <tr>
                    <td colSpan={vistaActual === 'pendientes' ? 7 : 6} className="text-center p-8 text-muted-foreground">
                      No hay solicitudes
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
                              {solicitud.segundApellidoEmpleado}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm">{getTipoPermisoLabel(solicitud.tipoPermiso)}</div>
                      </td>
                      <td className="p-3">
                        <div className="text-sm">
                          <div>{formatearFecha(solicitud.fechaInicio)}</div>
                          {solicitud.unidadTiempo === 'HORAS' ? (
                            <div className="text-muted-foreground text-xs">
                              {solicitud.horaInicio} - {solicitud.horaFin}
                            </div>
                          ) : (
                            <>
                              <div className="text-muted-foreground text-xs">hasta</div>
                              <div>{formatearFecha(solicitud.fechaFin)}</div>
                            </>
                          )}
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        {solicitud.unidadTiempo === 'HORAS' ? (
                          <span className="font-semibold">{formatearHoras(solicitud.totalHoras || 0)}</span>
                        ) : (
                          <span className="font-semibold">{solicitud.diasTotales}</span>
                        )}
                      </td>
                      <td className="p-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoPermisoColor(solicitud.estadoSolicitud)}`}>
                          {getEstadoPermisoLabel(solicitud.estadoSolicitud)}
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
                            <span className="text-muted-foreground text-sm">Sin jefe</span>
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
                          {vistaActual === 'todas' && (
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
          if (!procesando) {
            setShowRevisarModal(false);
            setSolicitudSeleccionada(null);
            setComentarios('');
          }
        }}
        title="Aprobación Final - RH"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="bg-muted/50 p-4 rounded-lg">
              <h3 className="font-semibold mb-3">Información del Solicitante</h3>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <Label className="text-muted-foreground">Empleado</Label>
                  <p className="font-medium">
                    {solicitudSeleccionada.nombreEmpleado} {solicitudSeleccionada.primerApellidoEmpleado} {solicitudSeleccionada.segundApellidoEmpleado}
                  </p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Fecha Solicitud</Label>
                  <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaSolicitud)}</p>
                </div>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Tipo de Permiso</Label>
                <p className="font-medium">{getTipoPermisoLabel(solicitudSeleccionada.tipoPermiso)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' ? 'Tiempo Solicitado' : 'Días Solicitados'}
                </Label>
                <p className="font-medium text-lg text-primary">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' 
                    ? formatearHoras(solicitudSeleccionada.totalHoras || 0)
                    : `${solicitudSeleccionada.diasTotales} días hábiles`
                  }
                </p>
              </div>
            </div>

            {/* Mostrar saldo de vacaciones si es una solicitud de vacaciones */}
            {solicitudSeleccionada.tipoPermiso === 'VACACIONES' && saldoEmpleado !== null && (
              <div className="p-3 rounded-lg bg-blue-50 border border-blue-200 dark:bg-blue-950 dark:border-blue-800">
                <div className="flex items-center gap-2">
                  <Palmtree className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                  <div>
                    <p className="text-sm text-blue-800 dark:text-blue-200">
                      Saldo actual del empleado: <span className="font-bold">{saldoEmpleado} día{saldoEmpleado !== 1 ? 's' : ''}</span>
                    </p>
                    <p className="text-xs text-blue-600 dark:text-blue-400">
                      Después de aprobar quedará con {saldoEmpleado - solicitudSeleccionada.diasTotales} día{(saldoEmpleado - solicitudSeleccionada.diasTotales) !== 1 ? 's' : ''}
                    </p>
                  </div>
                </div>
              </div>
            )}

            {solicitudSeleccionada.unidadTiempo === 'HORAS' ? (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label className="text-muted-foreground">Fecha</Label>
                  <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaInicio)}</p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Horario</Label>
                  <p className="font-medium">{solicitudSeleccionada.horaInicio} - {solicitudSeleccionada.horaFin}</p>
                </div>
              </div>
            ) : (
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
            )}

            <div>
              <Label className="text-muted-foreground">Motivo</Label>
              <p className="bg-muted/30 p-3 rounded text-sm">{solicitudSeleccionada.motivo}</p>
            </div>

            <div>
              <Label className="text-muted-foreground">Observaciones del Empleado</Label>
              <p className="bg-muted/30 p-3 rounded text-sm">{solicitudSeleccionada.observacionesEmpleado}</p>
            </div>

            {solicitudSeleccionada.comentariosJefe && (
              <div className="bg-cyan-50 dark:bg-cyan-950 p-3 rounded">
                <Label className="text-muted-foreground">Comentarios del Jefe</Label>
                <p className="text-sm italic mt-1">"{solicitudSeleccionada.comentariosJefe}"</p>
                {solicitudSeleccionada.nombreAprobadorJefe && (
                  <p className="text-xs text-muted-foreground mt-2">
                    - {solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe}
                    {solicitudSeleccionada.fechaAprobacionJefe && ` (${formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)})`}
                  </p>
                )}
              </div>
            )}

            <div className="border-t pt-4">
              <Label htmlFor="comentariosRH">Comentarios de RH (Opcional)</Label>
              <Textarea
                id="comentariosRH"
                value={comentarios}
                onChange={(e) => setComentarios(e.target.value)}
                placeholder="Agregue comentarios sobre su decisión..."
                maxLength={500}
                rows={4}
                disabled={procesando}
              />
              <p className="text-xs text-muted-foreground mt-1">
                {comentarios.length}/500 caracteres
              </p>
            </div>

            <div className="flex gap-2 justify-end pt-4 border-t">
              <Button
                type="button"
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
                type="button"
                variant="destructive"
                onClick={handleRechazarRH}
                disabled={procesando}
                className="gap-2"
              >
                <XCircle className="h-4 w-4" />
                Rechazar
              </Button>
              <Button
                type="button"
                variant="default"
                onClick={handleAprobarRH}
                disabled={procesando}
                className="gap-2 bg-green-600 hover:bg-green-700"
              >
                <CheckCircle className="h-4 w-4" />
                Aprobar (Enviar Notificación)
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Detalle (Vista Auditoría) */}
      <Modal
        isOpen={showDetalleModal}
        onClose={() => {
          setShowDetalleModal(false);
          setSolicitudSeleccionada(null);
        }}
        title="Detalle de Solicitud"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">Solicitud #{solicitudSeleccionada.id}</h3>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getEstadoPermisoColor(solicitudSeleccionada.estadoSolicitud)}`}>
                {getEstadoPermisoLabel(solicitudSeleccionada.estadoSolicitud)}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4 text-sm">
              <div>
                <Label className="text-muted-foreground">Empleado</Label>
                <p className="font-medium">
                  {solicitudSeleccionada.nombreEmpleado} {solicitudSeleccionada.primerApellidoEmpleado} {solicitudSeleccionada.segundApellidoEmpleado}
                </p>
              </div>
              <div>
                <Label className="text-muted-foreground">Tipo</Label>
                <p className="font-medium">{getTipoPermisoLabel(solicitudSeleccionada.tipoPermiso)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' ? 'Tiempo' : 'Días'}
                </Label>
                <p className="font-medium">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' 
                    ? formatearHoras(solicitudSeleccionada.totalHoras || 0)
                    : `${solicitudSeleccionada.diasTotales} días hábiles`
                  }
                </p>
              </div>
              <div>
                <Label className="text-muted-foreground">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' ? 'Fecha y Horario' : 'Fechas'}
                </Label>
                <p className="font-medium">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' 
                    ? `${formatearFecha(solicitudSeleccionada.fechaInicio)} (${solicitudSeleccionada.horaInicio} - ${solicitudSeleccionada.horaFin})`
                    : `${formatearFecha(solicitudSeleccionada.fechaInicio)} - ${formatearFecha(solicitudSeleccionada.fechaFin)}`
                  }
                </p>
              </div>
            </div>

            {/* Mostrar saldo de vacaciones si es una solicitud de vacaciones */}
            {solicitudSeleccionada.tipoPermiso === 'VACACIONES' && saldoEmpleado !== null && (
              <div className="p-3 rounded-lg bg-blue-50 border border-blue-200 dark:bg-blue-950 dark:border-blue-800">
                <div className="flex items-center gap-2">
                  <Palmtree className="h-5 w-5 text-blue-600 dark:text-blue-400" />
                  <p className="text-sm text-blue-800 dark:text-blue-200">
                    Saldo actual del empleado: <span className="font-bold">{saldoEmpleado} día{saldoEmpleado !== 1 ? 's' : ''}</span>
                  </p>
                </div>
              </div>
            )}

            <div className="border-t pt-4">
              <h4 className="font-semibold mb-3">Historial</h4>
              <div className="space-y-3 text-sm">
                <div>
                  <div className="flex items-center gap-2 text-muted-foreground">
                    <Clock className="h-3 w-3" />
                    <span>Solicitud creada: {formatearFecha(solicitudSeleccionada.fechaSolicitud)}</span>
                  </div>
                </div>
                
                {solicitudSeleccionada.fechaAprobacionJefe && (
                  <div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="h-3 w-3 text-cyan-600" />
                      <span>Aprobada por jefe: {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}</span>
                    </div>
                    {solicitudSeleccionada.comentariosJefe && (
                      <p className="ml-5 text-muted-foreground italic">"{solicitudSeleccionada.comentariosJefe}"</p>
                    )}
                  </div>
                )}
                
                {solicitudSeleccionada.fechaAprobacionRH && (
                  <div>
                    <div className="flex items-center gap-2">
                      <CheckCircle className="h-3 w-3 text-green-600" />
                      <span>
                        {solicitudSeleccionada.estadoSolicitud === 'APROBADA' ? 'Aprobada' : 'Rechazada'} por RH: {formatearFecha(solicitudSeleccionada.fechaAprobacionRH)}
                      </span>
                    </div>
                    {solicitudSeleccionada.comentariosRH && (
                      <p className="ml-5 text-muted-foreground italic">"{solicitudSeleccionada.comentariosRH}"</p>
                    )}
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end pt-4">
              <Button onClick={() => setShowDetalleModal(false)}>Cerrar</Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
