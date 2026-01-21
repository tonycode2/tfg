import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { TimePicker } from '@/components/ui/time-picker';
import { DatePicker } from '@/components/ui/date-picker';
import { Modal } from '@/components/Modal';
import type { RespuestaPermiso } from '../../services/permisosService';
import { 
  crearSolicitud, 
  obtenerMisSolicitudes 
} from '../../services/permisosService';
import { obtenerMiSaldo } from '../../services/vacacionesService';
import { validarRangoSinFeriados, formatearFechaFeriado } from '../../services/diasFeriadosService';
import { authService } from '../../services/authService';
import {
  getEstadoPermisoColor,
  getEstadoPermisoLabel,
  getTipoPermisoLabel,
  calcularDiasHabiles,
  calcularHoras,
  formatearFecha,
  formatearHoras
} from '../../lib/utils';
import { Calendar, Plus, Eye, FileText, Clock, CheckCircle, XCircle, Palmtree } from 'lucide-react';

const TIPOS_PERMISO = [
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'MEDICO', label: 'Médico' },
  { value: 'LUTO', label: 'Luto' },
  { value: 'MATERNIDAD', label: 'Maternidad' },
  { value: 'PATERNIDAD', label: 'Paternidad' },
  { value: 'ESTUDIO', label: 'Estudio' },
  { value: 'SIN_GOCE_SALARIO', label: 'Sin Goce de Salario' },
  { value: 'VACACIONES', label: 'Vacaciones' },
];

export default function PermisosView() {
  const [solicitudes, setSolicitudes] = useState<RespuestaPermiso[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNuevaSolicitudModal, setShowNuevaSolicitudModal] = useState(false);
  const [showDetalleModal, setShowDetalleModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaPermiso | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [saldoVacaciones, setSaldoVacaciones] = useState<number | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    fechaInicio: '',
    fechaFin: '',
    diasTotales: 0,
    unidadTiempo: 'DIAS', // 'DIAS' o 'HORAS'
    horaInicio: '',
    horaFin: '',
    totalHoras: 0,
    tipoPermiso: 'PERSONAL',
    motivo: '',
    urlDocumentoAdjunto: '',
  });

  useEffect(() => {
    cargarSolicitudes();
    cargarSaldoVacaciones();
  }, []);

  const cargarSaldoVacaciones = async () => {
    try {
      const data = await obtenerMiSaldo();
      setSaldoVacaciones(data.diasDisponibles);
    } catch (err: any) {
      console.error('Error al cargar saldo de vacaciones:', err);
      setSaldoVacaciones(0);
    }
  };

  const cargarSolicitudes = async () => {
    try {
      setLoading(true);
      const data = await obtenerMisSolicitudes();
      setSolicitudes(data);
      setError(null);
    } catch (err: any) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  };

  const handleFechaChange = (campo: 'fechaInicio' | 'fechaFin', valor: string) => {
    const newFormData = { ...formData, [campo]: valor };
    
    // Si es permiso por DIAS, calcular días automáticamente
    if (newFormData.unidadTiempo === 'DIAS' && newFormData.fechaInicio && newFormData.fechaFin) {
      const dias = calcularDiasHabiles(newFormData.fechaInicio, newFormData.fechaFin);
      newFormData.diasTotales = dias;
    }
    
    // Si es permiso por HORAS y cambia la fecha, igualar fechaFin a fechaInicio
    if (newFormData.unidadTiempo === 'HORAS' && campo === 'fechaInicio') {
      newFormData.fechaFin = valor;
    }
    
    setFormData(newFormData);
  };

  const handleHoraChange = (campo: 'horaInicio' | 'horaFin', valor: string) => {
    const newFormData = { ...formData, [campo]: valor };
    
    // Calcular horas automáticamente si ambas horas están llenas
    if (newFormData.horaInicio && newFormData.horaFin) {
      const horas = calcularHoras(newFormData.horaInicio, newFormData.horaFin);
      newFormData.totalHoras = horas;
    }
    
    setFormData(newFormData);
  };

  const handleUnidadTiempoChange = (unidad: string) => {
    // No permitir cambiar a HORAS si es VACACIONES
    if (unidad === 'HORAS' && formData.tipoPermiso === 'VACACIONES') {
      return;
    }
    setFormData({
      ...formData,
      unidadTiempo: unidad,
      // Limpiar campos al cambiar de unidad
      fechaInicio: '',
      fechaFin: '',
      diasTotales: 0,
      horaInicio: '',
      horaFin: '',
      totalHoras: 0,
    });
  };

  const handleTipoPermisoChange = (value: string) => {
    // Si cambia a VACACIONES, forzar unidad de tiempo a DIAS
    if (value === 'VACACIONES') {
      setFormData({ 
        ...formData, 
        tipoPermiso: value, 
        unidadTiempo: 'DIAS',
        horaInicio: '',
        horaFin: '',
        totalHoras: 0,
      });
    } else {
      setFormData({ ...formData, tipoPermiso: value });
    }
  };

  const handleSubmitNuevaSolicitud = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validaciones comunes
    if (!formData.fechaInicio) {
      alert('Debe seleccionar la fecha de inicio');
      return;
    }
    
    if (formData.motivo.length < 10) {
      alert('El motivo debe tener al menos 10 caracteres');
      return;
    }
    
    // Validar que la fecha de inicio sea futura
    const hoy = new Date();
    const hoyStr = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
    
    if (formData.fechaInicio < hoyStr) {
      alert('No se permiten solicitudes con fechas pasadas');
      return;
    }
    
    // Validaciones específicas por unidad de tiempo
    if (formData.unidadTiempo === 'HORAS') {
      if (!formData.horaInicio || !formData.horaFin) {
        alert('Debe seleccionar hora de inicio y fin');
        return;
      }
      if (formData.totalHoras <= 0) {
        alert('La hora de fin debe ser posterior a la hora de inicio');
        return;
      }
      // No se permiten vacaciones por horas
      if (formData.tipoPermiso === 'VACACIONES') {
        alert('Las vacaciones solo pueden solicitarse por días completos');
        return;
      }
    } else {
      if (!formData.fechaFin) {
        alert('Debe seleccionar la fecha de fin');
        return;
      }
      // Validar saldo de vacaciones
      if (formData.tipoPermiso === 'VACACIONES' && saldoVacaciones !== null) {
        if (formData.diasTotales > saldoVacaciones) {
          alert(`No tiene suficiente saldo de vacaciones. Tiene ${saldoVacaciones} día(s) disponible(s) y está solicitando ${formData.diasTotales} día(s).`);
          return;
        }
      }
      
      // Validar que no haya días feriados en el rango
      try {
        const feriadosEnRango = await validarRangoSinFeriados(formData.fechaInicio, formData.fechaFin);
        if (feriadosEnRango.length > 0) {
          const listaFeriados = feriadosEnRango
            .map(f => `• ${formatearFechaFeriado(f.fecha)} - ${f.nombre}`)
            .join('\n');
          alert(`No se pueden solicitar permisos en días feriados.\n\nLos siguientes días feriados están incluidos en su solicitud:\n${listaFeriados}`);
          return;
        }
      } catch (err) {
        console.error('Error al validar días feriados:', err);
        // Continuar con la solicitud si hay error en la validación
      }
    }
    
    try {
      // Obtener idEmpleado del token JWT decodificado
      const userInfo = authService.getUserInfo();
      const idEmpleado = userInfo.idEmpleado;
      
      if (!idEmpleado) {
        alert('Error: No se pudo obtener la información del empleado');
        return;
      }
      
      const solicitud: any = {
        fechaInicio: formData.fechaInicio,
        fechaFin: formData.unidadTiempo === 'HORAS' ? formData.fechaInicio : formData.fechaFin,
        diasTotales: formData.diasTotales,
        unidadTiempo: formData.unidadTiempo,
        motivo: formData.motivo,
        tipoPermiso: formData.tipoPermiso,
        idEmpleado,
      };

      if (formData.unidadTiempo === 'HORAS') {
        solicitud.horaInicio = formData.horaInicio;
        solicitud.horaFin = formData.horaFin;
        solicitud.totalHoras = formData.totalHoras;
      }

      if (formData.urlDocumentoAdjunto) {
        solicitud.urlDocumentoAdjunto = formData.urlDocumentoAdjunto;
      }
      
      await crearSolicitud(solicitud);
      
      alert('Solicitud creada exitosamente');
      setShowNuevaSolicitudModal(false);
      resetForm();
      cargarSolicitudes();
      // Recargar saldo de vacaciones si se creó una solicitud de vacaciones
      if (formData.tipoPermiso === 'VACACIONES') {
        cargarSaldoVacaciones();
      }
    } catch (err: any) {
      console.error('Error al crear solicitud:', err);
      alert(err.response?.data?.message || err.message || 'Error al crear la solicitud');
    }
  };

  const resetForm = () => {
    setFormData({
      fechaInicio: '',
      fechaFin: '',
      diasTotales: 0,
      unidadTiempo: 'DIAS',
      horaInicio: '',
      horaFin: '',
      totalHoras: 0,
      tipoPermiso: 'PERSONAL',
      motivo: '',
      urlDocumentoAdjunto: '',
    });
  };

  const handleVerDetalle = (solicitud: RespuestaPermiso) => {
    setSolicitudSeleccionada(solicitud);
    setShowDetalleModal(true);
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Cargando solicitudes...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Mis Solicitudes de Permisos</h2>
          <p className="text-muted-foreground">
            Gestiona tus solicitudes de permisos laborales
          </p>
        </div>
        <Button onClick={() => setShowNuevaSolicitudModal(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Solicitud
        </Button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Historial de Solicitudes</CardTitle>
          <CardDescription>
            {solicitudes.length} solicitud(es) en total
          </CardDescription>
        </CardHeader>
        <CardContent>
          <div className="overflow-x-auto">
            <table className="w-full">
              <thead>
                <tr className="border-b">
                  <th className="text-left p-3">Tipo</th>
                  <th className="text-left p-3">Fechas</th>
                  <th className="text-center p-3">Días</th>
                  <th className="text-left p-3">Estado</th>
                  <th className="text-left p-3">Fecha Solicitud</th>
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudes.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="text-center p-8 text-muted-foreground">
                      No hay solicitudes registradas
                    </td>
                  </tr>
                ) : (
                  solicitudes.map((solicitud) => (
                    <tr key={solicitud.id} className="border-b hover:bg-muted/50">
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <FileText className="h-4 w-4 text-muted-foreground" />
                          {getTipoPermisoLabel(solicitud.tipoPermiso)}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2 text-sm">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          {formatearFecha(solicitud.fechaInicio)} - {formatearFecha(solicitud.fechaFin)}
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        {solicitud.unidadTiempo === 'HORAS' ? (
                          <span className="font-semibold">{formatearHoras(solicitud.totalHoras || 0)}</span>
                        ) : (
                          <span className="font-semibold">{solicitud.diasTotales} días</span>
                        )}
                      </td>
                      <td className="p-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoPermisoColor(solicitud.estadoSolicitud)}`}>
                          {getEstadoPermisoLabel(solicitud.estadoSolicitud)}
                        </span>
                      </td>
                      <td className="p-3 text-sm text-muted-foreground">
                        {formatearFecha(solicitud.fechaSolicitud)}
                      </td>
                      <td className="p-3 text-center">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleVerDetalle(solicitud)}
                          className="gap-2"
                        >
                          <Eye className="h-4 w-4" />
                          Ver
                        </Button>
                      </td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </CardContent>
      </Card>

      {/* Modal Nueva Solicitud */}
      <Modal
        isOpen={showNuevaSolicitudModal}
        onClose={() => {
          setShowNuevaSolicitudModal(false);
          resetForm();
        }}
        title="Nueva Solicitud de Permiso"
      >
        <form onSubmit={handleSubmitNuevaSolicitud} className="space-y-4">
          {/* Tipo de Permiso */}
          <div>
            <Label htmlFor="tipoPermiso">Tipo de Permiso *</Label>
            <Select
              value={formData.tipoPermiso}
              onValueChange={handleTipoPermisoChange}
              required
            >
              <SelectTrigger>
                <SelectValue placeholder="Seleccione un tipo de permiso" />
              </SelectTrigger>
              <SelectContent>
                {TIPOS_PERMISO.map((tipo) => (
                  <SelectItem key={tipo.value} value={tipo.value}>
                    {tipo.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Mostrar saldo de vacaciones cuando se selecciona VACACIONES */}
          {formData.tipoPermiso === 'VACACIONES' && saldoVacaciones !== null && (
            <div className={`p-4 rounded-lg border ${
              saldoVacaciones > 0 
                ? 'bg-green-50 border-green-200 dark:bg-green-950 dark:border-green-800' 
                : 'bg-yellow-50 border-yellow-200 dark:bg-yellow-950 dark:border-yellow-800'
            }`}>
              <div className="flex items-center gap-2">
                <Palmtree className={`h-5 w-5 ${saldoVacaciones > 0 ? 'text-green-600 dark:text-green-400' : 'text-yellow-600 dark:text-yellow-400'}`} />
                <div>
                  <p className={`text-sm font-medium ${saldoVacaciones > 0 ? 'text-green-800 dark:text-green-200' : 'text-yellow-800 dark:text-yellow-200'}`}>
                    Saldo de Vacaciones Disponible
                  </p>
                  <p className={`text-2xl font-bold ${saldoVacaciones > 0 ? 'text-green-700 dark:text-green-300' : 'text-yellow-700 dark:text-yellow-300'}`}>
                    {saldoVacaciones} día{saldoVacaciones !== 1 ? 's' : ''}
                  </p>
                </div>
              </div>
              {saldoVacaciones === 0 && (
                <p className="text-xs text-yellow-600 dark:text-yellow-400 mt-2">
                  No tiene días de vacaciones disponibles. Los días se acumulan el primer día de cada mes.
                </p>
              )}
            </div>
          )}

          {/* Toggle Días/Horas - Solo mostrar si NO es VACACIONES */}
          {formData.tipoPermiso !== 'VACACIONES' && (
            <div>
              <Label>Unidad de Tiempo *</Label>
              <div className="flex gap-2 mt-2">
                <Button
                  type="button"
                  variant={formData.unidadTiempo === 'DIAS' ? 'default' : 'outline'}
                  className="flex-1"
                  onClick={() => handleUnidadTiempoChange('DIAS')}
                >
                  <Calendar className="h-4 w-4 mr-2" />
                  Por Días
                </Button>
                <Button
                  type="button"
                  variant={formData.unidadTiempo === 'HORAS' ? 'default' : 'outline'}
                  className="flex-1"
                  onClick={() => handleUnidadTiempoChange('HORAS')}
                >
                  <Clock className="h-4 w-4 mr-2" />
                  Por Horas
                </Button>
              </div>
            </div>
          )}

          {/* UI Condicional: Días */}
          {formData.unidadTiempo === 'DIAS' && (
            <>
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="fechaInicio">Fecha Inicio *</Label>
                  <DatePicker
                    value={formData.fechaInicio}
                    onChange={(fecha) => handleFechaChange('fechaInicio', fecha)}
                    placeholder="Seleccionar fecha inicio"
                    fromYear={new Date().getFullYear()}
                    toYear={new Date().getFullYear() + 2}
                  />
                </div>
                <div>
                  <Label htmlFor="fechaFin">Fecha Fin *</Label>
                  <DatePicker
                    value={formData.fechaFin}
                    onChange={(fecha) => handleFechaChange('fechaFin', fecha)}
                    placeholder="Seleccionar fecha fin"
                    fromYear={new Date().getFullYear()}
                    toYear={new Date().getFullYear() + 2}
                  />
                </div>
              </div>

              <div>
                <Label>Días Hábiles Calculados</Label>
                <div className="text-2xl font-bold text-primary">
                  {formData.diasTotales} días
                </div>
                <p className="text-xs text-muted-foreground">
                  (Excluye fines de semana)
                </p>
              </div>
            </>
          )}

          {/* UI Condicional: Horas */}
          {formData.unidadTiempo === 'HORAS' && (
            <>
              <div>
                <Label htmlFor="fechaPermisoHoras">Fecha del Permiso *</Label>
                <DatePicker
                  value={formData.fechaInicio}
                  onChange={(fecha) => handleFechaChange('fechaInicio', fecha)}
                  placeholder="Seleccionar fecha del permiso"
                  fromYear={new Date().getFullYear()}
                  toYear={new Date().getFullYear() + 2}
                />
              </div>

              <div className="grid grid-cols-2 gap-4">
                <div>
                  <Label htmlFor="horaInicio">Hora Inicio *</Label>
                  <TimePicker
                    value={formData.horaInicio}
                    onChange={(valor) => handleHoraChange('horaInicio', valor)}
                    placeholder="Seleccionar hora inicio"
                  />
                </div>
                <div>
                  <Label htmlFor="horaFin">Hora Fin *</Label>
                  <TimePicker
                    value={formData.horaFin}
                    onChange={(valor) => handleHoraChange('horaFin', valor)}
                    placeholder="Seleccionar hora fin"
                  />
                </div>
              </div>

              {formData.totalHoras > 0 && (
                <div>
                  <Label>Total de Horas Calculadas</Label>
                  <div className="text-2xl font-bold text-primary">
                    {formatearHoras(formData.totalHoras)}
                  </div>
                </div>
              )}
            </>
          )}

          <div>
            <div className="flex justify-between items-center mb-2">
              <Label htmlFor="motivo">Motivo *</Label>
              <span className="text-xs text-muted-foreground">{formData.motivo.length}/500</span>
            </div>
            <Textarea
              id="motivo"
              value={formData.motivo}
              onChange={(e: React.ChangeEvent<HTMLTextAreaElement>) => setFormData({ ...formData, motivo: e.target.value })}
              placeholder="Describa el motivo de su solicitud (mínimo 10 caracteres)..."
              minLength={10}
              maxLength={500}
              rows={3}
              required
            />
          </div>

          {/* TODO: Implementar carga de archivo adjunto en el futuro */}
          {/* <div>
            <Label htmlFor="documentoAdjunto">Documento Adjunto (Opcional)</Label>
            <Input
              id="documentoAdjunto"
              type="file"
              accept=".pdf,.doc,.docx,.jpg,.jpeg,.png"
              className="cursor-pointer"
            />
            <p className="text-xs text-muted-foreground mt-1">
              Puede adjuntar un documento de soporte (máx. 5MB)
            </p>
          </div> */}

          <div className="flex gap-2 justify-end">
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setShowNuevaSolicitudModal(false);
                resetForm();
              }}
            >
              Cancelar
            </Button>
            <Button type="submit">
              Crear Solicitud
            </Button>
          </div>
        </form>
      </Modal>

      {/* Modal Detalle Solicitud */}
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
              <h3 className="text-lg font-semibold">
                Solicitud #{solicitudSeleccionada.id}
              </h3>
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getEstadoPermisoColor(solicitudSeleccionada.estadoSolicitud)}`}>
                {getEstadoPermisoLabel(solicitudSeleccionada.estadoSolicitud)}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Tipo de Permiso</Label>
                <p className="font-medium">{getTipoPermisoLabel(solicitudSeleccionada.tipoPermiso)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' ? 'Horas Solicitadas' : 'Días Solicitados'}
                </Label>
                <p className="font-medium">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' 
                    ? formatearHoras(solicitudSeleccionada.totalHoras || 0)
                    : `${solicitudSeleccionada.diasTotales} días hábiles`}
                </p>
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">
                  {solicitudSeleccionada.unidadTiempo === 'HORAS' ? 'Fecha' : 'Fecha Inicio'}
                </Label>
                <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaInicio)}</p>
              </div>
              {solicitudSeleccionada.unidadTiempo === 'HORAS' ? (
                <div>
                  <Label className="text-muted-foreground">Horario</Label>
                  <p className="font-medium">
                    {solicitudSeleccionada.horaInicio} - {solicitudSeleccionada.horaFin}
                  </p>
                </div>
              ) : (
                <div>
                  <Label className="text-muted-foreground">Fecha Fin</Label>
                  <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaFin)}</p>
                </div>
              )}
            </div>

            <div>
              <Label className="text-muted-foreground">Motivo</Label>
              <p className="font-medium">{solicitudSeleccionada.motivo}</p>
            </div>

            <div>
              <Label className="text-muted-foreground">Observaciones</Label>
              <p className="font-medium">{solicitudSeleccionada.observacionesEmpleado}</p>
            </div>

            {solicitudSeleccionada.urlDocumentoAdjunto && (
              <div>
                <Label className="text-muted-foreground">Documento Adjunto</Label>
                <a
                  href={solicitudSeleccionada.urlDocumentoAdjunto}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline"
                >
                  Ver documento
                </a>
              </div>
            )}

            <div className="border-t pt-4">
              <h4 className="font-semibold mb-3 flex items-center gap-2">
                <Clock className="h-4 w-4" />
                Historial de Aprobación
              </h4>
              
              <div className="space-y-3">
                <div className="flex items-start gap-3">
                  <div className="mt-1">
                    <div className="h-6 w-6 rounded-full bg-primary/20 flex items-center justify-center">
                      <FileText className="h-3 w-3 text-primary" />
                    </div>
                  </div>
                  <div>
                    <p className="font-medium">Solicitud Creada</p>
                    <p className="text-sm text-muted-foreground">
                      {formatearFecha(solicitudSeleccionada.fechaSolicitud)}
                    </p>
                  </div>
                </div>

                {solicitudSeleccionada.fechaAprobacionJefe && solicitudSeleccionada.estadoSolicitud !== 'RECHAZADA_POR_JEFE' && (
                  <div className="flex items-start gap-3">
                    <div className="mt-1">
                      <div className="h-6 w-6 rounded-full bg-cyan-100 dark:bg-cyan-900 flex items-center justify-center">
                        <CheckCircle className="h-3 w-3 text-cyan-700 dark:text-cyan-300" />
                      </div>
                    </div>
                    <div>
                      <p className="font-medium">
                        Aprobada por Jefe
                        {solicitudSeleccionada.nombreAprobadorJefe && (
                          <span className="text-muted-foreground text-sm ml-2">
                            ({solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe})
                          </span>
                        )}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}
                      </p>
                      {solicitudSeleccionada.comentariosJefe && (
                        <p className="text-sm mt-1 italic">"{solicitudSeleccionada.comentariosJefe}"</p>
                      )}
                    </div>
                  </div>
                )}

                {solicitudSeleccionada.estadoSolicitud === 'RECHAZADA_POR_JEFE' && (
                  <div className="flex items-start gap-3">
                    <div className="mt-1">
                      <div className="h-6 w-6 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
                        <XCircle className="h-3 w-3 text-red-700 dark:text-red-300" />
                      </div>
                    </div>
                    <div>
                      <p className="font-medium">
                        Rechazada por Jefe
                        {solicitudSeleccionada.nombreAprobadorJefe && (
                          <span className="text-muted-foreground text-sm ml-2">
                            ({solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe})
                          </span>
                        )}
                      </p>
                      {solicitudSeleccionada.fechaAprobacionJefe && (
                        <p className="text-sm text-muted-foreground">
                          {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}
                        </p>
                      )}
                      {solicitudSeleccionada.comentariosJefe && (
                        <p className="text-sm mt-1 italic">"{solicitudSeleccionada.comentariosJefe}"</p>
                      )}
                    </div>
                  </div>
                )}

                {solicitudSeleccionada.estadoSolicitud === 'RECHAZADA_POR_RH' && (
                  <div className="flex items-start gap-3">
                    <div className="mt-1">
                      <div className="h-6 w-6 rounded-full bg-red-100 dark:bg-red-900 flex items-center justify-center">
                        <XCircle className="h-3 w-3 text-red-700 dark:text-red-300" />
                      </div>
                    </div>
                    <div>
                      <p className="font-medium">
                        Rechazada por RH
                        {solicitudSeleccionada.nombreAprobadorRH && (
                          <span className="text-muted-foreground text-sm ml-2">
                            ({solicitudSeleccionada.nombreAprobadorRH} {solicitudSeleccionada.primerApellidoAprobadorRH})
                          </span>
                        )}
                      </p>
                      {solicitudSeleccionada.fechaAprobacionRH && (
                        <p className="text-sm text-muted-foreground">
                          {formatearFecha(solicitudSeleccionada.fechaAprobacionRH)}
                        </p>
                      )}
                      {solicitudSeleccionada.comentariosRH && (
                        <p className="text-sm mt-1 italic">"{solicitudSeleccionada.comentariosRH}"</p>
                      )}
                    </div>
                  </div>
                )}

                {solicitudSeleccionada.estadoSolicitud === 'APROBADA' && solicitudSeleccionada.fechaAprobacionRH && (
                  <div className="flex items-start gap-3">
                    <div className="mt-1">
                      <div className="h-6 w-6 rounded-full bg-green-100 dark:bg-green-900 flex items-center justify-center">
                        <CheckCircle className="h-3 w-3 text-green-700 dark:text-green-300" />
                      </div>
                    </div>
                    <div>
                      <p className="font-medium">
                        Aprobada por RH
                        {solicitudSeleccionada.nombreAprobadorRH && (
                          <span className="text-muted-foreground text-sm ml-2">
                            ({solicitudSeleccionada.nombreAprobadorRH} {solicitudSeleccionada.primerApellidoAprobadorRH})
                          </span>
                        )}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {formatearFecha(solicitudSeleccionada.fechaAprobacionRH)}
                      </p>
                      {solicitudSeleccionada.comentariosRH && (
                        <p className="text-sm mt-1 italic">"{solicitudSeleccionada.comentariosRH}"</p>
                      )}
                    </div>
                  </div>
                )}
              </div>
            </div>

            <div className="flex justify-end pt-4">
              <Button onClick={() => setShowDetalleModal(false)}>
                Cerrar
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
