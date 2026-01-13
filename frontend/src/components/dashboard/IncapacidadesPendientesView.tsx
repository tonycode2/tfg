import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Modal } from '@/components/Modal';
import type { RespuestaIncapacidad } from '@/services/incapacidadesService';
import { 
  obtenerSolicitudesPendientesDepartamento,
  aprobarPorJefe,
  rechazarPorJefe
} from '@/services/incapacidadesService';
import { formatearFecha } from '../../lib/utils';
import { Calendar, FileText, User, Eye, CheckCircle, XCircle, Clock, Activity, AlertCircle } from 'lucide-react';

const TIPOS_INCAPACIDAD: Record<string, string> = {
  'ENFERMEDAD_COMUN': 'Enfermedad Común',
  'ACCIDENTE_LABORAL': 'Accidente Laboral',
  'ACCIDENTE_TRANSITO': 'Accidente de Tránsito',
  'MATERNIDAD': 'Maternidad',
  'RIESGO_EMBARAZO': 'Riesgo de Embarazo',
  'ENFERMEDAD_PROFESIONAL': 'Enfermedad Profesional',
};

const ENTIDADES_EMISORAS: Record<string, string> = {
  'CCSS': 'CCSS',
  'INS': 'INS',
  'CLINICA_PRIVADA': 'Clínica Privada',
  'OTRO': 'Otro',
};

const getTipoIncapacidadLabel = (tipo: string) => TIPOS_INCAPACIDAD[tipo] || tipo;
const getEntidadEmisoraLabel = (entidad: string) => ENTIDADES_EMISORAS[entidad] || entidad;

export default function IncapacidadesPendientesView() {
  const [solicitudes, setSolicitudes] = useState<RespuestaIncapacidad[]>([]);
  const [loading, setLoading] = useState(true);
  const [showRevisarModal, setShowRevisarModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaIncapacidad | null>(null);
  const [comentarios, setComentarios] = useState('');
  const [procesando, setProcesando] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    cargarSolicitudes();
  }, []);

  const cargarSolicitudes = async () => {
    try {
      setLoading(true);
      const data = await obtenerSolicitudesPendientesDepartamento();
      setSolicitudes(data);
      setError(null);
    } catch (err: unknown) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes pendientes');
    } finally {
      setLoading(false);
    }
  };

  const handleRevisar = (solicitud: RespuestaIncapacidad) => {
    setSolicitudSeleccionada(solicitud);
    setComentarios('');
    setShowRevisarModal(true);
  };

  const handleAprobar = async () => {
    if (!solicitudSeleccionada) return;

    try {
      setProcesando(true);
      await aprobarPorJefe(solicitudSeleccionada.id, { comentarios });
      alert('Solicitud de incapacidad aprobada exitosamente');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarSolicitudes();
    } catch (err: unknown) {
      console.error('Error al aprobar solicitud:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al aprobar la solicitud';
      alert(errorMessage);
    } finally {
      setProcesando(false);
    }
  };

  const handleRechazar = async () => {
    if (!solicitudSeleccionada) return;

    if (!comentarios || comentarios.trim().length < 10) {
      alert('Por favor, proporcione comentarios (mínimo 10 caracteres) para rechazar la solicitud');
      return;
    }

    if (!confirm('¿Está seguro de que desea rechazar esta solicitud de incapacidad?')) {
      return;
    }

    try {
      setProcesando(true);
      await rechazarPorJefe(solicitudSeleccionada.id, { comentarios });
      alert('Solicitud de incapacidad rechazada');
      setShowRevisarModal(false);
      setSolicitudSeleccionada(null);
      setComentarios('');
      cargarSolicitudes();
    } catch (err: unknown) {
      console.error('Error al rechazar solicitud:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al rechazar la solicitud';
      alert(errorMessage);
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
        <h2 className="text-3xl font-bold tracking-tight">Incapacidades Pendientes</h2>
        <p className="text-muted-foreground">
          Revise y apruebe/rechace las solicitudes de incapacidad de su departamento
        </p>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          {error}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Activity className="h-5 w-5" />
            Solicitudes de Incapacidad Pendientes
          </CardTitle>
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
                  <th className="text-left p-3">Entidad</th>
                  <th className="text-center p-3">Documento</th>
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudes.length === 0 ? (
                  <tr>
                    <td colSpan={7} className="text-center p-8 text-muted-foreground">
                      No hay solicitudes de incapacidad pendientes de revisión
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
                              {solicitud.segundoApellidoEmpleado}
                            </div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <Activity className="h-4 w-4 text-muted-foreground" />
                          {getTipoIncapacidadLabel(solicitud.tipoIncapacidad)}
                        </div>
                      </td>
                      <td className="p-3">
                        <div className="flex items-center gap-2 text-sm">
                          <Calendar className="h-4 w-4 text-muted-foreground" />
                          <div>
                            <div>{formatearFecha(solicitud.fechaInicio)}</div>
                            <div className="text-muted-foreground">hasta</div>
                            <div>{formatearFecha(solicitud.fechaFin)}</div>
                          </div>
                        </div>
                      </td>
                      <td className="p-3 text-center">
                        <span className="font-semibold">{solicitud.diasTotales}</span>
                      </td>
                      <td className="p-3 text-sm">
                        {getEntidadEmisoraLabel(solicitud.entidadEmisora)}
                      </td>
                      <td className="p-3 text-center">
                        {solicitud.urlDocumentoAdjunto ? (
                          <a
                            href={solicitud.urlDocumentoAdjunto}
                            target="_blank"
                            rel="noopener noreferrer"
                            className="text-primary hover:underline"
                          >
                            <FileText className="h-4 w-4 inline" />
                          </a>
                        ) : (
                          <span className="text-muted-foreground">-</span>
                        )}
                      </td>
                      <td className="p-3 text-center">
                        <Button
                          variant="outline"
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
          setShowRevisarModal(false);
          setSolicitudSeleccionada(null);
          setComentarios('');
        }}
        title="Revisar Solicitud de Incapacidad"
      >
        {solicitudSeleccionada && (
          <div className="space-y-4">
            <div className="flex items-center justify-between">
              <h3 className="text-lg font-semibold">
                Incapacidad #{solicitudSeleccionada.id}
              </h3>
              <span className="px-3 py-1 rounded-full text-sm font-medium bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-200">
                <Clock className="h-3 w-3 inline mr-1" />
                Pendiente
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
                <Label className="text-muted-foreground">Días Solicitados</Label>
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

            {/* Comentarios del jefe */}
            <div>
              <div className="flex justify-between items-center mb-2">
                <Label htmlFor="comentarios">Comentarios (opcional para aprobar, requerido para rechazar)</Label>
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
                onClick={handleRechazar}
                disabled={procesando}
                className="gap-2"
              >
                <XCircle className="h-4 w-4" />
                Rechazar
              </Button>
              <Button
                onClick={handleAprobar}
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
    </div>
  );
}
