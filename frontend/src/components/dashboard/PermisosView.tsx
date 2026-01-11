import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { Modal } from '@/components/Modal';
import type { RespuestaPermiso } from '../../services/permisosService';
import { 
  crearSolicitud, 
  obtenerMisSolicitudes 
} from '../../services/permisosService';
import { authService } from '../../services/authService';
import {
  getEstadoPermisoColor,
  getEstadoPermisoLabel,
  getTipoPermisoLabel,
  calcularDiasHabiles,
  formatearFecha
} from '../../lib/utils';
import { Calendar, Plus, Eye, FileText, Clock, CheckCircle, XCircle } from 'lucide-react';

const TIPOS_PERMISO = [
  { value: 'PERSONAL', label: 'Personal' },
  { value: 'MEDICO', label: 'Médico' },
  { value: 'LUTO', label: 'Luto' },
  { value: 'MATERNIDAD', label: 'Maternidad' },
  { value: 'PATERNIDAD', label: 'Paternidad' },
  { value: 'ESTUDIO', label: 'Estudio' },
  { value: 'SIN_GOCE_SALARIO', label: 'Sin Goce de Salario' },
];

export default function PermisosView() {
  const [solicitudes, setSolicitudes] = useState<RespuestaPermiso[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNuevaSolicitudModal, setShowNuevaSolicitudModal] = useState(false);
  const [showDetalleModal, setShowDetalleModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaPermiso | null>(null);
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    fechaInicio: '',
    fechaFin: '',
    diasTotales: 0,
    tipoPermiso: 'PERSONAL',
    motivo: '',
    urlDocumentoAdjunto: '',
  });

  useEffect(() => {
    cargarSolicitudes();
  }, []);

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
    
    // Calcular días automáticamente si ambas fechas están llenas
    if (newFormData.fechaInicio && newFormData.fechaFin) {
      const dias = calcularDiasHabiles(newFormData.fechaInicio, newFormData.fechaFin);
      newFormData.diasTotales = dias;
    }
    
    setFormData(newFormData);
  };

  const handleSubmitNuevaSolicitud = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.fechaInicio || !formData.fechaFin) {
      alert('Debe seleccionar ambas fechas');
      return;
    }
    
    if (formData.motivo.length < 10) {
      alert('El motivo debe tener al menos 10 caracteres');
      return;
    }
    
    // Validar que la fecha de inicio sea futura
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    const fechaInicio = new Date(formData.fechaInicio);
    
    if (fechaInicio < hoy) {
      alert('No se permiten solicitudes con fechas pasadas');
      return;
    }
    
    try {
      // Obtener idEmpleado del token JWT decodificado
      const userInfo = authService.getUserInfo();
      const idEmpleado = userInfo.idEmpleado;
      
      if (!idEmpleado) {
        alert('Error: No se pudo obtener la información del empleado');
        return;
      }
      
      await crearSolicitud({
        ...formData,
        idEmpleado,
        urlDocumentoAdjunto: formData.urlDocumentoAdjunto || undefined,
      });
      
      alert('Solicitud creada exitosamente');
      setShowNuevaSolicitudModal(false);
      resetForm();
      cargarSolicitudes();
    } catch (err: any) {
      console.error('Error al crear solicitud:', err);
      alert(err.response?.data?.message || 'Error al crear la solicitud');
    }
  };

  const resetForm = () => {
    setFormData({
      fechaInicio: '',
      fechaFin: '',
      diasTotales: 0,
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
                        <span className="font-semibold">{solicitud.diasTotales}</span>
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
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="fechaInicio">Fecha Inicio *</Label>
              <Input
                id="fechaInicio"
                type="date"
                value={formData.fechaInicio}
                onChange={(e) => handleFechaChange('fechaInicio', e.target.value)}
                required
                min={new Date().toISOString().split('T')[0]}
              />
            </div>
            <div>
              <Label htmlFor="fechaFin">Fecha Fin *</Label>
              <Input
                id="fechaFin"
                type="date"
                value={formData.fechaFin}
                onChange={(e) => handleFechaChange('fechaFin', e.target.value)}
                required
                min={formData.fechaInicio || new Date().toISOString().split('T')[0]}
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

          <div>
            <Label htmlFor="tipoPermiso">Tipo de Permiso *</Label>
            <Select
              value={formData.tipoPermiso}
              onValueChange={(value) => setFormData({ ...formData, tipoPermiso: value })}
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
                <Label className="text-muted-foreground">Días Solicitados</Label>
                <p className="font-medium">{solicitudSeleccionada.diasTotales} días hábiles</p>
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
