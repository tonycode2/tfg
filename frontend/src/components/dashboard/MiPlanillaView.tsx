import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { Button } from '@/components/ui/button';
import { Dialog, DialogContent, DialogDescription, DialogHeader, DialogTitle } from '@/components/ui/dialog';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { authService } from '@/services/authService';
import {  planillasService, type PlanillaEmpleado } from '@/services/apiService';
import { obtenerAguinaldosPorEmpleado, type AguinaldoCalculado } from '@/services/aguinaldoService';
import { toast } from 'sonner';

const MoneyIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
  </svg>
);

const GiftIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M12 8c-1.657 0-3 .895-3 2s1.343 2 3 2 3 .895 3 2-1.343 2-3 2m0-8c1.11 0 2.08.402 2.599 1M12 8V7m0 1v8m0 0v1m0-1c-1.11 0-2.08-.402-2.599-1M21 12a9 9 0 11-18 0 9 9 0 0118 0z"
    />
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

type ViewType = 'planillas' | 'aguinaldos';

export function MiPlanillaView() {
  const [viewType, setViewType] = useState<ViewType>('planillas');
  const [planillas, setPlanillas] = useState<PlanillaEmpleado[]>([]);
  const [aguinaldos, setAguinaldos] = useState<AguinaldoCalculado[]>([]);
  const [loading, setLoading] = useState(true);
  const [selectedPlanilla, setSelectedPlanilla] = useState<PlanillaEmpleado | null>(null);
  const [selectedAguinaldo, setSelectedAguinaldo] = useState<AguinaldoCalculado | null>(null);
  const [aguinaldoPlanillas, setAguinaldoPlanillas] = useState<PlanillaEmpleado[]>([]);
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [pdfGeneratingId, setPdfGeneratingId] = useState<number | null>(null);

  const userInfo = authService.getUserInfo();

  useEffect(() => {
    cargarDatos();
  }, []);

  const cargarDatos = async () => {
    try {
      setLoading(true);

      if (!userInfo.idEmpleado) {
        toast.error('Error al cargar datos', {
          description: 'No se pudo obtener la información del empleado',
        });
        return;
      }

      // Cargar planillas
      const dataPlanillas = await planillasService.getPlanillasPorEmpleado(userInfo.idEmpleado);
      const planillasArray = Array.isArray(dataPlanillas) ? dataPlanillas : [];
      const planillasConId = planillasArray.map(p => ({ ...p, id: p.idDetalle || 0 }));
      setPlanillas(planillasConId);

      // Cargar aguinaldos
      try {
        const dataAguinaldos = await obtenerAguinaldosPorEmpleado(userInfo.idEmpleado);
        const aguinaldosArray = Array.isArray(dataAguinaldos) ? dataAguinaldos : [];
        setAguinaldos(aguinaldosArray);
      } catch (err) {
        console.warn('Error al cargar aguinaldos:', err);
        setAguinaldos([]);
      }

      setPage(0);
      setSelectedPlanilla(null);
      setSelectedAguinaldo(null);
    } catch (err: any) {
      console.error('Error al cargar datos:', err);
      toast.error('Error al cargar los datos', {
        description: err.message || 'Ocurrió un error inesperado',
      });
    } finally {
      setLoading(false);
    }
  };

  const totalPages = Math.ceil((viewType === 'planillas' ? planillas : aguinaldos).length / pageSize);
  const startIndex = page * pageSize;
  const currentData = viewType === 'planillas' ? planillas : aguinaldos;
  const paginatedData = currentData.slice(startIndex, startIndex + pageSize);

  useEffect(() => {
    if (totalPages > 0 && page >= totalPages) {
      setPage(Math.max(totalPages - 1, 0));
    }
  }, [page, totalPages]);

  useEffect(() => {
    if (selectedAguinaldo && planillas.length > 0) {
      // Filtrar planillas que caigan en el período del aguinaldo
      const planillasDelPeriodo = planillas.filter((p) => {
        const fechaPlanilla = parseLocalDate(p.fechaPago);
        const fechaInicio = parseLocalDate(selectedAguinaldo.fechaInicioPeriodo);
        const fechaFin = parseLocalDate(selectedAguinaldo.fechaFinPeriodo);
        
        if (!fechaPlanilla || !fechaInicio || !fechaFin) return false;
        
        return fechaPlanilla >= fechaInicio && fechaPlanilla <= fechaFin;
      });
      
      // Ordenar por fecha de pago
      planillasDelPeriodo.sort((a, b) => {
        const dateA = parseLocalDate(a.fechaPago) || new Date(0);
        const dateB = parseLocalDate(b.fechaPago) || new Date(0);
        return dateA.getTime() - dateB.getTime();
      });
      
      setAguinaldoPlanillas(planillasDelPeriodo);
    }
  }, [selectedAguinaldo, planillas]);

  // Agrupar planillas por mes y sumar salarios brutos
  const agruparPorMes = (planillas: PlanillaEmpleado[]): Array<{
    mes: string;
    mesNum: number;
    anio: number;
    planillas: PlanillaEmpleado[];
    totalBruto: number;
  }> => {
    const meses: Record<string, {
      mes: string;
      mesNum: number;
      anio: number;
      planillas: PlanillaEmpleado[];
      totalBruto: number;
    }> = {};

    planillas.forEach((planilla) => {
      const date = parseLocalDate(planilla.fechaPago);
      if (!date) return;
      
      const mes = date.toLocaleDateString('es-CR', { month: 'long', year: 'numeric' });
      const key = `${date.getFullYear()}-${date.getMonth()}`;
      
      if (!meses[key]) {
        meses[key] = {
          mes,
          mesNum: date.getMonth(),
          anio: date.getFullYear(),
          planillas: [],
          totalBruto: 0,
        };
      }
      
      meses[key].planillas.push(planilla);
      meses[key].totalBruto += planilla.totalDevengado || 0;
    });

    return Object.values(meses).sort((a, b) => {
      if (a.anio !== b.anio) return a.anio - b.anio;
      return a.mesNum - b.mesNum;
    });
  };

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0);
  };

  const handleViewTypeChange = (newViewType: string) => {
    setViewType(newViewType as ViewType);
    setPage(0);
    setSelectedPlanilla(null);
    setSelectedAguinaldo(null);
  };

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

  const handlePdf = async (planilla: PlanillaEmpleado) => {
    const detalleId = planilla.idDetalle ?? planilla.id;
    const filename = `colilla-planilla-${detalleId}.pdf`;

    setPdfGeneratingId(planilla.id);

    try {
      if (planilla.urlPdf) {
        const pdfBlob = await planillasService.downloadPlanillaPdf(detalleId, planilla.urlPdf);
        openPdfBlob(pdfBlob, filename);
        return;
      }

      // Use backend-generated colilla PDF (secure, server-side rendering)
      await import('@/services/reportesService').then(m => m.reportesService.colilla(detalleId));
    } catch (error: any) {
      console.error('Error al generar PDF:', error);
      toast.error('Error al generar PDF', {
        description: error?.message || 'No se pudo generar la colilla de pago',
      });
    } finally {
      setPdfGeneratingId(null);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent className="p-8">
          <div className="text-center text-muted-foreground">Cargando datos...</div>
        </CardContent>
      </Card>
    );
  }

  const isEmptyState = viewType === 'planillas' ? planillas.length === 0 : aguinaldos.length === 0;

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex items-center justify-between gap-4">
            <div className="flex items-center gap-3">
              <div className="p-2 bg-primary/10 rounded-lg">
                {viewType === 'planillas' ? <MoneyIcon /> : <GiftIcon />}
              </div>
              <div>
                <CardTitle>{viewType === 'planillas' ? 'Mi Planilla' : 'Mis Aguinaldos'}</CardTitle>
                <CardDescription>
                  {viewType === 'planillas' ? 'Consulta el historial de tus planillas' : 'Consulta el historial de tus aguinaldos'}
                </CardDescription>
              </div>
            </div>
            <Select value={viewType} onValueChange={handleViewTypeChange}>
              <SelectTrigger className="w-[150px]">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="planillas">Planillas</SelectItem>
                <SelectItem value="aguinaldos">Aguinaldos</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {isEmptyState && !loading ? (
            <Alert>
              <InfoIcon />
              <AlertDescription>
                {viewType === 'planillas' 
                  ? 'No tienes planillas registradas en el sistema aún.'
                  : 'No tienes aguinaldos registrados en el sistema aún.'}
              </AlertDescription>
            </Alert>
          ) : (
            <div className="space-y-4">
              <div className="overflow-x-auto">
                <table className="w-full">
                  <thead className="bg-muted">
                    <tr>
                      {viewType === 'planillas' ? (
                        <>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Periodo</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Fecha de Pago</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Salario Neto</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Acciones</th>
                        </>
                      ) : (
                        <>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Período</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Fecha de Cálculo</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Monto</th>
                          <th className="px-4 py-3 text-left text-sm font-medium text-muted-foreground">Acciones</th>
                        </>
                      )}
                    </tr>
                  </thead>
                  <tbody className="bg-card divide-y divide-border">
                    {paginatedData.map((item) => {
                      if (viewType === 'planillas') {
                        const planilla = item as PlanillaEmpleado;
                        return (
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
                              <div className="flex flex-col gap-2 sm:flex-row">
                                <Button
                                  size="sm"
                                  variant="outline"
                                  onClick={() =>
                                    setSelectedPlanilla((current) =>
                                      current?.id === planilla.id ? null : planilla,
                                    )
                                  }
                                >
                                  {selectedPlanilla?.id === planilla.id ? 'Cerrar detalle' : 'Ver detalle'}
                                </Button>
                                <Button
                                  size="sm"
                                  variant="secondary"
                                  onClick={() => handlePdf(planilla)}
                                  disabled={pdfGeneratingId === planilla.id}
                                >
                                  {pdfGeneratingId === planilla.id ? 'Generando PDF...' : 'PDF'}
                                </Button>
                              </div>
                            </td>
                          </tr>
                        );
                      } else {
                        const aguinaldo = item as AguinaldoCalculado;
                        return (
                          <tr
                            key={aguinaldo.id}
                            className={`hover:bg-muted/50 transition-colors ${
                              selectedAguinaldo?.id === aguinaldo.id ? 'bg-primary/10' : ''
                            }`}
                          >
                            <td className="px-4 py-3">
                              <div className="font-medium">{formatDate(aguinaldo.fechaInicioPeriodo)}</div>
                              <div className="text-sm text-muted-foreground">al {formatDate(aguinaldo.fechaFinPeriodo)}</div>
                            </td>
                            <td className="px-4 py-3">{formatDate(aguinaldo.fechaCalculo)}</td>
                            <td className="px-4 py-3">
                              <span className="font-semibold text-green-600">
                                {formatCurrency(aguinaldo.montoAguinaldo)}
                              </span>
                            </td>
                            <td className="px-4 py-3">
                              <Button
                                size="sm"
                                variant="outline"
                                onClick={() =>
                                  setSelectedAguinaldo((current) =>
                                    current?.id === aguinaldo.id ? null : aguinaldo,
                                  )
                                }
                              >
                                {selectedAguinaldo?.id === aguinaldo.id ? 'Cerrar detalle' : 'Ver detalle'}
                              </Button>
                            </td>
                          </tr>
                        );
                      }
                    })}
                  </tbody>
                </table>
              </div>

              {currentData.length > 0 && (
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
                      ? `Página ${page + 1} de ${totalPages} • ${currentData.length} ${viewType === 'planillas' ? 'planilla(s)' : 'aguinaldo(s)'}`
                      : `Sin ${viewType === 'planillas' ? 'planillas' : 'aguinaldos'} para paginar`}
                  </div>
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* Modal para Planilla */}
      <Dialog
        open={Boolean(selectedPlanilla)}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedPlanilla(null);
          }
        }}
      >
        {selectedPlanilla && (
          <DialogContent className="max-w-4xl max-h-[85vh] overflow-y-auto">
            <DialogHeader>
              <DialogTitle>Detalle de Planilla</DialogTitle>
              <DialogDescription>
                Periodo: {formatDate(selectedPlanilla.fechaInicioPeriodo)} al {formatDate(selectedPlanilla.fechaFinPeriodo)}
              </DialogDescription>
            </DialogHeader>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
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
                    <span className="text-muted-foreground">Feriados Trabajados:</span>
                    <span className="font-medium">{formatCurrency(selectedPlanilla.montoFeriadosTrabajados)}</span>
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
          </DialogContent>
        )}
      </Dialog>

      {/* Modal para Aguinaldo */}
      <Dialog
        open={Boolean(selectedAguinaldo)}
        onOpenChange={(open) => {
          if (!open) {
            setSelectedAguinaldo(null);
            setAguinaldoPlanillas([]);
          }
        }}
      >
        {selectedAguinaldo && (
          <DialogContent className="max-w-4xl max-h-[90vh] overflow-hidden flex flex-col">
            <DialogHeader>
              <DialogTitle>Detalle de Aguinaldo</DialogTitle>
              <DialogDescription>
                Período: {formatDate(selectedAguinaldo.fechaInicioPeriodo)} al {formatDate(selectedAguinaldo.fechaFinPeriodo)}
              </DialogDescription>
            </DialogHeader>

            <div className="overflow-y-auto modal-scrollbar flex-1 space-y-6 pr-4">
              <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
                <div className="p-4 bg-muted rounded-lg">
                  <p className="text-sm text-muted-foreground mb-1">Período</p>
                  <p className="font-semibold text-lg">{selectedAguinaldo.anio}</p>
                </div>
                <div className="p-4 bg-muted rounded-lg">
                  <p className="text-sm text-muted-foreground mb-1">Fecha de Cálculo</p>
                  <p className="font-semibold text-sm">{formatDate(selectedAguinaldo.fechaCalculo)}</p>
                </div>
                <div className="p-4 bg-gradient-to-br from-green-50 to-emerald-100 border-2 border-green-300 rounded-lg">
                  <p className="text-sm font-semibold text-green-700 mb-1">Monto Aguinaldo</p>
                  <p className="font-bold text-lg text-green-700">{formatCurrency(selectedAguinaldo.montoAguinaldo)}</p>
                </div>
              </div>

              {/* Desglose de Salarios Mensuales */}
              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Salarios del Período (por Mes)</h3>
                {aguinaldoPlanillas.length > 0 ? (
                  <div className="space-y-2">
                    {agruparPorMes(aguinaldoPlanillas).map((mesData, idx) => (
                      <div key={idx} className="space-y-2">
                        <div className="grid grid-cols-12 gap-2 p-3 bg-muted rounded-lg font-semibold">
                          <div className="col-span-8 capitalize">{mesData.mes}</div>
                          <div className="col-span-4 text-right text-primary">{formatCurrency(mesData.totalBruto)}</div>
                        </div>
                      </div>
                    ))}
                    <div className="grid grid-cols-12 gap-2 p-3 bg-primary/10 rounded-lg font-semibold border-t-2 mt-4">
                      <div className="col-span-8">Total de Salarios Brutos</div>
                      <div className="col-span-4 text-right text-primary">{formatCurrency(selectedAguinaldo.totalSalariosDevengados)}</div>
                    </div>
                  </div>
                ) : (
                  <div className="p-4 bg-muted rounded-lg text-center text-muted-foreground text-sm">
                    No hay planillas disponibles para mostrar en este período
                  </div>
                )}
              </div>

              <div className="space-y-4">
                <h3 className="font-semibold text-lg border-b pb-2">Cálculo</h3>
                <div className="space-y-3">
                  <div className="flex justify-between items-center p-3 bg-muted rounded-lg">
                    <span className="text-muted-foreground">Total de Salarios Brutos:</span>
                    <span className="font-semibold">{formatCurrency(selectedAguinaldo.totalSalariosDevengados)}</span>
                  </div>
                  <div className="flex justify-between items-center text-sm text-muted-foreground">
                    <span>Fórmula: Total Salarios ÷ 12 meses</span>
                  </div>
                  <div className="pt-3 border-t-2">
                    <div className="flex justify-between items-center">
                      <span className="text-lg font-semibold">Aguinaldo Calculado:</span>
                      <span className="text-2xl font-bold text-green-700">{formatCurrency(selectedAguinaldo.montoAguinaldo)}</span>
                    </div>
                  </div>
                </div>
              </div>

              {selectedAguinaldo.fechaPago && (
                <Alert>
                  <InfoIcon />
                  <AlertDescription>
                    Fecha de pago: {formatDate(selectedAguinaldo.fechaPago)}
                  </AlertDescription>
                </Alert>
              )}
            </div>
          </DialogContent>
        )}
      </Dialog>
    </div>
  );
}
