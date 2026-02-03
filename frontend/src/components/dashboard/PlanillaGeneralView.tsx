import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { DatePicker } from '@/components/ui/date-picker';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { planillasService, type PlanillaEncabezado } from '@/services/apiService';
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

const addDays = (dateString: string, days: number): string => {
  const date = new Date(dateString);
  date.setDate(date.getDate() + days);
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
};

const getNextMonday = (dateString: string): string => {
  const date = new Date(dateString);
  const dayOfWeek = date.getDay(); // 0 = Domingo, 1 = Lunes, ..., 6 = Sábado
  
  // Si es lunes (1), agregar 7 días para el siguiente lunes
  // Si no es lunes, calcular días hasta el siguiente lunes
  let daysToAdd;
  if (dayOfWeek === 0) { // Domingo
    daysToAdd = 1;
  } else if (dayOfWeek === 1) { // Lunes
    daysToAdd = 7;
  } else { // Martes a Sábado
    daysToAdd = 8 - dayOfWeek;
  }
  
  return addDays(dateString, daysToAdd);
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
  const [fechaInicio, setFechaInicio] = useState<string>('');
  const [fechaFin, setFechaFin] = useState<string>('');
  const [fechaPago, setFechaPago] = useState<string>('');
  const [loading, setLoading] = useState(false);
  const [planillas, setPlanillas] = useState<PlanillaEncabezado[]>([]);
  const [loadingPlanillas, setLoadingPlanillas] = useState(true);
  const [selectedPlanilla, setSelectedPlanilla] = useState<PlanillaEncabezado | null>(null);

  useEffect(() => {
    cargarPlanillas();
  }, []);

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

  const handleFechaInicioChange = (date: string) => {
    setFechaInicio(date);
    if (date) {
      // Calcular fecha fin automáticamente (15 días después)
      const calculatedFechaFin = addDays(date, 15);
      setFechaFin(calculatedFechaFin);
      
      // Sugerir fecha de pago (siguiente lunes después de la fecha fin)
      const suggestedFechaPago = getNextMonday(calculatedFechaFin);
      setFechaPago(suggestedFechaPago);
    } else {
      setFechaFin('');
      setFechaPago('');
    }
  };

  const handleCrearPlanilla = async () => {
    if (!fechaInicio || !fechaFin || !fechaPago) {
      toast.error('Por favor selecciona todas las fechas');
      return;
    }

    try {
      setLoading(true);

      const planillaData = {
        fechaInicioPeriodo: fechaInicio,
        fechaFinPeriodo: fechaFin,
        fechaPago: fechaPago,
        totalPlanillaBruto: 0,
        totalPlanillaNeto: 0,
        estadoPlanilla: 'BORRADOR',
      };

      await planillasService.create(planillaData);
      
      toast.success('Planilla creada exitosamente', {
        description: `Periodo: ${formatDate(fechaInicio)} al ${formatDate(fechaFin)}`,
      });

      // Limpiar formulario
      setFechaInicio('');
      setFechaFin('');
      setFechaPago('');
    } catch (error: any) {
      console.error('Error al crear planilla:', error);
      toast.error('Error al crear la planilla', {
        description: error.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoading(false);
    }
  };

  const canCreatePlanilla = fechaInicio && fechaFin && fechaPago && !loading;

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
            Selecciona la fecha de inicio del periodo de planilla
          </CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
            {/* Fecha de Inicio */}
            <div className="space-y-2">
              <Label htmlFor="fechaInicio" className="flex items-center gap-2">
                <CalendarIcon />
                Fecha de Inicio del Periodo
              </Label>
              <DatePicker
                value={fechaInicio}
                onChange={handleFechaInicioChange}
                placeholder="Seleccionar fecha"
              />
              {fechaInicio && (
                <p className="text-sm text-muted-foreground">
                  {formatDate(fechaInicio)}
                </p>
              )}
            </div>

            {/* Fecha de Fin (calculada automáticamente) */}
            <div className="space-y-2">
              <Label className="flex items-center gap-2">
                <CalendarIcon />
                Fecha de Fin del Periodo
              </Label>
              <DatePicker
                value={fechaFin}
                onChange={setFechaFin}
                placeholder="Calculada automáticamente"
                disabled={true}
              />
              {fechaFin && (
                <p className="text-sm text-muted-foreground">
                  {formatDate(fechaFin)}
                </p>
              )}
            </div>

            {/* Fecha de Pago */}
            <div className="space-y-2">
              <Label htmlFor="fechaPago" className="flex items-center gap-2">
                <CalendarIcon />
                Fecha de Pago
              </Label>
              <DatePicker
                value={fechaPago}
                onChange={setFechaPago}
                placeholder="Fecha de pago"
                disabled={!fechaInicio}
              />
              {fechaPago && (
                <p className="text-sm text-muted-foreground">
                  {formatDate(fechaPago)}
                </p>
              )}
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
                  <p className="font-medium">15 días</p>
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
                setFechaInicio('');
                setFechaFin('');
                setFechaPago('');
              }}
              disabled={!fechaInicio && !fechaFin}
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
                      </td>
                      <td className="px-4 py-3">{formatDate(planilla.fechaPago)}</td>
                      <td className="px-4 py-3 font-semibold">₡{planilla.totalPlanillaBruto?.toLocaleString('es-CR') || '0.00'}</td>
                      <td className="px-4 py-3 font-semibold text-green-600">₡{planilla.totalPlanillaNeto?.toLocaleString('es-CR') || '0.00'}</td>
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
                    <span className="font-medium">₡{selectedPlanilla.totalPlanillaBruto?.toLocaleString('es-CR') || '0.00'}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t">
                    <span className="font-semibold">Total Neto:</span>
                    <span className="font-semibold text-green-600">₡{selectedPlanilla.totalPlanillaNeto?.toLocaleString('es-CR') || '0.00'}</span>
                  </div>
                </div>
              </div>
            </div>
        
          </CardContent>
        </Card>
      )}

    </div>
  );
}
