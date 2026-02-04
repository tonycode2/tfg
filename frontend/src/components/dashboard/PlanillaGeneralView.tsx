import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { planillasService, type PlanillaEncabezado, type PlanillaDetalleGeneral } from '@/services/apiService';
import { toast } from 'sonner';

const CalendarIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M8 7V3m8 4V3m-9 8h10M5 21h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v12a2 2 0 002 2z" />
  </svg>
);

const DocumentIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
  </svg>
);

const InfoIcon = () => (
  <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const buildDateString = (year: number, month: number, day: number): string => {
  const monthValue = String(month).padStart(2, '0');
  const dayValue = String(day).padStart(2, '0');
  return `${year}-${monthValue}-${dayValue}`;
};

const getLastDayOfMonth = (year: number, month: number): number => {
  return new Date(year, month, 0).getDate();
};

const getQuincenaLabel = (tipoQuincena: string | undefined): string => {
  if (tipoQuincena === 'PRIMERA') {
    return 'Primera quincena (1 al 15)';
  }
  if (tipoQuincena === 'SEGUNDA') {
    return 'Segunda quincena (16 al último día)';
  }
  return 'Sin definir';
};

const formatDate = (dateString: string | undefined): string => {
  if (!dateString) return 'No seleccionada';
  const date = new Date(dateString + 'T00:00:00');
  return date.toLocaleDateString('es-CR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const formatCurrency = (value: number | undefined): string => {
  const amount = typeof value === 'number' && !Number.isNaN(value) ? value : 0;
  return `₡${amount.toLocaleString('es-CR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const getEstadoBadge = (estado: string | undefined): string => {
  if (!estado) return 'bg-gray-100 text-gray-800';
  
  const estadoMap: Record<string, string> = {
    'BORRADOR': 'bg-gray-100 text-gray-800',
    'EN_REVISION': 'bg-yellow-100 text-yellow-800',
    'APROBADA': 'bg-blue-100 text-blue-800',
    'PAGADA': 'bg-green-100 text-green-800',
    'CANCELADA': 'bg-red-100 text-red-800',
  };
  
  return estadoMap[estado] || 'bg-gray-100 text-gray-800';
};

const getEstadoLabel = (estado: string | undefined): string => {
  if (!estado) return 'Desconocido';
  
  const estadoLabels: Record<string, string> = {
    'BORRADOR': 'Borrador',
    'EN_REVISION': 'En Revisión',
    'APROBADA': 'Aprobada',
    'PAGADA': 'Pagada',
    'CANCELADA': 'Cancelada',
  };
  
  return estadoLabels[estado] || estado;
};

export function PlanillaGeneralView() {
  const currentYear = new Date().getFullYear();
  const [mes, setMes] = useState<string>('');
  const [anio, setAnio] = useState<number>(currentYear);
  const [tipoQuincena, setTipoQuincena] = useState<string>('');
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [fechaFin, setFechaFin] = useState<string>('');
  const [fechaPago, setFechaPago] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [planillas, setPlanillas] = useState<PlanillaEncabezado[]>([]);
  const [loadingPlanillas, setLoadingPlanillas] = useState(true);
  const [selectedPlanilla, setSelectedPlanilla] = useState<PlanillaEncabezado | null>(null);
  const [detallesPlanilla, setDetallesPlanilla] = useState<PlanillaDetalleGeneral[]>([]);
  const [loadingDetalles, setLoadingDetalles] = useState(false);
  const [detallesError, setDetallesError] = useState<string | null>(null);

  useEffect(() => {
    cargarPlanillas();
  }, []);

  useEffect(() => {
    if (!mes || !tipoQuincena || !anio) {
      setFechaInicio('');
      setFechaFin('');
      setFechaPago('');
      return;
    }

    const monthValue = Number(mes);
    const lastDay = getLastDayOfMonth(anio, monthValue);
    const inicio = tipoQuincena === 'PRIMERA' ? 1 : 16;
    const fin = tipoQuincena === 'PRIMERA' ? 15 : lastDay;

    setFechaInicio(buildDateString(anio, monthValue, inicio));
    setFechaFin(buildDateString(anio, monthValue, fin));
    setFechaPago(buildDateString(anio, monthValue, lastDay));
  }, [mes, tipoQuincena, anio]);

  useEffect(() => {
    if (!selectedPlanilla?.id) {
      setDetallesPlanilla([]);
      setDetallesError(null);
      return;
    }

    const controller = new AbortController();

    const cargarDetalles = async () => {
      try {
        setLoadingDetalles(true);
        setDetallesError(null);
        const data = await planillasService.getDetallesPorPlanilla(selectedPlanilla.id, controller.signal);
        const detallesArray = Array.isArray(data) ? data : [];
        setDetallesPlanilla(detallesArray);
      } catch (error: any) {
        if (error?.name === 'AbortError') {
          return;
        }
        console.error('Error al cargar detalles de planilla:', error);
        setDetallesError(error.message || 'Ocurrió un error inesperado');
        setDetallesPlanilla([]);
      } finally {
        setLoadingDetalles(false);
      }
    };

    cargarDetalles();

    return () => controller.abort();
  }, [selectedPlanilla?.id]);

  const cargarPlanillas = async () => {
    try {
      setLoadingPlanillas(true);
      const data = await planillasService.getAllUnpaginated();
      const planillasArray = Array.isArray(data) ? data : [];
      // Ordenar por fecha más reciente primero
      planillasArray.sort((a, b) => {
        const dateA = new Date(a.fechaInicioPeriodo);
        const dateB = new Date(b.fechaInicioPeriodo);
        return dateB.getTime() - dateA.getTime();
      });
      setPlanillas(planillasArray);
    } catch (error: any) {
      console.error('Error al cargar planillas:', error);
      toast.error('Error al cargar las planillas', {
        description: error.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoadingPlanillas(false);
    }
  };

  const handleCrearPlanilla = async () => {
    if (!mes || !tipoQuincena || !anio) {
      toast.error('Por favor selecciona el mes, la quincena y el año');
      return;
    }

    try {
      setLoading(true);

      const planillaData = {
        mes: Number(mes),
        anio,
        tipoQuincena,
      };

      await planillasService.generarPlanilla(planillaData);
      
      toast.success('Planilla creada exitosamente', {
        description: `Periodo: ${formatDate(fechaInicio)} al ${formatDate(fechaFin)}`,
      });

      // Limpiar formulario
      setMes('');
      setTipoQuincena('');

      await cargarPlanillas();
    } catch (error: any) {
      console.error('Error al crear planilla:', error);
      toast.error('Error al crear la planilla', {
        description: error.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoading(false);
    }
  };

  const canCreatePlanilla = mes && tipoQuincena && anio && !loading;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <DocumentIcon />
            </div>
            <div>
              <CardTitle>Planilla General</CardTitle>
              <CardDescription>
                Gestión de planillas generales de la empresa
              </CardDescription>
            </div>
          </div>
        </CardHeader>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Crear Nueva Planilla</CardTitle>
          <CardDescription>
            Selecciona el mes y la quincena para generar la planilla
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Mes */}
            <div className="space-y-2">
              <Label htmlFor="mes" className="flex items-center gap-2">
                <CalendarIcon />
                Mes
              </Label>
              <Select value={mes} onValueChange={setMes}>
                <SelectTrigger id="mes">
                  <SelectValue placeholder="Seleccionar mes" />
                </SelectTrigger>
                <SelectContent className="max-h-56 overflow-y-auto">
                  <SelectItem value="1">Enero</SelectItem>
                  <SelectItem value="2">Febrero</SelectItem>
                  <SelectItem value="3">Marzo</SelectItem>
                  <SelectItem value="4">Abril</SelectItem>
                  <SelectItem value="5">Mayo</SelectItem>
                  <SelectItem value="6">Junio</SelectItem>
                  <SelectItem value="7">Julio</SelectItem>
                  <SelectItem value="8">Agosto</SelectItem>
                  <SelectItem value="9">Septiembre</SelectItem>
                  <SelectItem value="10">Octubre</SelectItem>
                  <SelectItem value="11">Noviembre</SelectItem>
                  <SelectItem value="12">Diciembre</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Quincena */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <CalendarIcon />
                Quincena
              </Label>
              <Select value={tipoQuincena} onValueChange={setTipoQuincena}>
                <SelectTrigger>
                  <SelectValue placeholder="Seleccionar quincena" />
                </SelectTrigger>
                <SelectContent className="max-h-56 overflow-y-auto">
                  <SelectItem value="PRIMERA">Primera quincena (1 al 15)</SelectItem>
                  <SelectItem value="SEGUNDA">Segunda quincena (16 al último día)</SelectItem>
                </SelectContent>
              </Select>
            </div>

            {/* Año */}
            <div className="space-y-2">
              <Label htmlFor="anio" className="flex items-center gap-2">
                <CalendarIcon />
                Año
              </Label>
              <Input
                id="anio"
                type="number"
                min={2000}
                max={2100}
                value={anio}
                onChange={(event) => setAnio(Number(event.target.value))}
              />
            </div>
          </div>

          {/* Resumen */}
          {fechaInicio && fechaFin && (
            <div className="p-4 bg-muted rounded-lg space-y-2">
              <h4 className="font-semibold">Resumen de la Planilla</h4>
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4 text-sm">
                <div>
                  <span className="text-muted-foreground">Periodo:</span>
                  <p className="font-medium">
                    {formatDate(fechaInicio)} al {formatDate(fechaFin)}
                  </p>
                </div>
                <div>
                  <span className="text-muted-foreground">Duración:</span>
                  <p className="font-medium">{getQuincenaLabel(tipoQuincena)}</p>
                </div>
                <div>
                  <span className="text-muted-foreground">Fecha de Pago:</span>
                  <p className="font-medium">{formatDate(fechaPago)}</p>
                </div>
              </div>
            </div>
          )}

          <div className="flex justify-end gap-3">
            <Button
              variant="outline"
              onClick={() => {
                setMes('');
                setTipoQuincena('');
              }}
              disabled={!mes && !tipoQuincena}
            >
              Limpiar
            </Button>
            <Button
              onClick={handleCrearPlanilla}
              disabled={!canCreatePlanilla}
            >
              {loading ? 'Creando...' : 'Crear Planilla'}
            </Button>
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle>Planillas Generadas</CardTitle>
          <CardDescription>
            Listado de todas las planillas creadas en el sistema
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loadingPlanillas ? (
            <div className="text-center py-8 text-muted-foreground">Cargando planillas...</div>
          ) : planillas.length === 0 ? (
            <Alert>
              <InfoIcon />
              <AlertDescription>
                No hay planillas creadas aún. Crea una nueva planilla para comenzar.
              </AlertDescription>
            </Alert>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-muted">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">ID</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Periodo</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Fecha de Pago</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Total Bruto</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Total Neto</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Estado</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Acciones</th>
                  </tr>
                </thead>
                <tbody className="bg-card divide-y divide-border">
                  {planillas.map((planilla) => (
                    <tr
                      key={planilla.id}
                      className={`hover:bg-muted/50 transition-colors ${
                        selectedPlanilla?.id === planilla.id ? 'bg-primary/10' : ''
                      }`}
                    >
                      <td className="px-4 py-3 font-mono text-sm">{planilla.id}</td>
                      <td className="px-4 py-3">
                        <div className="font-medium">{formatDate(planilla.fechaInicioPeriodo)}</div>
                        <div className="text-sm text-muted-foreground">al {formatDate(planilla.fechaFinPeriodo)}</div>
                        <div className="text-xs text-muted-foreground">{getQuincenaLabel(planilla.tipoQuincena)}</div>
                      </td>
                      <td className="px-4 py-3">{formatDate(planilla.fechaPago)}</td>
                      <td className="px-4 py-3 font-semibold">{formatCurrency(planilla.totalPlanillaBruto)}</td>
                      <td className="px-4 py-3 font-semibold text-green-600">{formatCurrency(planilla.totalPlanillaNeto)}</td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoBadge(planilla.estadoPlanilla)}`}>
                          {getEstadoLabel(planilla.estadoPlanilla)}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => setSelectedPlanilla(selectedPlanilla?.id === planilla.id ? null : planilla)}
                        >
                          {selectedPlanilla?.id === planilla.id ? 'Ocultar' : 'Ver Detalle'}
                        </Button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>

      {selectedPlanilla && (
        <Card>
          <CardHeader>
            <CardTitle>Detalle de Planilla #{selectedPlanilla.id}</CardTitle>
            <CardDescription>
              Periodo: {formatDate(selectedPlanilla.fechaInicioPeriodo)} al {formatDate(selectedPlanilla.fechaFinPeriodo)}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Información General</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">ID de Planilla:</span>
                    <span className="font-medium">{selectedPlanilla.id}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Fecha de Inicio:</span>
                    <span className="font-medium">{formatDate(selectedPlanilla.fechaInicioPeriodo)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Fecha de Fin:</span>
                    <span className="font-medium">{formatDate(selectedPlanilla.fechaFinPeriodo)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Fecha de Pago:</span>
                    <span className="font-medium">{formatDate(selectedPlanilla.fechaPago)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Quincena:</span>
                    <span className="font-medium">{getQuincenaLabel(selectedPlanilla.tipoQuincena)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t">
                    <span className="text-muted-foreground">Estado:</span>
                    <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoBadge(selectedPlanilla.estadoPlanilla)}`}>
                      {getEstadoLabel(selectedPlanilla.estadoPlanilla)}
                    </span>
                  </div>
                </div>
              </div>

              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Totales</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Total Bruto:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.totalPlanillaBruto)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t">
                    <span className="font-semibold">Total Neto:</span>
                    <span className="font-semibold text-green-600">{formatCurrency(selectedPlanilla.totalPlanillaNeto)}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-8 space-y-4">
              <div className="flex items-center justify-between gap-4">
                <h3 className="font-semibold text-lg">Detalle por Empleado</h3>
                <span className="text-sm text-muted-foreground">
                  {detallesPlanilla.length} empleados
                </span>
              </div>

              {loadingDetalles ? (
                <div className="text-center py-6 text-muted-foreground">Cargando detalles...</div>
              ) : detallesError ? (
                <Alert>
                  <InfoIcon />
                  <AlertDescription>{detallesError}</AlertDescription>
                </Alert>
              ) : detallesPlanilla.length === 0 ? (
                <Alert>
                  <InfoIcon />
                  <AlertDescription>No hay detalles disponibles para esta planilla.</AlertDescription>
                </Alert>
              ) : (
                <div className="overflow-x-auto">
                  <table className="w-full">
                    <thead className="bg-muted">
                      <tr>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Empleado</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Salario Base</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Horas Extra</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Incapacidad</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Total Devengado</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Deducciones</th>
                        <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Salario Neto</th>
                      </tr>
                    </thead>
                    <tbody className="bg-card divide-y divide-border">
                      {detallesPlanilla.map((detalle) => {
                        const totalDevengado =
                          (detalle.salarioBasePeriodo || 0) +
                          (detalle.montoHorasExtra || 0) +
                          (detalle.montoIncapacidad || 0);
                        const totalDeducciones =
                          (detalle.deduccionCcssIvm || 0) +
                          (detalle.deduccionCcssSem || 0) +
                          (detalle.impuestoDeRenta || 0) +
                          (detalle.otrasDeducciones || 0);
                        const salarioNeto = totalDevengado - totalDeducciones;
                        const nombreCompleto = [
                          detalle.nombreEmpleado,
                          detalle.primerApellidoEmpleado,
                          detalle.segundoApellidoEmpleado,
                        ]
                          .filter(Boolean)
                          .join(' ')
                          .trim();

                        return (
                          <tr key={detalle.id} className="hover:bg-muted/50 transition-colors">
                            <td className="px-4 py-3 font-medium">
                              {nombreCompleto || 'Empleado'}
                            </td>
                            <td className="px-4 py-3">{formatCurrency(detalle.salarioBasePeriodo)}</td>
                            <td className="px-4 py-3">{formatCurrency(detalle.montoHorasExtra)}</td>
                            <td className="px-4 py-3">{formatCurrency(detalle.montoIncapacidad)}</td>
                            <td className="px-4 py-3 font-semibold">{formatCurrency(totalDevengado)}</td>
                            <td className="px-4 py-3">{formatCurrency(totalDeducciones)}</td>
                            <td className="px-4 py-3 font-semibold text-green-600">{formatCurrency(salarioNeto)}</td>
                          </tr>
                        );
                      })}
                    </tbody>
                  </table>
                </div>
              )}
            </div>
          </CardContent>
        </Card>
      )}

    </div>
  );
}
