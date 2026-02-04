import { useState, useEffect, useRef } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Textarea } from '@/components/ui/textarea';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { DatePicker } from '@/components/ui/date-picker';
import { Modal } from '@/components/Modal';
import type { RespuestaIncapacidad } from '../../services/incapacidadesService';
import {
  crearSolicitud,
  obtenerMisSolicitudes,
  solicitarExtension
} from '../../services/incapacidadesService';
import { authService } from '../../services/authService';
import { formatearFecha, calcularDiasHabiles, parseContentDispositionFilename, buildIncapacidadFilename } from '../../lib/utils';
import { Calendar, Plus, Eye, FileText, Activity, AlertCircle, ArrowRightCircle } from 'lucide-react';
import { toast } from 'sonner';

const TIPOS_INCAPACIDAD = [
  { value: 'ENFERMEDAD_COMUN', label: 'Enfermedad Común' },
  { value: 'ACCIDENTE_LABORAL', label: 'Accidente Laboral' },
  { value: 'ACCIDENTE_TRANSITO', label: 'Accidente de Tránsito' },
  { value: 'LICENCIA_DE_MATERNIDAD', label: 'Licencia de Maternidad' },
  { value: 'LICENCIA_DE_PATERNIDAD', label: 'Licencia de Paternidad' },
];

const ENTIDADES_EMISORAS = [
  { value: 'CCSS', label: 'CCSS' },
  { value: 'INS', label: 'INS' },
  { value: 'CLINICA_PRIVADA', label: 'Clínica Privada' },
  { value: 'OTRO', label: 'Otro' },
];

// Helper functions for display
const getEstadoIncapacidadColor = (estado: string) => {
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

const getEstadoIncapacidadLabel = (estado: string) => {
  switch (estado) {
    case 'PENDIENTE':
      return 'Pendiente (Jefe)';
    case 'PENDIENTE_RH':
      return 'Pendiente (RH)';
    case 'APROBADA_POR_JEFE':
      return 'Aprobada por Jefe';
    case 'APROBADA':
      return 'Aprobada';
    case 'RECHAZADA_POR_JEFE':
      return 'Rechazada por Jefe';
    case 'RECHAZADA_POR_RH':
      return 'Rechazada por RH';
    case 'CANCELADA':
      return 'Cancelada';
    default:
      return estado;
  }
};

const getTipoIncapacidadLabel = (tipo: string) => {
  return TIPOS_INCAPACIDAD.find(t => t.value === tipo)?.label || tipo;
};

const getEntidadEmisoraLabel = (entidad: string) => {
  return ENTIDADES_EMISORAS.find(e => e.value === entidad)?.label || entidad;
};

export default function IncapacidadesView() {
  const [solicitudes, setSolicitudes] = useState<RespuestaIncapacidad[]>([]);
  const [loading, setLoading] = useState(true);
  const [showNuevaSolicitudModal, setShowNuevaSolicitudModal] = useState(false);
  const [showDetalleModal, setShowDetalleModal] = useState(false);
  const [showExtenderModal, setShowExtenderModal] = useState(false);
  const [solicitudSeleccionada, setSolicitudSeleccionada] = useState<RespuestaIncapacidad | null>(null);
  const [incapacidadAExtender, setIncapacidadAExtender] = useState<RespuestaIncapacidad | null>(null);
  const [nuevaFechaFin, setNuevaFechaFin] = useState('');
  const [diasAdicionales, setDiasAdicionales] = useState('');
  const [numeroDocumento, setNumeroDocumento] = useState('');
  const [observacionesExtension, setObservacionesExtension] = useState('');
  const [error, setError] = useState<string | null>(null);

  // Form state
  const [formData, setFormData] = useState({
    fechaInicio: '',
    fechaFin: '',
    diasTotales: 0,
    tipoIncapacidad: 'ENFERMEDAD_COMUN',
    porcentajePago: 50, // default for CCSS
    entidadEmisora: 'CCSS',
    numeroDocumento: '',
    observaciones: '',
  });
  const [archivoAdjunto, setArchivoAdjunto] = useState<File | null>(null);
  const fileInputRef = useRef<HTMLInputElement | null>(null);

  useEffect(() => {
    cargarSolicitudes();
  }, []);

  const cargarSolicitudes = async () => {
    try {
      setLoading(true);
      const data = await obtenerMisSolicitudes();
      setSolicitudes(data);
      setError(null);
    } catch (err: unknown) {
      console.error('Error al cargar solicitudes:', err);
      setError('Error al cargar las solicitudes');
    } finally {
      setLoading(false);
    }
  };

  const handleFechaChange = (campo: 'fechaInicio' | 'fechaFin', valor: string) => {
    const newFormData = { ...formData, [campo]: valor };
    
    // Calcular días automáticamente si ambas fechas están presentes
    if (newFormData.fechaInicio && newFormData.fechaFin) {
      const dias = calcularDiasHabiles(newFormData.fechaInicio, newFormData.fechaFin);
      newFormData.diasTotales = dias > 0 ? dias : 1;
    }
    
    setFormData(newFormData);
  };

  const handleSubmitNuevaSolicitud = async (e: React.FormEvent) => {
    e.preventDefault();
    
    // Validaciones
    if (!formData.fechaInicio) {
      toast.error('Debe seleccionar la fecha de inicio');
      return;
    }
    
    if (!formData.fechaFin) {
      toast.error('Debe seleccionar la fecha de fin');
      return;
    }
    
    // Validar que fechaFin >= fechaInicio
    if (formData.fechaFin < formData.fechaInicio) {
      toast.error('La fecha de fin debe ser igual o posterior a la fecha de inicio');
      return;
    }
    
    try {
      const userInfo = authService.getUserInfo();
      const idEmpleado = userInfo.idEmpleado;
      console.debug('userInfo:', userInfo, 'idEmpleado:', idEmpleado);
      
      if (!idEmpleado) {
        toast.error('Error: No se pudo obtener la información del empleado');
        return;
      }

      // Construir FormData siempre (el endpoint del backend espera multipart/form-data)
      const form = new FormData();
      form.append('fechaInicio', formData.fechaInicio);
      form.append('fechaFin', formData.fechaFin);
      form.append('diasTotales', String(formData.diasTotales > 0 ? formData.diasTotales : 1));
      form.append('tipoIncapacidad', formData.tipoIncapacidad);
      form.append('porcentajePago', String(formData.porcentajePago));
      form.append('entidadEmisora', formData.entidadEmisora);
      if (formData.numeroDocumento) form.append('numeroDocumento', formData.numeroDocumento);
      if (formData.observaciones) form.append('observaciones', formData.observaciones);
      form.append('idEmpleado', String(idEmpleado));

      // Si hay archivo, validarlo y anexarlo
      if (archivoAdjunto) {
        const MAX_SIZE = 5 * 1024 * 1024; // 5MB
        if (archivoAdjunto.size > MAX_SIZE) {
          toast.error('El archivo excede el tamaño máximo permitido de 5 MB');
          return;
        }
        const tipo = archivoAdjunto.type || '';
        if (!(tipo === 'application/pdf' || tipo.startsWith('image/'))) {
          toast.error('Tipo de archivo no permitido. Solo PDF o imágenes.');
          return;
        }

        form.append('archivo', archivoAdjunto);
      }

      // DEBUG: imprimir contenido del estado y del FormData antes de enviar
      console.debug('Form data state:', formData);
      const entries = Array.from(form.entries()).map(([k, v]) => [k, v instanceof File ? `(File) ${v.name}` : v]);
      console.debug('FormData entries:', entries);

      await crearSolicitud(form);

      // Éxito: cerrar modal, resetear formulario y recargar lista
      setShowNuevaSolicitudModal(false);
      resetForm();
      await cargarSolicitudes();
      toast.success('Solicitud creada correctamente');
    } catch (err: unknown) {
      console.error('Error al crear solicitud:', err);
      let errorMessage = 'Error al crear la solicitud';

      // Si es Error nativo
      if (err instanceof Error) {
        errorMessage = err.message;
      } else if (err && typeof err === 'object') {
        // El backend puede lanzar un objeto con {status, message}
        const anyErr: any = err;
        if (anyErr.message) {
          // Intentar parsear JSON si el backend devolvió un body JSON serializado
          try {
            const parsed = JSON.parse(anyErr.message);
            errorMessage = parsed.message || anyErr.message;
            // Si hay detalles de validación, añadirlos
            if (parsed.errors && Array.isArray(parsed.errors)) {
              errorMessage += '\n' + parsed.errors.map((e: any) => (e.defaultMessage || e.message || JSON.stringify(e))).join('\n');
            }
          } catch (_parseErr) {
            errorMessage = anyErr.message;
          }
        } else {
          errorMessage = JSON.stringify(anyErr);
        }
      } else if (typeof err === 'string') {
        errorMessage = err;
      }

      toast.error(errorMessage);
    }
  };

  const resetForm = () => {
    setFormData({
      fechaInicio: '',
      fechaFin: '',
      diasTotales: 0,
      tipoIncapacidad: 'ENFERMEDAD_COMUN',
      porcentajePago: 50,
      entidadEmisora: 'CCSS',
      numeroDocumento: '',
      observaciones: '',
    });
    setArchivoAdjunto(null);
    if (fileInputRef.current) fileInputRef.current.value = '';
  }; 

  const handleVerDetalle = (solicitud: RespuestaIncapacidad) => {
    setSolicitudSeleccionada(solicitud);
    setShowDetalleModal(true);
  };

  const handleExtender = (solicitud: RespuestaIncapacidad) => {
    setIncapacidadAExtender(solicitud);
    setShowExtenderModal(true);
    setNumeroDocumento('');
    setObservacionesExtension('');
    const fechaMinima = solicitud.fechaFin ? new Date(solicitud.fechaFin) : null;
    if (fechaMinima) {
      const siguiente = new Date(fechaMinima);
      siguiente.setDate(siguiente.getDate() + 1);
      const siguienteStr = siguiente.toISOString().slice(0, 10);
      setNuevaFechaFin(siguienteStr);
      setDiasAdicionales('1');
    } else {
      setNuevaFechaFin('');
      setDiasAdicionales('');
    }
  };

  const handleNuevaFechaFinChange = (fecha: string) => {
    setNuevaFechaFin(fecha);
    if (!incapacidadAExtender?.fechaFin || !fecha) {
      setDiasAdicionales('');
      return;
    }
    const finActual = new Date(incapacidadAExtender.fechaFin);
    const finNuevo = new Date(fecha);
    const diff = Math.round((finNuevo.getTime() - finActual.getTime()) / (1000 * 60 * 60 * 24));
    setDiasAdicionales(diff > 0 ? String(diff) : '');
  };

  const handleSolicitarExtension = async () => {
    if (!incapacidadAExtender) return;

    if (!nuevaFechaFin || !diasAdicionales) {
      toast.error('Complete fecha fin y días adicionales');
      return;
    }

    const diasNum = parseInt(diasAdicionales, 10);
    if (Number.isNaN(diasNum) || diasNum <= 0) {
      toast.error('Los días adicionales deben ser mayores a cero');
      return;
    }

    try {
      await solicitarExtension(incapacidadAExtender.id, {
        nuevaFechaFin,
        diasAdicionales: diasNum,
        numeroDocumento: numeroDocumento || undefined,
        observaciones: observacionesExtension || undefined,
      });
      toast.success('Extensión solicitada correctamente');
      setShowExtenderModal(false);
      setIncapacidadAExtender(null);
      setNuevaFechaFin('');
      setDiasAdicionales('');
      setNumeroDocumento('');
      setObservacionesExtension('');
      cargarSolicitudes();
    } catch (err: unknown) {
      console.error('Error al solicitar extensión:', err);
      const errorMessage = err instanceof Error ? err.message : 'Error al solicitar la extensión';
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

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Mis Incapacidades</h2>
          <p className="text-muted-foreground">
            Gestiona tus solicitudes de incapacidades médicas
          </p>
        </div>
        <Button onClick={() => setShowNuevaSolicitudModal(true)} className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Incapacidad
        </Button>
      </div>

      {error && (
        <div className="bg-red-100 border border-red-400 text-red-700 px-4 py-3 rounded flex items-center gap-2">
          <AlertCircle className="h-4 w-4" />
          {error}
        </div>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Historial de Incapacidades</CardTitle>
          <CardDescription>
            {solicitudes.length} incapacidad(es) en total
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
                  <th className="text-left p-3">Entidad</th>
                  <th className="text-left p-3">Estado</th>
                  <th className="text-center p-3">Acciones</th>
                </tr>
              </thead>
              <tbody>
                {solicitudes.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="text-center p-8 text-muted-foreground">
                      No hay incapacidades registradas
                    </td>
                  </tr>
                ) : (
                  solicitudes.map((solicitud) => (
                    <tr key={solicitud.id} className="border-b hover:bg-muted/50">
                      <td className="p-3">
                        <div className="flex items-center gap-2">
                          <Activity className="h-4 w-4 text-muted-foreground" />
                          {getTipoIncapacidadLabel(solicitud.tipoIncapacidad)}
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
                      <td className="p-3 text-sm">
                        {getEntidadEmisoraLabel(solicitud.entidadEmisora)}
                      </td>
                      <td className="p-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoIncapacidadColor(solicitud.estadoSolicitud)}`}>
                          {getEstadoIncapacidadLabel(solicitud.estadoSolicitud)}
                        </span>
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
                        {solicitud.estadoSolicitud === 'APROBADA' && (
                          <Button
                            variant="outline"
                            size="sm"
                            onClick={() => handleExtender(solicitud)}
                            className="gap-2 ml-2"
                          >
                            <ArrowRightCircle className="h-4 w-4" />
                            Extender
                          </Button>
                        )}
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
        title="Nueva Incapacidad"
      >
        <form onSubmit={handleSubmitNuevaSolicitud} className="space-y-4">
          {/* Tipo de Incapacidad */}
          <div>
            <Label htmlFor="tipoIncapacidad">Tipo de Incapacidad *</Label>
            <Select
              value={formData.tipoIncapacidad}
              onValueChange={(value) => setFormData({ ...formData, tipoIncapacidad: value })}
              required
            >
              <SelectTrigger>
                <SelectValue placeholder="Seleccione un tipo" />
              </SelectTrigger>
              <SelectContent>
                {TIPOS_INCAPACIDAD.map((tipo) => (
                  <SelectItem key={tipo.value} value={tipo.value}>
                    {tipo.label}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>

          {/* Fechas */}
          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label htmlFor="fechaInicio">Fecha Inicio *</Label>
              <DatePicker
                value={formData.fechaInicio}
                onChange={(fecha) => handleFechaChange('fechaInicio', fecha)}
                placeholder="Seleccionar fecha inicio"
                fromYear={new Date().getFullYear() - 1}
                toYear={new Date().getFullYear() + 1}
              />
            </div>
            <div>
              <Label htmlFor="fechaFin">Fecha Fin *</Label>
              <DatePicker
                value={formData.fechaFin}
                onChange={(fecha) => handleFechaChange('fechaFin', fecha)}
                placeholder="Seleccionar fecha fin"
                fromYear={new Date().getFullYear() - 1}
                toYear={new Date().getFullYear() + 1}
              />
            </div>
          </div>

          {/* Días calculados */}
          {formData.diasTotales > 0 && (
            <div className="p-3 bg-muted rounded-lg">
              <Label className="text-muted-foreground">Días de Incapacidad</Label>
              <p className="text-2xl font-bold text-primary">{formData.diasTotales} día(s)</p>
            </div>
          )}

          {/* Entidad Emisora */}
          <div className="grid grid-cols-1 gap-4">
            <div>
              <Label htmlFor="entidadEmisora">Entidad Emisora *</Label>
              <Select
                value={formData.entidadEmisora}
                onValueChange={(value) => {
                  const porcentaje = value === 'INS' ? 100 : value === 'CCSS' ? 50 : 0;
                  setFormData({ ...formData, entidadEmisora: value, porcentajePago: porcentaje });
                }}
                required
              >
                <SelectTrigger>
                  <SelectValue placeholder="Seleccione entidad" />
                </SelectTrigger>
                <SelectContent>
                  {ENTIDADES_EMISORAS.map((entidad) => (
                    <SelectItem key={entidad.value} value={entidad.value}>
                      {entidad.label}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
          </div>

          {/* Número de Documento */}
          <div>
            <Label htmlFor="numeroDocumento">Número de Documento</Label>
            <Input
              id="numeroDocumento"
              value={formData.numeroDocumento}
              onChange={(e) => setFormData({ ...formData, numeroDocumento: e.target.value })}
              placeholder="Ej: BLI-2026-001234"
              maxLength={100}
            />
          </div>

          {/* Documento Adjunto (solo carga de archivo) */}
          <div>
            <Label>Documento Adjunto (Opcional)</Label>
            <div className="mt-2">
              <input
                id="archivoAdjunto"
                ref={fileInputRef}
                type="file"
                accept="application/pdf,image/*"
                className="hidden"
                onChange={(e) => setArchivoAdjunto(e.target.files && e.target.files[0] ? e.target.files[0] : null)}
              />

              <div className="border border-dashed rounded-md p-3 flex items-center justify-between gap-4">
                <div className="text-sm text-muted-foreground">
                  {archivoAdjunto ? (
                    <span className="font-medium">{archivoAdjunto.name}</span>
                  ) : (
                    <span>No se ha seleccionado archivo</span>
                  )}
                </div>

                <div className="flex items-center gap-2">
                  <Button type="button" variant="outline" onClick={() => fileInputRef.current && fileInputRef.current.click()}>
                    Adjuntar archivo
                  </Button>
                  {archivoAdjunto && (
                    <Button type="button" variant="ghost" onClick={() => setArchivoAdjunto(null)}>
                      Quitar
                    </Button>
                  )}
                </div>
              </div>

              <p className="text-xs text-muted-foreground mt-1">Adjunte un PDF o imagen; el archivo se guardará internamente.</p>
            </div>
          </div>

          {/* Observaciones */}
          <div>
            <div className="flex justify-between items-center mb-2">
              <Label htmlFor="observaciones">Observaciones</Label>
              <span className="text-xs text-muted-foreground">{formData.observaciones.length}/1000</span>
            </div>
            <Textarea
              id="observaciones"
              value={formData.observaciones}
              onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
              placeholder="Información adicional sobre la incapacidad..."
              maxLength={1000}
              rows={3}
            />
          </div>

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

      {/* Modal Extender Incapacidad */}
      <Modal
        isOpen={showExtenderModal}
        onClose={() => {
          setShowExtenderModal(false);
          setIncapacidadAExtender(null);
        }}
        title="Extender Incapacidad"
      >
        {incapacidadAExtender && (
          <div className="space-y-4">
            <div className="p-3 bg-muted rounded-lg">
              <p className="text-sm text-muted-foreground">Incapacidad #{incapacidadAExtender.id}</p>
              <p className="font-semibold">
                {formatearFecha(incapacidadAExtender.fechaInicio)} - {formatearFecha(incapacidadAExtender.fechaFin)}
              </p>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label>Nueva fecha fin *</Label>
                <DatePicker
                  value={nuevaFechaFin}
                  onChange={(fecha) => handleNuevaFechaFinChange(fecha)}
                  placeholder="Seleccionar fecha"
                  fromYear={new Date().getFullYear() - 1}
                  toYear={new Date().getFullYear() + 2}
                />
              </div>
              <div>
                <Label>Días adicionales *</Label>
                <Input
                  type="number"
                  min={1}
                  value={diasAdicionales}
                  onChange={(e) => setDiasAdicionales(e.target.value)}
                  placeholder="Ej: 2"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label htmlFor="numeroDocumento">Número de documento</Label>
                <Input
                  id="numeroDocumento"
                  value={numeroDocumento}
                  onChange={(e) => setNumeroDocumento(e.target.value)}
                  placeholder="Opcional"
                />
              </div>
              <div>
                <Label htmlFor="observacionesExtension">Observaciones</Label>
                <Textarea
                  id="observacionesExtension"
                  value={observacionesExtension}
                  onChange={(e) => setObservacionesExtension(e.target.value)}
                  placeholder="Motivo o detalles de la extensión"
                  rows={3}
                />
              </div>
            </div>

            <div className="flex justify-end gap-2">
              <Button
                variant="outline"
                onClick={() => {
                  setShowExtenderModal(false);
                  setIncapacidadAExtender(null);
                }}
              >
                Cancelar
              </Button>
              <Button onClick={handleSolicitarExtension}>
                Enviar extensión
              </Button>
            </div>
          </div>
        )}
      </Modal>

      {/* Modal Detalle Solicitud */}
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
              <span className={`px-3 py-1 rounded-full text-sm font-medium ${getEstadoIncapacidadColor(solicitudSeleccionada.estadoSolicitud)}`}>
                {getEstadoIncapacidadLabel(solicitudSeleccionada.estadoSolicitud)}
              </span>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <Label className="text-muted-foreground">Tipo de Incapacidad</Label>
                <p className="font-medium">{getTipoIncapacidadLabel(solicitudSeleccionada.tipoIncapacidad)}</p>
              </div>
              <div>
                <Label className="text-muted-foreground">Días</Label>
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

            <div className="grid grid-cols-1 gap-4">
              <div>
                <Label className="text-muted-foreground">Entidad Emisora</Label>
                <p className="font-medium">{getEntidadEmisoraLabel(solicitudSeleccionada.entidadEmisora)}</p>
              </div>
            </div>

            {solicitudSeleccionada.numeroDocumento && (
              <div>
                <Label className="text-muted-foreground">Número de Documento</Label>
                <p className="font-medium">{solicitudSeleccionada.numeroDocumento}</p>
              </div>
            )}

            <div>
              <Label className="text-muted-foreground">Fecha de Solicitud</Label>
              <p className="font-medium">{formatearFecha(solicitudSeleccionada.fechaSolicitud)}</p>
            </div>

            {solicitudSeleccionada.observaciones && (
              <div>
                <Label className="text-muted-foreground">Observaciones</Label>
                <p className="text-sm">{solicitudSeleccionada.observaciones}</p>
              </div>
            )}

            {solicitudSeleccionada.urlDocumentoAdjunto && (
              <div>
                <Label className="text-muted-foreground">Documento Adjunto</Label>
                <a
                  href="#"
                  onClick={async (e) => {
                    e.preventDefault();
                    try {
                      const token = localStorage.getItem('token');
                      const apiUrl = solicitudSeleccionada.urlDocumentoAdjunto?.startsWith('http') ? solicitudSeleccionada.urlDocumentoAdjunto : `http://localhost:8080${solicitudSeleccionada.urlDocumentoAdjunto}`;
                      const res = await fetch(apiUrl as string, { headers: { 'Authorization': `Bearer ${token}` } });
                      if (!res.ok) {
                        const msg = await res.text();
                        throw new Error(msg || 'Error al descargar el archivo');
                      }
                      const blob = await res.blob();
                      const disp = res.headers.get('content-disposition') || '';
                      let filename = parseContentDispositionFilename(disp);
                      if (!filename) {
                        let ext = '';
                        try {
                          const path = new URL(apiUrl as string).pathname;
                          const dot = path.lastIndexOf('.');
                          if (dot > -1) ext = path.substring(dot);
                        } catch (e) {}
                        filename = buildIncapacidadFilename(solicitudSeleccionada?.id, solicitudSeleccionada?.nombreEmpleado, solicitudSeleccionada?.primerApellidoEmpleado, ext);
                      }
                      const href = URL.createObjectURL(blob);
                      const a = document.createElement('a');
                      a.href = href;
                      a.download = filename;
                      document.body.appendChild(a);
                      a.click();
                      a.remove();
                      URL.revokeObjectURL(href);
                    } catch (err) {
                      console.error(err);
                      toast.error('No se pudo descargar el archivo');
                    }
                  }
                  }
                  className="text-primary hover:underline flex items-center gap-1"
                >
                  <FileText className="h-4 w-4" />
                  Ver documento
                </a>
              </div>
            )}

            {/* Información de aprobación */}
            {(solicitudSeleccionada.comentariosJefe || solicitudSeleccionada.nombreAprobadorJefe) && (
              <div className="border-t pt-4">
                <h4 className="font-medium mb-2">Revisión del Jefe</h4>
                {solicitudSeleccionada.nombreAprobadorJefe && (
                  <p className="text-sm">
                    <span className="text-muted-foreground">Aprobador:</span>{' '}
                    {solicitudSeleccionada.nombreAprobadorJefe} {solicitudSeleccionada.primerApellidoAprobadorJefe}
                  </p>
                )}
                {solicitudSeleccionada.fechaAprobacionJefe && (
                  <p className="text-sm">
                    <span className="text-muted-foreground">Fecha:</span>{' '}
                    {formatearFecha(solicitudSeleccionada.fechaAprobacionJefe)}
                  </p>
                )}
                {solicitudSeleccionada.comentariosJefe && (
                  <p className="text-sm mt-1">
                    <span className="text-muted-foreground">Comentarios:</span>{' '}
                    {solicitudSeleccionada.comentariosJefe}
                  </p>
                )}
              </div>
            )}

            {(solicitudSeleccionada.comentariosRH || solicitudSeleccionada.nombreAprobadorRH) && (
              <div className="border-t pt-4">
                <h4 className="font-medium mb-2">Revisión de RH</h4>
                {solicitudSeleccionada.nombreAprobadorRH && (
                  <p className="text-sm">
                    <span className="text-muted-foreground">Aprobador:</span>{' '}
                    {solicitudSeleccionada.nombreAprobadorRH} {solicitudSeleccionada.primerApellidoAprobadorRH}
                  </p>
                )}
                {solicitudSeleccionada.fechaAprobacionRH && (
                  <p className="text-sm">
                    <span className="text-muted-foreground">Fecha:</span>{' '}
                    {formatearFecha(solicitudSeleccionada.fechaAprobacionRH)}
                  </p>
                )}
                {solicitudSeleccionada.comentariosRH && (
                  <p className="text-sm mt-1">
                    <span className="text-muted-foreground">Comentarios:</span>{' '}
                    {solicitudSeleccionada.comentariosRH}
                  </p>
                )}
              </div>
            )}

            <div className="flex justify-end">
              <Button
                variant="outline"
                onClick={() => {
                  setShowDetalleModal(false);
                  setSolicitudSeleccionada(null);
                }}
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
