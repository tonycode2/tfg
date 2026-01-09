import { useState, useEffect, useCallback, useMemo } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { DatePicker } from '@/components/ui/date-picker';
import { TimePicker } from '@/components/ui/time-picker';
import { SearchableSelect } from '@/components/ui/searchable-select';
import { SimpleDataTable, type Column } from '@/components/SimpleDataTable';
import { authService } from '@/services/authService';
import { departamentosService, type Departamento } from '@/services/apiService';
import {
  marcarEntrada,
  marcarSalida,
  obtenerMiEstado,
  obtenerDepartamentosAccesibles,
  obtenerResumenDepartamento,
  obtenerHistorial,
  combineDateAndTime,
  getCurrentDateString,
  getCurrentTimeString,
  getStartOfMonthString,
  getEndOfMonthString,
  type EstadoAsistencia,
  type ResumenDepartamento,
  type Asistencia,
} from '@/services/asistenciaService';

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
  horaEntrada: string;
  horaSalida: string;
  horasTrabajadas: string;
  observaciones: string;
}

export function AsistenciaView() {
  const userInfo = useMemo(() => authService.getUserInfo(), []);
  const canViewDepartments = ['HR', 'JEFE', 'ADMIN'].includes(userInfo.role);

  // ==================== STATE ====================

  const [isLoading, setIsLoading] = useState(true);
  const [isClocking, setIsClocking] = useState(false);
  const [successMessage, setSuccessMessage] = useState<string | null>(null);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const [, setMiEstado] = useState<EstadoAsistencia | null>(null);

  const [testDate, setTestDate] = useState<string>(getCurrentDateString());
  const [testTime, setTestTime] = useState<string>(getCurrentTimeString());

  const [departamentos, setDepartamentos] = useState<Departamento[]>([]);
  const [departamentosAccesibles, setDepartamentosAccesibles] = useState<number[]>([]);
  const [selectedDepartamento, setSelectedDepartamento] = useState<number | null>(null);
  const [resumenDepartamento, setResumenDepartamento] = useState<ResumenDepartamento | null>(null);
  const [isLoadingDepartamento, setIsLoadingDepartamento] = useState(false);
  const [searchFilter, setSearchFilter] = useState('');
  const [fechaDepartamento, setFechaDepartamento] = useState<string>(getCurrentDateString());

  const [historial, setHistorial] = useState<Asistencia[]>([]);
  const [historialFechaInicio, setHistorialFechaInicio] = useState<string>('');
  const [historialFechaFin, setHistorialFechaFin] = useState<string>('');
  const [isLoadingHistorial, setIsLoadingHistorial] = useState(false);

  // ==================== DATA LOADING ====================

  const loadMiEstado = useCallback(async () => {
    try {
      const estado = await obtenerMiEstado();
      setMiEstado(estado);
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

  const loadHistorial = useCallback(async () => {
    setIsLoadingHistorial(true);
    try {
      const fechaInicio = historialFechaInicio ? `${historialFechaInicio} 00:00:00` : getStartOfMonthString();
      const fechaFin = historialFechaFin ? `${historialFechaFin} 23:59:59` : getEndOfMonthString();

      const data = await obtenerHistorial(undefined, fechaInicio, fechaFin);
      setHistorial(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error loading history:', error);
      setHistorial([]);
    } finally {
      setIsLoadingHistorial(false);
    }
  }, [historialFechaInicio, historialFechaFin]);

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

  const handleMarcarEntrada = async () => {
    setIsClocking(true);
    try {
      const fechaHora = combineDateAndTime(testDate, testTime);
      await marcarEntrada(fechaHora);

      showMessage(`✅ Marcaste entrada a las ${testTime}`);

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
    setIsClocking(true);
    try {
      const fechaHora = combineDateAndTime(testDate, testTime);
      await marcarSalida(fechaHora);

      showMessage(`✅ Marcaste salida a las ${testTime}`);

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

  const handleFilterHistorial = () => {
    loadHistorial();
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
      const horaEntrada = group.entrada?.fechaHora.split(' ')[1]?.substring(0,5) || '-';
      const horaSalida = group.salida?.fechaHora.split(' ')[1]?.substring(0,5) || '-';

      let horasTrabajadas = '-';
      if (group.entrada && group.salida) {
        const entrada = new Date(group.entrada.fechaHora.replace(' ', 'T'));
        const salida = new Date(group.salida.fechaHora.replace(' ', 'T'));
        const diff = (salida.getTime() - entrada.getTime()) / (1000 * 60 * 60);
        horasTrabajadas = `${diff.toFixed(2)}h`;
      }

      const observaciones = [group.entrada?.observaciones, group.salida?.observaciones].filter(Boolean).join(' | ');

      rows.push({ id: fecha, fecha, horaEntrada: horaEntrada, horaSalida: horaSalida, horasTrabajadas, observaciones: observaciones || '-' });
    });

    return rows.sort((a,b) => b.fecha.localeCompare(a.fecha));
  }, [historial]);

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
      render: (value) => (value ? String(value).substring(0,5) : '-'),
    },
    {
      key: 'horaSalidaHoy',
      label: 'Salida Hoy',
      render: (value) => (value ? String(value).substring(0,5) : '-'),
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
      <Card>
        <CardHeader>
          <CardTitle>Registro de Asistencia</CardTitle>
          <CardDescription>Marca tu entrada y salida, o prueba con una hora manual.</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex flex-col sm:flex-row gap-4 items-end">
            <div className="space-y-2">
              <Label>Fecha</Label>
              <DatePicker value={testDate} onChange={(v: string) => setTestDate(v)} />
            </div>
            <div className="space-y-2">
              <Label>Hora</Label>
              <TimePicker value={testTime} onChange={(v: string) => setTestTime(v)} />
            </div>
            <div className="flex items-center space-x-2">
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

          {successMessage && <div className="mt-4 text-green-600">{successMessage}</div>}
          {errorMessage && <div className="mt-4 text-red-600">{errorMessage}</div>}
        </CardContent>
      </Card>

      {canViewDepartments && (
        <Card>
          <CardHeader>
            <CardTitle>Estado del Departamento</CardTitle>
            <CardDescription>Resumen por departamento (solo JEFE/HR/ADMIN)</CardDescription>
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

                <SimpleDataTable data={filteredEmpleados as any} columns={empleadosColumns as any} />
              </div>
            ) : (
              <div>No hay datos del departamento seleccionado.</div>
            )}
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Historial</CardTitle>
          <CardDescription>Consulta tu historial de asistencias</CardDescription>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2 mb-4">
            <div>
              <Label>Fecha inicio</Label>
              <DatePicker value={historialFechaInicio} onChange={(v: string) => setHistorialFechaInicio(v)} />
            </div>
            <div>
              <Label>Fecha fin</Label>
              <DatePicker value={historialFechaFin} onChange={(v: string) => setHistorialFechaFin(v)} />
            </div>
            <div className="flex items-end">
              <Button onClick={handleFilterHistorial} disabled={isLoadingHistorial}>Filtrar</Button>
            </div>
          </div>

          <SimpleDataTable data={historialRows as any} columns={historialColumns as any} />
        </CardContent>
      </Card>
    </div>
  );
}
