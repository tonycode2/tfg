import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
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

export function MiPlanillaView() {
  const [planillas, setPlanillas] = useState<PlanillaEmpleado[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPlanilla, setSelectedPlanilla] = useState<PlanillaEmpleado | null>(null);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);

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
      setPage(0);

      // Mantener selección explícita del usuario
      setSelectedPlanilla(null);
    } catch (err: any) {
      console.error('Error al cargar planillas:', err);
      toast.error('Error al cargar las planillas', {
        description: err.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoading(false);
    }
  };

  const totalPages = Math.ceil(planillas.length / pageSize);
  const startIndex = page * pageSize;
  const paginatedPlanillas = planillas.slice(startIndex, startIndex + pageSize);

  useEffect(() => {
    if (totalPages > 0 && page >= totalPages) {
      setPage(Math.max(totalPages - 1, 0));
    }
  }, [page, totalPages]);

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0);
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
            <div className="space-y-4">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-muted">
                    <tr>
                      <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Periodo</th>
                      <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Fecha de Pago</th>
                      <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Salario Neto</th>
                      <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Acciones</th>
                    </tr>
                  </thead>
                  <tbody className="bg-card divide-y divide-border">
                    {paginatedPlanillas.map((planilla) => (
                      <tr
                        key={planilla.id}
                        className={`hover:bg-muted/50 transition-colors ${
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
                          <Button
                            size="sm"
                            variant="outline"
                            onClick={() =>
                              setSelectedPlanilla((current) =>
                                current?.id === planilla.id ? null : planilla,
                              )
                            }
                          >
                            {selectedPlanilla?.id === planilla.id ? 'Ocultar detalle' : 'Ver detalle'}
                          </Button>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>

              {planillas.length > 0 && (
                <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                  <div className="flex items-center gap-2">
                    <span className="text-sm text-muted-foreground">Mostrar:</span>
                    <Select value={String(pageSize)} onValueChange={handlePageSizeChange}>
                      <SelectTrigger className="w-[110px]">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="5">5</SelectItem>
                        <SelectItem value="10">10</SelectItem>
                        <SelectItem value="20">20</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>

                  {totalPages > 1 && (
                    <Pagination>
                      <PaginationContent>
                        <PaginationItem>
                          <PaginationPrevious
                            onClick={() => setPage(Math.max(0, page - 1))}
                            className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>

                        {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => {
                          if (
                            pageNum === 0 ||
                            pageNum === totalPages - 1 ||
                            (pageNum >= page - 1 && pageNum <= page + 1)
                          ) {
                            return (
                              <PaginationItem key={pageNum}>
                                <PaginationLink
                                  onClick={() => setPage(pageNum)}
                                  isActive={pageNum === page}
                                  className="cursor-pointer"
                                >
                                  {pageNum + 1}
                                </PaginationLink>
                              </PaginationItem>
                            );
                          }
                          if (pageNum === page - 2 || pageNum === page + 2) {
                            return (
                              <PaginationItem key={pageNum}>
                                <span className="flex h-9 w-9 items-center justify-center">...</span>
                              </PaginationItem>
                            );
                          }
                          return null;
                        })}

                        <PaginationItem>
                          <PaginationNext
                            onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                            className={page === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                          />
                        </PaginationItem>
                      </PaginationContent>
                    </Pagination>
                  )}

                  <div className="text-sm text-muted-foreground text-center sm:text-right">
                    {totalPages > 0
                      ? `Página ${page + 1} de ${totalPages} • ${planillas.length} planilla(s)`
                      : 'Sin planillas para paginar'}
                  </div>
                </div>
              )}
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

            {(selectedPlanilla.cantidadDiasFeriados ?? 0) > 0 && (
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
