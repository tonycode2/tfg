import { useState, useEffect, useCallback, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { DatePicker } from '@/components/ui/date-picker';
import { TimePicker } from '@/components/ui/time-picker';
import { SearchableSelect } from '@/components/ui/searchable-select';
import { SimpleDataTable, type Column } from '@/components/SimpleDataTable';
import { Pagination, PaginationContent, PaginationItem, PaginationLink, PaginationNext, PaginationPrevious } from '@/components/ui/pagination';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import { authService } from '@/services/authService';
import { departamentosService, type Departamento } from '@/services/apiService';
import {
  marcarEntrada,
  marcarSalida,
  obtenerMiEstado,
  obtenerDepartamentosAccesibles,
  obtenerResumenDepartamento,
  obtenerHistorial,
  obtenerPreviewJornadaDiaria,
  combineDateAndTime,
  getCurrentDateString,
  getCurrentTimeString,
  getStartOfMonthString,
  getEndOfMonthString,
  type EstadoAsistencia,
  type ResumenDepartamento,
  type Asistencia,
} from '@/services/asistenciaService';
import { ConfirmClockOutModal } from '@/components/ConfirmClockOutModal';

// ==================== ICONS ====================

const ClockInIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 16l-4-4m0 0l4-4m-4 4h14m-5 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h7a3 3 0 013 3v1" />
  </svg>
);

const ClockOutIcon = () => (
  <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
  </svg>
);

const RefreshIcon = () => (
  <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15" />
  </svg>
);

// ==================== INTERFACES ====================

interface HistorialRow {
  id: string;
  fecha: string;
  fechaOrden: string;
  horaEntrada: string;
  horaSalida: string;
  horasTrabajadas: string;
  observaciones: string;
}

const formatElapsedTime = (totalSeconds: number) => {
  const safeSeconds = Math.max(0, totalSeconds);
  const hours = Math.floor(safeSeconds / 3600);
  const minutes = Math.floor((safeSeconds % 3600) / 60);
  const seconds = safeSeconds % 60;
  return [hours, minutes, seconds].map(unit => String(unit).padStart(2, '0')).join(':');
};

const parseDateTimeToMs = (dateTime: string) => {
  const parsed = new Date(dateTime.replace(' ', 'T'));
  const parsedMs = parsed.getTime();
  return Number.isNaN(parsedMs) ? Date.now() : parsedMs;
};

const formatToAmPm = (timeValue?: string | null) => {
  if (!timeValue || timeValue === '-') return '-';

  const [hoursPart, minutesPart] = String(timeValue).split(':');
  const hours24 = Number(hoursPart);

  if (Number.isNaN(hours24) || !minutesPart) {
    return String(timeValue);
  }

  const hours12 = ((hours24 + 11) % 12) + 1;
  const period = hours24 >= 12 ? 'PM' : 'AM';
  return `${String(hours12).padStart(2, '0')}:${minutesPart} ${period}`;
};

const formatDateToDisplay = (dateValue: string) => {
  const [year, month, day] = dateValue.split('-');
  if (!year || !month || !day) return dateValue;
  return `${day}/${month}/${year}`;
};

export function AsistenciaView() {
  const userInfo = useMemo(() => authService.getUserInfo(), []);
  const canViewDepartments = ['HR', 'JEFE', 'ADMIN'].includes(userInfo.role);
  const isManualDateTimeEnabled = import.meta.env.VITE_ASISTENCIA_MANUAL_DATETIME === 'true';

  // ==================== STATE ====================

  const [isLoading, setIsLoading] = useState(true);
  const [isClocking, setIsClocking] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [miEstado, setMiEstado] = useState<EstadoAsistencia | null>(null);
  const [workStartMs, setWorkStartMs] = useState<number | null>(null);
  const [workElapsedSeconds, setWorkElapsedSeconds] = useState(0);

  const [testDate, setTestDate] = useState<string>(getCurrentDateString());
  const [testTime, setTestTime] = useState<string>(getCurrentTimeString());

  const [departamentos, setDepartamentos] = useState<Departamento[]>([]);
  const [departamentosAccesibles, setDepartamentosAccesibles] = useState<number[]>([]);
  const [selectedDepartamento, setSelectedDepartamento] = useState<number | null>(null);
  const [resumenDepartamento, setResumenDepartamento] = useState<ResumenDepartamento | null>(null);
  const [isLoadingDepartamento, setIsLoadingDepartamento] = useState(false);
  const [searchFilter, setSearchFilter] = useState('');
  const [fechaDepartamento, setFechaDepartamento] = useState<string>(getCurrentDateString());
  const [pageEmpleados, setPageEmpleados] = useState(0);
  const [pageSizeEmpleados, setPageSizeEmpleados] = useState(5);

  const [historial, setHistorial] = useState<Asistencia[]>([]);
  const [historialFechaInicio, setHistorialFechaInicio] = useState<string>('');
  const [historialFechaFin, setHistorialFechaFin] = useState<string>('');
  const [, setIsLoadingHistorial] = useState(false);
  const [pageHistorial, setPageHistorial] = useState(0);
  const pageSizeHistorial = 5;

  const [isConfirmModalOpen, setIsConfirmModalOpen] = useState(false);
  const [jornadaPreview, setJornadaPreview] = useState<any>(null);

  // ==================== DATA LOADING ====================

  const loadMiEstado = useCallback(async () => {
    try {
      const estado = await obtenerMiEstado();
      setMiEstado(estado);

      if (estado.estadoActual === 'LABORANDO' && estado.horaEntradaHoy) {
        const startMs = parseDateTimeToMs(`${getCurrentDateString()} ${estado.horaEntradaHoy}`);
        setWorkStartMs(startMs);
        setWorkElapsedSeconds(Math.max(0, Math.floor((Date.now() - startMs) / 1000)));
      } else {
        setWorkStartMs(null);
        setWorkElapsedSeconds(0);
      }
    } catch (error) {
      console.error('Error loading status:', error);
    }
  }, []);

  const loadDepartamentos = useCallback(async () => {
    if (!canViewDepartments) return;

    try {
      const accesibles = await obtenerDepartamentosAccesibles();
      setDepartamentosAccesibles(accesibles);

      const allDepts = await departamentosService.getAllUnpaginated();
      const deptsArray = (allDepts as { content?: Departamento[] }).content || allDepts;
      setDepartamentos(Array.isArray(deptsArray) ? deptsArray : []);

      if (accesibles.length > 0 && !selectedDepartamento) {
        setSelectedDepartamento(accesibles[0]);
      }
    } catch (error) {
      console.error('Error loading departments:', error);
    }
  }, [canViewDepartments, selectedDepartamento]);

  const loadResumenDepartamento = useCallback(async () => {
    if (!selectedDepartamento) return;

    setIsLoadingDepartamento(true);
    try {
      const resumen = await obtenerResumenDepartamento(selectedDepartamento, fechaDepartamento);
      setResumenDepartamento(resumen);
    } catch (error) {
      console.error('Error loading department summary:', error);
      setResumenDepartamento(null);
    } finally {
      setIsLoadingDepartamento(false);
    }
  }, [selectedDepartamento, fechaDepartamento]);

  const loadHistorial = useCallback(async (fechaInicio?: string, fechaFin?: string) => {
    setIsLoadingHistorial(true);
    try {
      let fechaInicioStr: string;
      let fechaFinStr: string;

      if (fechaInicio && fechaFin) {
        fechaInicioStr = `${fechaInicio} 00:00:00`;
        fechaFinStr = `${fechaFin} 23:59:59`;
      } else if (fechaInicio) {
        // Only start provided -> search that day
        fechaInicioStr = `${fechaInicio} 00:00:00`;
        fechaFinStr = `${fechaInicio} 23:59:59`;
      } else if (fechaFin) {
        // Only end provided -> search that day
        fechaInicioStr = `${fechaFin} 00:00:00`;
        fechaFinStr = `${fechaFin} 23:59:59`;
      } else {
        // No dates provided -> default to current month
        fechaInicioStr = getStartOfMonthString();
        fechaFinStr = getEndOfMonthString();
      }

      // Safety: if computed range is inverted, swap them
      const parsedInicio = new Date(fechaInicioStr.replace(' ', 'T'));
      const parsedFin = new Date(fechaFinStr.replace(' ', 'T'));
      if (parsedInicio > parsedFin) {
        const tmp = fechaInicioStr;
        fechaInicioStr = fechaFinStr;
        fechaFinStr = tmp;
      }

      const data = await obtenerHistorial(undefined, fechaInicioStr, fechaFinStr);
      setHistorial(Array.isArray(data) ? data : []);
      setPageHistorial(0);
    } catch (error) {
      console.error('Error loading history:', error);
      setHistorial([]);
    } finally {
      setIsLoadingHistorial(false);
    }
  }, []);

  useEffect(() => {
    const loadInitialData = async () => {
      setIsLoading(true);
      await Promise.all([loadMiEstado(), loadDepartamentos(), loadHistorial()]);
      setIsLoading(false);
    };

    loadInitialData();
  }, [loadMiEstado, loadDepartamentos, loadHistorial]);

  useEffect(() => {
    if (selectedDepartamento) {
      loadResumenDepartamento();
    }
  }, [selectedDepartamento, loadResumenDepartamento]);

  useEffect(() => {
    if (!workStartMs) return;

    const updateElapsed = () => {
      setWorkElapsedSeconds(Math.max(0, Math.floor((Date.now() - workStartMs) / 1000)));
    };

    updateElapsed();
    const timerId = window.setInterval(updateElapsed, 1000);
    return () => window.clearInterval(timerId);
  }, [workStartMs]);

  // ==================== HANDLERS ====================

  const showMessage = (message: string, isError: boolean = false) => {
    if (isError) {
      setErrorMessage(message);
      setSuccessMessage(null);
    } else {
      setSuccessMessage(message);
      setErrorMessage(null);
    }
    setTimeout(() => {
      setSuccessMessage(null);
      setErrorMessage(null);
    }, 5000);
  };

  const getClockingDateTime = () => {
    const date = isManualDateTimeEnabled ? testDate : getCurrentDateString();
    const time = isManualDateTimeEnabled ? testTime : getCurrentTimeString();

    return {
      time,
      dateTime: combineDateAndTime(date, time),
    };
  };

  const handleMarcarEntrada = async () => {
    setIsClocking(true);
    try {
      const { time, dateTime: fechaHora } = getClockingDateTime();
      await marcarEntrada(fechaHora);

      const entradaMs = parseDateTimeToMs(fechaHora);
      setWorkStartMs(entradaMs);
      setWorkElapsedSeconds(Math.max(0, Math.floor((Date.now() - entradaMs) / 1000)));

      showMessage(`✅ Marcaste entrada a las ${time}`);

      await Promise.all([loadMiEstado(), loadHistorial()]);
      if (selectedDepartamento) {
        await loadResumenDepartamento();
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Error al marcar entrada';
      showMessage(message, true);
    } finally {
      setIsClocking(false);
    }
  };

  const handleMarcarSalida = async () => {
    try {
      // Primero obtener el preview de la jornada con la hora efectiva (manual en dev, actual en prod)
      const { dateTime: fechaHoraSalida } = getClockingDateTime();
      const preview = await obtenerPreviewJornadaDiaria(fechaHoraSalida);
      setJornadaPreview(preview);
      setIsConfirmModalOpen(true);
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Error al obtener información de la jornada';
      showMessage(message, true);
    }
  };

  const handleConfirmSalida = async () => {
    setIsClocking(true);
    try {
      const { time, dateTime: fechaHora } = getClockingDateTime();
      await marcarSalida(fechaHora);

      setWorkStartMs(null);
      setWorkElapsedSeconds(0);

      showMessage(`✅ Marcaste salida a las ${time}`);
      setIsConfirmModalOpen(false);
      setJornadaPreview(null);

      await Promise.all([loadMiEstado(), loadHistorial()]);
      if (selectedDepartamento) {
        await loadResumenDepartamento();
      }
    } catch (error) {
      const message = error instanceof Error ? error.message : 'Error al marcar salida';
      showMessage(message, true);
    } finally {
      setIsClocking(false);
    }
  };

  const handleRefreshDepartamento = () => {
    loadResumenDepartamento();
  };

  // ==================== COMPUTED VALUES ====================

  const accessibleDepartamentos = useMemo(() => {
    return departamentos.filter(d => departamentosAccesibles.includes(d.id));
  }, [departamentos, departamentosAccesibles]);

  const departamentoOptions = useMemo(() => {
    return accessibleDepartamentos.map(d => ({ value: d.id, label: d.nombre }));
  }, [accessibleDepartamentos]);

  const filteredEmpleados = useMemo(() => {
    if (!resumenDepartamento?.empleados) return [];
    if (!searchFilter.trim()) return resumenDepartamento.empleados;

    const searchLower = searchFilter.toLowerCase();
    return resumenDepartamento.empleados.filter(e => e.nombreCompleto.toLowerCase().includes(searchLower));
  }, [resumenDepartamento, searchFilter]);

  const historialRows = useMemo((): HistorialRow[] => {
    const grouped = new Map<string, { entrada?: Asistencia; salida?: Asistencia }>();

    historial.forEach(record => {
      const fecha = record.fechaHora.split(' ')[0];
      const key = fecha;

      if (!grouped.has(key)) grouped.set(key, {});
      const group = grouped.get(key)!;
      if (record.tipoEvento === 'ENTRADA') group.entrada = record;
      else group.salida = record;
    });

    const rows: HistorialRow[] = [];
    grouped.forEach((group, fecha) => {
      const horaEntradaRaw = group.entrada?.fechaHora.split(' ')[1]?.substring(0,5) || '-';
      const horaSalidaRaw = group.salida?.fechaHora.split(' ')[1]?.substring(0,5) || '-';
      const horaEntrada = formatToAmPm(horaEntradaRaw);
      const horaSalida = formatToAmPm(horaSalidaRaw);

      let horasTrabajadas = '-';
      if (group.entrada && group.salida) {
        horasTrabajadas = '8.00h';
      }

      const observaciones = [group.entrada?.observaciones, group.salida?.observaciones].filter(Boolean).join(' | ');

      rows.push({
        id: fecha,
        fecha: formatDateToDisplay(fecha),
        fechaOrden: fecha,
        horaEntrada: horaEntrada,
        horaSalida: horaSalida,
        horasTrabajadas,
        observaciones: observaciones || '-',
      });
    });

    return rows.sort((a,b) => b.fechaOrden.localeCompare(a.fechaOrden));
  }, [historial]);

  // ==================== PAGINATION ====================

  const totalPagesEmpleados = Math.ceil(filteredEmpleados.length / pageSizeEmpleados);
  const paginatedEmpleados = filteredEmpleados.slice(
    pageEmpleados * pageSizeEmpleados,
    pageEmpleados * pageSizeEmpleados + pageSizeEmpleados,
  );

  const totalPagesHistorial = Math.ceil(historialRows.length / pageSizeHistorial);
  const paginatedHistorial = historialRows.slice(
    pageHistorial * pageSizeHistorial,
    pageHistorial * pageSizeHistorial + pageSizeHistorial,
  );

  useEffect(() => {
    setPageEmpleados(0);
  }, [searchFilter, resumenDepartamento]);

  useEffect(() => {
    if (totalPagesEmpleados > 0 && pageEmpleados >= totalPagesEmpleados) {
      setPageEmpleados(Math.max(totalPagesEmpleados - 1, 0));
    }
  }, [pageEmpleados, totalPagesEmpleados]);

  useEffect(() => {
    if (totalPagesHistorial > 0 && pageHistorial >= totalPagesHistorial) {
      setPageHistorial(Math.max(totalPagesHistorial - 1, 0));
    }
  }, [pageHistorial, totalPagesHistorial]);

  useEffect(() => {
    // Auto-filtrar solo cuando el rango está completo (inicio + fin)
    // para evitar recargas innecesarias al seleccionar la primera fecha.
    if (!historialFechaInicio || !historialFechaFin) return;

    const timerId = window.setTimeout(() => {
      loadHistorial(historialFechaInicio, historialFechaFin);
    }, 250);

    return () => window.clearTimeout(timerId);
  }, [historialFechaInicio, historialFechaFin, loadHistorial]);

  const handlePageSizeEmpleadosChange = (newSize: string) => {
    setPageSizeEmpleados(Number(newSize));
    setPageEmpleados(0);
  };

  // ==================== TABLE COLUMNS ====================

  const historialColumns: Column<HistorialRow>[] = [
    { key: 'fecha', label: 'Fecha' },
    { key: 'horaEntrada', label: 'Hora Entrada' },
    { key: 'horaSalida', label: 'Hora Salida' },
    { key: 'horasTrabajadas', label: 'Horas Trabajadas' },
    { key: 'observaciones', label: 'Observaciones' },
  ];

  const empleadosColumns: Column<EstadoAsistencia>[] = [
    { key: 'nombreCompleto', label: 'Empleado' },
    { key: 'puestoNombre', label: 'Puesto' },
    {
      key: 'estadoActual',
      label: 'Estado',
      render: (value) => (
        <span>{value === 'LABORANDO' ? '🟢 LABORANDO' : '⚪ FUERA'}</span>
      ),
    },
    {
      key: 'horaEntradaHoy',
      label: 'Entrada Hoy',
      render: (value) => (value ? formatToAmPm(String(value).substring(0,5)) : '-'),
    },
    {
      key: 'horaSalidaHoy',
      label: 'Salida Hoy',
      render: (value) => (value ? formatToAmPm(String(value).substring(0,5)) : '-'),
    },
    {
      key: 'observaciones',
      label: 'Observaciones',
      render: (value) => value || '-',
    },
  ];

  // ==================== RENDER ====================

  if (isLoading) {
    return (
      <div className="flex items-center justify-center min-h-[400px]">
        Cargando...
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="grid grid-cols-1 gap-6 xl:grid-cols-2">
      <Card className="h-full flex flex-col">
        <CardHeader>
          <CardTitle>Registro de Asistencia</CardTitle>
          <CardDescription>
            {isManualDateTimeEnabled
              ? 'Marca tu entrada y salida, o prueba con una hora manual.'
              : 'Marca tu entrada y salida con la hora actual.'}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex-1 flex flex-col">
          <div className="space-y-4">
            <div className="grid grid-cols-1 gap-3 sm:grid-cols-2">
              {isManualDateTimeEnabled && (
                <>
                  <div className="space-y-2">
                    <Label>Fecha</Label>
                    <DatePicker value={testDate} onChange={(v: string) => setTestDate(v)} />
                  </div>
                  <div className="space-y-2">
                    <Label>Hora</Label>
                    <TimePicker value={testTime} onChange={(v: string) => setTestTime(v)} />
                  </div>
                </>
              )}
            </div>

            <div className="flex flex-wrap items-center justify-center gap-2">
              <Button onClick={handleMarcarEntrada} disabled={isClocking} variant="default">
                <ClockInIcon />
                <span className="ml-2">Marcar Entrada</span>
              </Button>
              <Button onClick={handleMarcarSalida} disabled={isClocking} variant="outline">
                <ClockOutIcon />
                <span className="ml-2">Marcar Salida</span>
              </Button>
            </div>

          </div>

          <div className="flex-1 flex items-center justify-center pt-6 lg:pt-8">
            <div className="w-full max-w-[360px]">
              <div className="rounded-lg border bg-card px-5 py-4">
                <div className="flex items-center justify-between gap-2">
                  <span className="text-xs text-muted-foreground">Tiempo laborado</span>
                  <span className={`text-xs font-medium ${workStartMs ? 'text-primary' : 'text-muted-foreground'}`}>
                    {workStartMs ? 'En curso' : 'Sin iniciar'}
                  </span>
                </div>
                <div className="mt-2 text-4xl font-semibold tabular-nums">{formatElapsedTime(workElapsedSeconds)}</div>
                <div className="mt-1 text-sm text-muted-foreground">
                  {workStartMs && miEstado?.estadoActual === 'LABORANDO'
                    ? 'Contando desde tu entrada'
                    : 'Marca entrada para iniciar'}
                </div>
              </div>
            </div>
          </div>

          {successMessage && <div className="mt-4 text-green-600">{successMessage}</div>}
          {errorMessage && <div className="mt-4 text-red-600">{errorMessage}</div>}
        </CardContent>
      </Card>

      <Card className="h-full">
        <CardHeader>
          <CardTitle>Historial</CardTitle>
          <CardDescription>Consulta tu historial de asistencias</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap items-end gap-2 mb-4">
            <div className="w-full sm:w-[360px]">
              <Label>Rango de fechas</Label>
              <DatePicker
                mode="range"
                startValue={historialFechaInicio}
                endValue={historialFechaFin}
                onRangeChange={(startDate: string, endDate: string) => {
                  setHistorialFechaInicio(startDate);
                  setHistorialFechaFin(endDate);
                }}
                placeholder="Seleccionar rango"
              />
            </div>
          </div>

          <div className="space-y-3">
            <SimpleDataTable data={paginatedHistorial as any} columns={historialColumns as any} hideHorizontalScrollbar />

            {historialRows.length > 0 && (
              <div className="flex flex-col gap-3 md:flex-row md:items-center md:justify-between">
                {totalPagesHistorial > 1 && (
                  <Pagination>
                    <PaginationContent>
                      <PaginationItem>
                        <PaginationPrevious
                          onClick={() => setPageHistorial(Math.max(0, pageHistorial - 1))}
                          className={pageHistorial === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                        />
                      </PaginationItem>

                      {Array.from({ length: totalPagesHistorial }, (_, i) => i).map((pageNum) => {
                        if (
                          pageNum === 0 ||
                          pageNum === totalPagesHistorial - 1 ||
                          (pageNum >= pageHistorial - 1 && pageNum <= pageHistorial + 1)
                        ) {
                          return (
                            <PaginationItem key={pageNum}>
                              <PaginationLink
                                onClick={() => setPageHistorial(pageNum)}
                                isActive={pageNum === pageHistorial}
                                className="cursor-pointer"
                              >
                                {pageNum + 1}
                              </PaginationLink>
                            </PaginationItem>
                          );
                        }
                        if (pageNum === pageHistorial - 2 || pageNum === pageHistorial + 2) {
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
                          onClick={() => setPageHistorial(Math.min(totalPagesHistorial - 1, pageHistorial + 1))}
                          className={pageHistorial === totalPagesHistorial - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                        />
                      </PaginationItem>
                    </PaginationContent>
                  </Pagination>
                )}

                <div className="text-sm text-muted-foreground text-left md:text-right md:whitespace-nowrap">
                  {totalPagesHistorial > 0
                    ? `Página ${pageHistorial + 1} de ${totalPagesHistorial} • ${historialRows.length} registro(s)`
                    : 'Sin historial para paginar'}
                </div>
              </div>
            )}
          </div>
        </CardContent>
      </Card>
      </div>

      {canViewDepartments && (
        <Card>
          <CardHeader>
            <CardTitle>Estado del Departamento</CardTitle>
            <CardDescription>Resumen por departamento</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="flex flex-wrap items-end gap-4 mb-4">
              <div className="w-72">
                <Label>Departamento</Label>
                <SearchableSelect
                  options={departamentoOptions}
                  value={selectedDepartamento ?? undefined}
                  onChange={(val) => setSelectedDepartamento(typeof val === 'number' ? val : Number(val))}
                />
              </div>
              <div>
                <Label>Fecha</Label>
                <DatePicker 
                  value={fechaDepartamento} 
                  onChange={(v: string) => setFechaDepartamento(v)}
                  toYear={new Date().getFullYear()}
                />
              </div>
              <div className="ml-auto flex items-center gap-2">
                <Button size="sm" onClick={handleRefreshDepartamento} disabled={isLoadingDepartamento}>
                  <RefreshIcon />
                  <span className="ml-2">Actualizar</span>
                </Button>
              </div>
            </div>

            {resumenDepartamento ? (
              <div>
                <div className="flex gap-6 mb-4">
                  <div>Total: <strong>{resumenDepartamento.totalEmpleados}</strong></div>
                  <div className="text-green-600">Laborando: <strong>{resumenDepartamento.empleadosLaborando}</strong></div>
                  <div className="text-gray-600">Fuera: <strong>{resumenDepartamento.empleadosFuera}</strong></div>
                </div>

                <div className="mb-4">
                  <Input placeholder="Buscar empleado" value={searchFilter} onChange={(e) => setSearchFilter(e.target.value)} />
                </div>

                <div className="space-y-3">
                  <SimpleDataTable data={paginatedEmpleados as any} columns={empleadosColumns as any} />

                  {filteredEmpleados.length > 0 && (
                    <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:justify-between">
                      <div className="flex items-center gap-2">
                        <span className="text-sm text-muted-foreground">Mostrar:</span>
                        <Select value={String(pageSizeEmpleados)} onValueChange={handlePageSizeEmpleadosChange}>
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

                      {totalPagesEmpleados > 1 && (
                        <Pagination>
                          <PaginationContent>
                            <PaginationItem>
                              <PaginationPrevious
                                onClick={() => setPageEmpleados(Math.max(0, pageEmpleados - 1))}
                                className={pageEmpleados === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                              />
                            </PaginationItem>

                            {Array.from({ length: totalPagesEmpleados }, (_, i) => i).map((pageNum) => {
                              if (
                                pageNum === 0 ||
                                pageNum === totalPagesEmpleados - 1 ||
                                (pageNum >= pageEmpleados - 1 && pageNum <= pageEmpleados + 1)
                              ) {
                                return (
                                  <PaginationItem key={pageNum}>
                                    <PaginationLink
                                      onClick={() => setPageEmpleados(pageNum)}
                                      isActive={pageNum === pageEmpleados}
                                      className="cursor-pointer"
                                    >
                                      {pageNum + 1}
                                    </PaginationLink>
                                  </PaginationItem>
                                );
                              }
                              if (pageNum === pageEmpleados - 2 || pageNum === pageEmpleados + 2) {
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
                                onClick={() => setPageEmpleados(Math.min(totalPagesEmpleados - 1, pageEmpleados + 1))}
                                className={pageEmpleados === totalPagesEmpleados - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                              />
                            </PaginationItem>
                          </PaginationContent>
                        </Pagination>
                      )}

                      <div className="text-sm text-muted-foreground text-center sm:text-right">
                        {totalPagesEmpleados > 0
                          ? `Página ${pageEmpleados + 1} de ${totalPagesEmpleados} • ${filteredEmpleados.length} empleado(s)`
                          : 'Sin empleados para paginar'}
                      </div>
                    </div>
                  )}
                </div>
              </div>
            ) : (
              <div>No hay datos del departamento seleccionado.</div>
            )}
          </CardContent>
        </Card>
      )}

      <ConfirmClockOutModal
        isOpen={isConfirmModalOpen}
        onClose={() => {
          setIsConfirmModalOpen(false);
          setJornadaPreview(null);
        }}
        onConfirm={handleConfirmSalida}
        preview={jornadaPreview}
        isLoading={isClocking}
      />
    </div>
  );
}
