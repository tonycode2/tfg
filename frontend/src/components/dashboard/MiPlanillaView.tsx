import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { authService } from '@/services/authService';
import { planillasService, type PlanillaEmpleado } from '@/services/apiService';
import { toast } from 'sonner';

const MoneyIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const InfoIcon = () => (
  <svg className="w-5 h-5 text-blue-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M13 16h-1v-4h-1m1-4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const formatCurrency = (value: number | undefined): string => {
  if (value === undefined || value === null) return '₡0.00';
  return new Intl.NumberFormat('es-CR', {
    style: 'currency',
    currency: 'CRC',
    minimumFractionDigits: 2,
  }).format(value);
};

const formatDate = (dateString: string | undefined): string => {
  if (!dateString) return 'N/A';
  const date = new Date(dateString);
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

export function MiPlanillaView() {
  const [planillas, setPlanillas] = useState<PlanillaEmpleado[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPlanilla, setSelectedPlanilla] = useState<PlanillaEmpleado | null>(null);

  const userInfo = authService.getUserInfo();

  useEffect(() => {
    cargarPlanillas();
  }, []);

  const cargarPlanillas = async () => {
    try {
      setLoading(true);

      if (!userInfo.idEmpleado) {
        toast.error('Error al cargar planillas', {
          description: 'No se pudo obtener la información del empleado',
        });
        return;
      }

      const data = await planillasService.getPlanillasPorEmpleado(userInfo.idEmpleado);
      const planillasArray = Array.isArray(data) ? data : [];
      // Agregar id para compatibilidad con DataTable (usar idDetalle como id único, garantizando que nunca sea undefined)
      const planillasConId = planillasArray.map(p => ({ ...p, id: p.idDetalle || 0 }));
      setPlanillas(planillasConId);

      // Si hay planillas, seleccionar la más reciente por defecto
      if (planillasConId.length > 0) {
        setSelectedPlanilla(planillasConId[0]);
      }
    } catch (err: any) {
      console.error('Error al cargar planillas:', err);
      toast.error('Error al cargar las planillas', {
        description: err.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="p-8">
          <div className="text-center text-muted-foreground">Cargando planillas...</div>
        </CardContent>
      </Card>
    );
  }

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center gap-3">
            <div className="p-2 bg-primary/10 rounded-lg">
              <MoneyIcon />
            </div>
            <div>
              <CardTitle>Mi Planilla</CardTitle>
              <CardDescription>Consulta el historial de tus planillas</CardDescription>
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {planillas.length === 0 && !loading ? (
            <Alert>
              <InfoIcon />
              <AlertDescription>
                No tienes planillas registradas en el sistema aún.
              </AlertDescription>
            </Alert>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full">
                <thead className="bg-muted">
                  <tr>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Periodo</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Fecha de Pago</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Salario Neto</th>
                    <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Estado</th>
                  </tr>
                </thead>
                <tbody className="bg-card divide-y divide-border">
                  {planillas.map((planilla) => (
                    <tr
                      key={planilla.id}
                      onClick={() => setSelectedPlanilla(planilla)}
                      className={`cursor-pointer hover:bg-muted/50 transition-colors ${
                        selectedPlanilla?.id === planilla.id ? 'bg-primary/10' : ''
                      }`}
                    >
                      <td className="px-4 py-3">
                        <div className="font-medium">{formatDate(planilla.fechaInicioPeriodo)}</div>
                        <div className="text-sm text-muted-foreground">al {formatDate(planilla.fechaFinPeriodo)}</div>
                      </td>
                      <td className="px-4 py-3">{formatDate(planilla.fechaPago)}</td>
                      <td className="px-4 py-3">
                        <span className="font-semibold text-green-600">
                          {formatCurrency(planilla.salarioNeto)}
                        </span>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`px-2 py-1 rounded-full text-xs font-medium ${getEstadoBadge(planilla.estadoPlanilla)}`}>
                          {getEstadoLabel(planilla.estadoPlanilla)}
                        </span>
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
            <CardTitle>Detalle de Planilla</CardTitle>
            <CardDescription>
              Periodo: {formatDate(selectedPlanilla.fechaInicioPeriodo)} al {formatDate(selectedPlanilla.fechaFinPeriodo)}
            </CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {/* Devengado */}
              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Devengado</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Salario Base:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.salarioBasePeriodo)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Horas Extra:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.montoHorasExtra)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Incapacidad:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.montoIncapacidad)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t font-semibold">
                    <span>Total Devengado:</span>
                    <span className="text-green-600">{formatCurrency(selectedPlanilla.totalDevengado)}</span>
                  </div>
                </div>
              </div>

              {/* Deducciones */}
              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Deducciones</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">CCSS IVM:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.deduccionCcssIvm)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">CCSS SEM:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.deduccionCcssSem)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Impuesto de Renta:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.impuestoDeRenta)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Otras Deducciones:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.otrasDeducciones)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t font-semibold">
                    <span>Total Deducciones:</span>
                    <span className="text-red-600">{formatCurrency(selectedPlanilla.totalDeducciones)}</span>
                  </div>
                </div>
              </div>
            </div>

            {/* Salario Neto */}
            <div className="mt-6 p-4 bg-primary/5 rounded-lg">
              <div className="flex justify-between items-center">
                <span className="text-lg font-semibold">Salario Neto a Recibir:</span>
                <span className="text-2xl font-bold text-primary">
                  {formatCurrency(selectedPlanilla.salarioNeto)}
                </span>
              </div>
              <div className="flex justify-between items-center mt-2 text-sm">
                <span className="text-muted-foreground">Fecha de Pago:</span>
                <span className="font-medium">{formatDate(selectedPlanilla.fechaPago)}</span>
              </div>
            </div>

            {selectedPlanilla.cantidadDiasFeriados && selectedPlanilla.cantidadDiasFeriados > 0 && (
              <Alert className="mt-4">
                <InfoIcon />
                <AlertDescription>
                  Esta planilla incluye {selectedPlanilla.cantidadDiasFeriados} día(s) feriado(s).
                </AlertDescription>
              </Alert>
            )}
          </CardContent>
        </Card>
      )}
    </div>
  );
}
