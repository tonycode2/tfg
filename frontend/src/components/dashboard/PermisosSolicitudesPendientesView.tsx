import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Modal } from '@/components/Modal';
import type { RespuestaPermiso } from '@/services/permisosService';
import { 
  obtenerSolicitudesPendientesDepartamento,
  aprobarPorJefe,
  rechazarPorJefe
} from '@/services/permisosService';
import { obtenerSaldoEmpleado } from '@/services/vacacionesService';
import {
  getTipoPermisoLabel,
  formatearFecha,
  formatearHoras
} from '../../lib/utils';
import { Calendar, FileText, User, Eye, CheckCircle, XCircle, Clock, Palmtree } from 'lucide-react';
import { toast } from 'sonner';

export default function PermisosSolicitudesPendientesView() {
  const [solicitudes, setSolicitudes] = useState<RespuestaPermiso[]>([]);
  const [loading, setLoading] = useState(true);
  const [showRevisarModal, setShowRevisarModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaPermiso | null>(null);
  const [comentarios, setComentarios] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [saldoEmpleado, setSaldoEmpleado] = useState<number | null>(null);

  useEffect(() => {
    cargarSolicitudes();
  }, []);

  const cargarSolicitudes = async () => {
    try {
      setLoading(true);
      const data = await obtenerSolicitudesPendientesDepartamento();
      setSolicitudes(data);
      setError(null);
    } catch (err: any) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes pendientes');
    } finally {
      setLoading(false);
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

  const handleAprobar = async () => {
    if (!solicitudSeleccionada) return;

    try {
      setProcesando(true);
      await aprobarPorJefe(solicitudSeleccionada.id, { comentarios });
      toast.success('Solicitud aprobada exitosamente');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarSolicitudes();
    } catch (err: any) {
      console.error('Error al aprobar solicitud:', err);
      toast.error(err.response?.data?.message || 'Error al aprobar la solicitud');
    } finally {
      setProcesando(false);
    }
  };

  const handleRechazar = async () => {
    if (!solicitudSeleccionada) return;

    if (!comentarios || comentarios.trim().length < 10) {
      toast.error('Por favor, proporcione comentarios (mínimo 10 caracteres)');
      return;
    }

    if (!confirm('¿Está seguro de que desea rechazar esta solicitud?')) {
      return;
    }

    try {
      setProcesando(true);
      await rechazarPorJefe(solicitudSeleccionada.id, { comentarios });
      toast.success('Solicitud rechazada');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarSolicitudes();
    } catch (err: any) {
      console.error('Error al rechazar solicitud:', err);
      toast.error(err.response?.data?.message || 'Error al rechazar la solicitud');
    } finally {
      setProcesando(false);
    }
  };

  if (loading) {
    return (
      <div className="flex justify-center items-center h-64">
        <div className="text-lg">Cargando solicitudes pendientes...</div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-3xl font-bold tracking-tight">Solicitudes Pendientes</h2>
        <p className="text-muted-foreground">
          Revise y apruebe/rechace las solicitudes de su departamento
        </p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded">
          {error}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Solicitudes de Permisos Pendientes</CardTitle>
          <CardDescription>
            {solicitudes.length} solicitud(es) pendiente(s) de revisión
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
                  <th className="text-left p-3">Motivo</th>
                  <th className="text-center p-3">Documento</th>
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudes.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center p-8 text-muted-foreground">
                      No hay solicitudes pendientes de revisión
                    </td>
                  </tr>
                ) : (
                  solicitudes.map((solicitud) => (
                    <tr key={solicitud.id} className="border-b hover:bg-muted/50">
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <User className="h-4 w-4 text-muted-foreground" />
                          <div>
                            <div className="font-medium">
                              {solicitud.nombreEmpleado} {solicitud.primerApellidoEmpleado}
                            </div>
                            <div className="text-sm text-muted-foreground">
                              {solicitud.segundApellidoEmpleado}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <FileText className="h-4 w-4 text-muted-foreground" />
                          {getTipoPermisoLabel(solicitud.tipoPermiso)}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2 text-sm">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          <div>
                            <div>{formatearFecha(solicitud.fechaInicio)}</div>
                            {solicitud.unidadTiempo === 'HORAS' ? (
                              <div className="text-muted-foreground text-xs">
                                {solicitud.horaInicio} - {solicitud.horaFin}
                              </div>
                            ) : (
                              <>
                                <div className="text-muted-foreground">hasta</div>
                                <div>{formatearFecha(solicitud.fechaFin)}</div>
                              </>
                            )}
                          </div>
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        {solicitud.unidadTiempo === 'HORAS' ? (
                          <span className="font-semibold text-lg">{formatearHoras(solicitud.totalHoras || 0)}</span>
                        ) : (
                          <span className="font-semibold text-lg">{solicitud.diasTotales}</span>
                        )}
                      </td>
                      <td className="p-3">
                        <div className="max-w-xs truncate text-sm">
                          {solicitud.motivo || '-'}
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        {solicitud.urlDocumentoAdjunto ? (
                          <a
                            href={solicitud.urlDocumentoAdjunto}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-primary hover:underline text-sm"
                          >
                            Ver
                          </a>
                        ) : (
                          <span className="text-muted-foreground text-sm">-</span>
                        )}
                      </td>
                      <td className="p-3 text-center">
                        <Button
                          variant="default"
                          size="sm"
                          onClick={() => handleRevisar(solicitud)}
                          className="gap-2"
                        >
                          <Eye className="h-4 w-4" />
                          Revisar
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
        title="Revisar Solicitud de Permiso"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="bg-muted/50 p-4 rounded-lg">
              <h3 className="font-semibold mb-3 flex items-center gap-2">
                <User className="h-4 w-4" />
                Información del Solicitante
              </h3>
              <div className="grid grid-cols-2 gap-3 text-sm">
                <div>
                  <Label className="text-muted-foreground">Nombre Completo</Label>
                  <p className="font-medium">
                    {solicitudSeleccionada.nombreEmpleado} {solicitudSeleccionada.primerApellidoEmpleado} {solicitudSeleccionada.segundApellidoEmpleado}
                  </p>
                </div>
                <div>
                  <Label className="text-muted-foreground">Fecha de Solicitud</Label>
                  <p className="font-medium flex items-center gap-1">
                    <Clock className="h-3 w-3" />
                    {formatearFecha(solicitudSeleccionada.fechaSolicitud)}
                  </p>
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
              <p className="font-medium bg-muted/30 p-3 rounded">{solicitudSeleccionada.motivo}</p>
            </div>

            <div>
              <Label className="text-muted-foreground">Observaciones del Empleado</Label>
              <p className="font-medium bg-muted/30 p-3 rounded">{solicitudSeleccionada.observacionesEmpleado}</p>
            </div>

            {solicitudSeleccionada.urlDocumentoAdjunto && (
              <div>
                <Label className="text-muted-foreground">Documento Adjunto</Label>
                <a
                  href={solicitudSeleccionada.urlDocumentoAdjunto}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="text-primary hover:underline flex items-center gap-2 mt-1"
                >
                  <FileText className="h-4 w-4" />
                  Ver documento adjunto
                </a>
              </div>
            )}

            <div className="border-t pt-4">
              <Label htmlFor="comentarios">
                Comentarios (Opcional)
              </Label>
              <Textarea
                id="comentarios"
                value={comentarios}
                onChange={(e) => setComentarios(e.target.value)}
                placeholder="Agregue comentarios sobre su decisión..."
                maxLength={500}
                rows={4}
                disabled={procesando}
              />
              <p className="text-xs text-muted-foreground mt-1">
                {comentarios.length}/500 caracteres
                {comentarios.length > 0 && comentarios.length < 10 && (
                  <span className="text-yellow-600 ml-2">
                    (Mínimo 10 caracteres requeridos para rechazar)
                  </span>
                )}
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
                onClick={handleRechazar}
                disabled={procesando}
                className="gap-2"
              >
                <XCircle className="h-4 w-4" />
                Rechazar
              </Button>
              <Button
                type="button"
                variant="default"
                onClick={handleAprobar}
                disabled={procesando}
                className="gap-2 bg-green-600 hover:bg-green-700"
              >
                <CheckCircle className="h-4 w-4" />
                Aprobar
              </Button>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
