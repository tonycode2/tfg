import { memo, useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '../ui/card';
import { Button } from '../ui/button';
import { Alert, AlertDescription } from '../ui/alert';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '../ui/dialog';
import { empleadosService, planillasService, type Empleado, type PlanillaEmpleado } from '../../services/apiService';
import { toast } from 'sonner';

interface InicioViewProps {
  username: string;
  employeeId?: number;
}

interface UpcomingEvent {
  id: number;
  name: string;
  date: Date;
  label: string;
}

const formatCurrency = (value: number | undefined): string => {
  if (value === undefined || value === null) return '₡0.00';
  return new Intl.NumberFormat('es-CR', {
    style: 'currency',
    currency: 'CRC',
    minimumFractionDigits: 2,
  }).format(value);
};

const parseLocalDate = (dateString: string | undefined): Date | null => {
  if (!dateString) return null;
  if (dateString.includes('T')) {
    const date = new Date(dateString);
    return Number.isNaN(date.getTime()) ? null : date;
  }

  const [year, month, day] = dateString.split('-').map(Number);
  if (!year || !month || !day) {
    const fallback = new Date(dateString);
    return Number.isNaN(fallback.getTime()) ? null : fallback;
  }

  return new Date(year, month - 1, day);
};

const formatDate = (dateString: string | undefined): string => {
  const date = parseLocalDate(dateString);
  if (!date) return 'N/A';
  return date.toLocaleDateString('es-CR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const formatShortDate = (date: Date): string =>
  date.toLocaleDateString('es-CR', { day: '2-digit', month: 'short' });

const resolveUpcomingEvents = (empleados: Empleado[], type: 'birthday' | 'anniversary'): UpcomingEvent[] => {
  const today = new Date();
  const startOfToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());

  const events = empleados
    .map((empleado) => {
      const sourceDate = type === 'birthday' ? empleado.fechaNacimiento : empleado.fechaIngreso;
      const parsedDate = parseLocalDate(sourceDate);
      if (!parsedDate) return null;

      const nextDate = new Date(today.getFullYear(), parsedDate.getMonth(), parsedDate.getDate());
      if (nextDate < startOfToday) {
        nextDate.setFullYear(nextDate.getFullYear() + 1);
      }

      const displayName = [empleado.nombre, empleado.primerApellido].filter(Boolean).join(' ');
      return {
        id: empleado.id,
        name: displayName || 'Empleado',
        date: nextDate,
        label: type === 'birthday' ? 'Cumpleaños' : 'Aniversario',
      };
    })
    .filter((event): event is UpcomingEvent => Boolean(event));

  return events.sort((a, b) => a.date.getTime() - b.date.getTime()).slice(0, 5);
};

export const InicioView = memo(function InicioView({ username, employeeId }: InicioViewProps) {
  const [birthdays, setBirthdays] = useState<UpcomingEvent[]>([]);
  const [anniversaries, setAnniversaries] = useState<UpcomingEvent[]>([]);
  const [planilla, setPlanilla] = useState<PlanillaEmpleado | null>(null);
  const [loadingEmployees, setLoadingEmployees] = useState(true);
  const [loadingPlanilla, setLoadingPlanilla] = useState(true);
  const [employeeError, setEmployeeError] = useState<string | null>(null);
  const [planillaError, setPlanillaError] = useState<string | null>(null);
  const [pdfLoading, setPdfLoading] = useState(false);
  const [detailOpen, setDetailOpen] = useState(false);

  useEffect(() => {
    const controller = new AbortController();
    const loadEmployees = async () => {
      try {
        setLoadingEmployees(true);
        setEmployeeError(null);
        const data = await empleadosService.getAllUnpaginated(controller.signal);
        const empleadosArray = Array.isArray((data as any)?.content)
          ? (data as any).content
          : Array.isArray(data)
            ? data
            : [];
        setBirthdays(resolveUpcomingEvents(empleadosArray, 'birthday'));
        setAnniversaries(resolveUpcomingEvents(empleadosArray, 'anniversary'));
      } catch (error: any) {
        if (error?.name !== 'AbortError') {
          setEmployeeError('No se pudieron cargar los eventos.');
        }
      } finally {
        setLoadingEmployees(false);
      }
    };

    void loadEmployees();

    return () => controller.abort();
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    const loadPlanilla = async () => {
      if (!employeeId) {
        setPlanillaError('No hay planillas asociadas al usuario.');
        setLoadingPlanilla(false);
        return;
      }

      try {
        setLoadingPlanilla(true);
        setPlanillaError(null);
        const data = await planillasService.getPlanillasPorEmpleado(employeeId, controller.signal);
        const planillasArray = Array.isArray(data) ? data : [];
        const sorted = planillasArray
          .slice()
          .sort((a, b) => {
            const dateA = parseLocalDate(a.fechaPago) || parseLocalDate(a.fechaFinPeriodo) || new Date(0);
            const dateB = parseLocalDate(b.fechaPago) || parseLocalDate(b.fechaFinPeriodo) || new Date(0);
            return dateB.getTime() - dateA.getTime();
          });
        setPlanilla(sorted[0] || null);
      } catch (error: any) {
        if (error?.name !== 'AbortError') {
          setPlanillaError('No se pudo cargar la última planilla.');
        }
      } finally {
        setLoadingPlanilla(false);
      }
    };

    void loadPlanilla();

    return () => controller.abort();
  }, [employeeId]);

  const openPdfBlob = (blob: Blob, filename: string) => {
    const objectUrl = URL.createObjectURL(blob);
    const newWindow = window.open(objectUrl, '_blank', 'noopener,noreferrer');
    if (!newWindow) {
      const link = document.createElement('a');
      link.href = objectUrl;
      link.download = filename;
      link.click();
    }
    window.setTimeout(() => URL.revokeObjectURL(objectUrl), 60000);
  };

  const handlePdf = async () => {
    if (!planilla) return;
    const detalleId = planilla.idDetalle ?? planilla.id;
    const filename = `colilla-planilla-${detalleId}.pdf`;

    try {
      setPdfLoading(true);
      if (planilla.urlPdf) {
        const pdfBlob = await planillasService.downloadPlanillaPdf(detalleId, planilla.urlPdf);
        openPdfBlob(pdfBlob, filename);
        return;
      }

      await import('@/services/reportesService').then((m) => m.reportesService.colilla(detalleId));
    } catch (error: any) {
      toast.error('Error al generar PDF', {
        description: error?.message || 'No se pudo descargar la colilla de pago',
      });
    } finally {
      setPdfLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex justify-start">
        <div className="text-2xl font-semibold text-foreground">
          Bienvenido, {username}
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
        <Card>
          <CardHeader>
            <CardTitle>Próximos cumpleaños</CardTitle>
            <CardDescription>Fechas cercanas en la empresa</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingEmployees ? (
              <p className="text-sm text-muted-foreground">Cargando...</p>
            ) : employeeError ? (
              <p className="text-sm text-muted-foreground">{employeeError}</p>
            ) : birthdays.length === 0 ? (
              <p className="text-sm text-muted-foreground">Sin cumpleaños próximos.</p>
            ) : (
              <div className="space-y-3">
                {birthdays.map((event) => (
                  <div key={event.id} className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium">{event.name}</p>
                      <p className="text-xs text-muted-foreground">{event.label}</p>
                    </div>
                    <span className="text-sm font-semibold text-foreground">{formatShortDate(event.date)}</span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Próximos aniversarios</CardTitle>
            <CardDescription>Antigüedad del equipo</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingEmployees ? (
              <p className="text-sm text-muted-foreground">Cargando...</p>
            ) : employeeError ? (
              <p className="text-sm text-muted-foreground">{employeeError}</p>
            ) : anniversaries.length === 0 ? (
              <p className="text-sm text-muted-foreground">Sin aniversarios próximos.</p>
            ) : (
              <div className="space-y-3">
                {anniversaries.map((event) => (
                  <div key={event.id} className="flex items-center justify-between">
                    <div>
                      <p className="text-sm font-medium">{event.name}</p>
                      <p className="text-xs text-muted-foreground">{event.label}</p>
                    </div>
                    <span className="text-sm font-semibold text-foreground">{formatShortDate(event.date)}</span>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        <Card>
          <CardHeader>
            <CardTitle>Última planilla</CardTitle>
            <CardDescription>Resumen del último pago</CardDescription>
          </CardHeader>
          <CardContent>
            {loadingPlanilla ? (
              <p className="text-sm text-muted-foreground">Cargando...</p>
            ) : planillaError ? (
              <p className="text-sm text-muted-foreground">{planillaError}</p>
            ) : !planilla ? (
              <p className="text-sm text-muted-foreground">No hay planillas disponibles.</p>
            ) : (
              <div className="space-y-4">
                <div>
                  <p className="text-sm text-muted-foreground">Total pagado</p>
                  <p className="text-lg font-semibold text-foreground">{formatCurrency(planilla.salarioNeto)}</p>
                </div>
                <div className="text-sm text-muted-foreground">
                  Fecha de pago: <span className="text-foreground">{formatDate(planilla.fechaPago)}</span>
                </div>
                <div className="text-sm text-muted-foreground">
                  Periodo: <span className="text-foreground">{formatDate(planilla.fechaInicioPeriodo)} al {formatDate(planilla.fechaFinPeriodo)}</span>
                </div>
                <div className="flex flex-col gap-2 sm:flex-row">
                  <Button variant="outline" onClick={() => setDetailOpen(true)} className="w-full">
                    Ver detalle
                  </Button>
                  <Button onClick={handlePdf} disabled={pdfLoading} className="w-full">
                    {pdfLoading ? 'Generando PDF...' : 'Descargar PDF'}
                  </Button>
                </div>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      <Dialog open={detailOpen} onOpenChange={setDetailOpen}>
        {planilla && (
          <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Detalle de Planilla</DialogTitle>
              <DialogDescription>
                Periodo: {formatDate(planilla.fechaInicioPeriodo)} al {formatDate(planilla.fechaFinPeriodo)}
              </DialogDescription>
            </DialogHeader>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Devengado</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Salario Base:</span>
                    <span className="font-medium">{formatCurrency(planilla.salarioBasePeriodo)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Horas Extra:</span>
                    <span className="font-medium">{formatCurrency(planilla.montoHorasExtra)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Feriados Trabajados:</span>
                    <span className="font-medium">{formatCurrency(planilla.montoFeriadosTrabajados)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Incapacidad:</span>
                    <span className="font-medium">{formatCurrency(planilla.montoIncapacidad)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t font-semibold">
                    <span>Total Devengado:</span>
                    <span className="text-green-600">{formatCurrency(planilla.totalDevengado)}</span>
                  </div>
                </div>
              </div>

              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Deducciones</h3>
                <div className="space-y-2">
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">CCSS IVM:</span>
                    <span className="font-medium">{formatCurrency(planilla.deduccionCcssIvm)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">CCSS SEM:</span>
                    <span className="font-medium">{formatCurrency(planilla.deduccionCcssSem)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Impuesto de Renta:</span>
                    <span className="font-medium">{formatCurrency(planilla.impuestoDeRenta)}</span>
                  </div>
                  <div className="flex justify-between">
                    <span className="text-muted-foreground">Otras Deducciones:</span>
                    <span className="font-medium">{formatCurrency(planilla.otrasDeducciones)}</span>
                  </div>
                  <div className="flex justify-between pt-2 border-t font-semibold">
                    <span>Total Deducciones:</span>
                    <span className="text-red-600">{formatCurrency(planilla.totalDeducciones)}</span>
                  </div>
                </div>
              </div>
            </div>

            <div className="mt-6 p-4 bg-primary/5 rounded-lg">
              <div className="flex justify-between items-center">
                <span className="text-lg font-semibold">Salario Neto a Recibir:</span>
                <span className="text-2xl font-bold text-primary">{formatCurrency(planilla.salarioNeto)}</span>
              </div>
              <div className="flex justify-between items-center mt-2 text-sm">
                <span className="text-muted-foreground">Fecha de Pago:</span>
                <span className="font-medium">{formatDate(planilla.fechaPago)}</span>
              </div>
            </div>

            {(planilla.cantidadDiasFeriados ?? 0) > 0 && (
              <Alert className="mt-4">
                <AlertDescription>
                  Esta planilla incluye {planilla.cantidadDiasFeriados} día(s) feriado(s).
                </AlertDescription>
              </Alert>
            )}
          </DialogContent>
        )}
      </Dialog>
    </div>
  );
});
