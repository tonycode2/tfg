import { useEffect, useMemo, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from '@/components/ui/select';
import reportesService from '@/services/reportesService';
import type { LiquidacionResumen, PlanillaDetalle, PlanillaEncabezado } from '@/services/reportesService';
import { toast } from 'sonner';

export function ReportesView() {
  const [planillas, setPlanillas] = useState<PlanillaEncabezado[]>([]);
  const [liquidaciones, setLiquidaciones] = useState<LiquidacionResumen[]>([]);
  const [colillaDetalles, setColillaDetalles] = useState<PlanillaDetalle[]>([]);
  const [cargandoPlanillas, setCargandoPlanillas] = useState(false);
  const [cargandoLiquidaciones, setCargandoLiquidaciones] = useState(false);
  const [cargandoColillaDetalles, setCargandoColillaDetalles] = useState(false);

  const [planillaMes, setPlanillaMes] = useState('');
  const [planillaQuincena, setPlanillaQuincena] = useState('');
  const [planillaId, setPlanillaId] = useState('');

  const [colillaMes, setColillaMes] = useState('');
  const [colillaQuincena, setColillaQuincena] = useState('');
  const [colillaPlanillaId, setColillaPlanillaId] = useState('');
  const [colillaDetalleId, setColillaDetalleId] = useState('');

  const [deduccionesMes, setDeduccionesMes] = useState('');
  const [deduccionesQuincena, setDeduccionesQuincena] = useState('');
  const [deduccionesPlanillaId, setDeduccionesPlanillaId] = useState('');

  const [liquidacionId, setLiquidacionId] = useState('');
  const [fechaInicio, setFechaInicio] = useState('');
  const [fechaFin, setFechaFin] = useState('');

  const monthLabels = useMemo(
    () => [
      'Enero',
      'Febrero',
      'Marzo',
      'Abril',
      'Mayo',
      'Junio',
      'Julio',
      'Agosto',
      'Septiembre',
      'Octubre',
      'Noviembre',
      'Diciembre',
    ],
    []
  );

  const formatQuincenaLabel = (q: string) => {
    if (!q) return q;
    const s = String(q).trim().toLowerCase();
    return s.charAt(0).toUpperCase() + s.slice(1);
  };

  const getMonthKey = (fecha: string) => (fecha ? fecha.slice(0, 7) : '');

  const formatMonthLabel = (monthKey: string) => {
    const [year, month] = monthKey.split('-');
    const monthIndex = Number(month) - 1;
    if (monthIndex < 0 || monthIndex >= monthLabels.length) return monthKey;
    return `${monthLabels[monthIndex]} ${year}`;
  };

  const formatEmpleado = (detalle: PlanillaDetalle) =>
    [detalle.nombreEmpleado, detalle.primerApellidoEmpleado, detalle.segundoApellidoEmpleado]
      .filter(Boolean)
      .join(' ');

  const formatLiquidacion = (liquidacion: LiquidacionResumen) => {
    const nombre = [
      liquidacion.nombreEmpleado,
      liquidacion.primerApellidoEmpleado,
      liquidacion.segundoApellidoEmpleado,
    ]
      .filter(Boolean)
      .join(' ');
    return `${nombre} - ${liquidacion.fechaSalida}`;
  };

  useEffect(() => {
    let active = true;
    const cargarPlanillas = async () => {
      setCargandoPlanillas(true);
      try {
        const data = await reportesService.obtenerPlanillas();
        if (active) setPlanillas(data);
      } catch (e) {
        console.error('Error cargando planillas', e);
        toast.error('No se pudieron cargar las planillas');
      } finally {
        if (active) setCargandoPlanillas(false);
      }
    };
    cargarPlanillas();
    return () => {
      active = false;
    };
  }, []);

  useEffect(() => {
    let active = true;
    const cargarLiquidaciones = async () => {
      setCargandoLiquidaciones(true);
      try {
        const data = await reportesService.obtenerLiquidaciones();
        if (active) setLiquidaciones(data);
      } catch (e) {
        console.error('Error cargando liquidaciones', e);
        toast.error('No se pudieron cargar las liquidaciones');
      } finally {
        if (active) setCargandoLiquidaciones(false);
      }
    };
    cargarLiquidaciones();
    return () => {
      active = false;
    };
  }, []);

  const monthOptions = useMemo(() => {
    const map = new Map<string, string>();
    planillas.forEach((planilla) => {
      const key = getMonthKey(planilla.fechaInicioPeriodo);
      if (key) map.set(key, formatMonthLabel(key));
    });
    return Array.from(map.entries())
      .map(([value, label]) => ({ value, label }))
      .sort((a, b) => a.value.localeCompare(b.value));
  }, [planillas, monthLabels]);

  const quincenaOptions = useMemo(() => {
    const values = new Set<string>();
    planillas.forEach((planilla) => {
      if (planilla.tipoQuincena) values.add(planilla.tipoQuincena);
    });
    return Array.from(values).sort();
  }, [planillas]);

  const filtrarPlanillas = (mes: string, quincena: string) =>
    planillas.filter((planilla) => {
      const coincideMes = mes ? getMonthKey(planilla.fechaInicioPeriodo) === mes : true;
      const coincideQuincena = quincena ? planilla.tipoQuincena === quincena : true;
      return coincideMes && coincideQuincena;
    });

  const planillasPlanilla = useMemo(
    () => filtrarPlanillas(planillaMes, planillaQuincena),
    [planillaMes, planillaQuincena, planillas]
  );

  const planillasColilla = useMemo(
    () => filtrarPlanillas(colillaMes, colillaQuincena),
    [colillaMes, colillaQuincena, planillas]
  );

  const planillasDeducciones = useMemo(
    () => filtrarPlanillas(deduccionesMes, deduccionesQuincena),
    [deduccionesMes, deduccionesQuincena, planillas]
  );

  useEffect(() => {
    if (planillasPlanilla.length === 1) {
      setPlanillaId(String(planillasPlanilla[0].id));
      return;
    }
    if (!planillasPlanilla.some((planilla) => String(planilla.id) === planillaId)) {
      setPlanillaId('');
    }
  }, [planillasPlanilla, planillaId]);

  useEffect(() => {
    if (planillasColilla.length === 1) {
      setColillaPlanillaId(String(planillasColilla[0].id));
      return;
    }
    if (!planillasColilla.some((planilla) => String(planilla.id) === colillaPlanillaId)) {
      setColillaPlanillaId('');
    }
  }, [planillasColilla, colillaPlanillaId]);

  useEffect(() => {
    if (planillasDeducciones.length === 1) {
      setDeduccionesPlanillaId(String(planillasDeducciones[0].id));
      return;
    }
    if (!planillasDeducciones.some((planilla) => String(planilla.id) === deduccionesPlanillaId)) {
      setDeduccionesPlanillaId('');
    }
  }, [planillasDeducciones, deduccionesPlanillaId]);

  useEffect(() => {
    let active = true;
    if (!colillaPlanillaId) {
      setColillaDetalles([]);
      setColillaDetalleId('');
      return () => {
        active = false;
      };
    }

    const cargarDetalles = async () => {
      setCargandoColillaDetalles(true);
      try {
        const data = await reportesService.obtenerPlanillaDetalles(Number(colillaPlanillaId));
        if (active) setColillaDetalles(data);
      } catch (e) {
        console.error('Error cargando detalles de planilla', e);
        toast.error('No se pudieron cargar los detalles de la planilla');
      } finally {
        if (active) setCargandoColillaDetalles(false);
      }
    };

    cargarDetalles();
    return () => {
      active = false;
    };
  }, [colillaPlanillaId]);

  useEffect(() => {
    if (!colillaDetalles.some((detalle) => String(detalle.id) === colillaDetalleId)) {
      setColillaDetalleId('');
    }
  }, [colillaDetalles, colillaDetalleId]);

  const handleDownload = async (fn: () => Promise<void>) => {
    try {
      await fn();
      toast.success('Reporte descargado');
    } catch (e: unknown) {
      console.error('Error generando reporte', e);
      const error = e as { message?: string };
      toast.error(error?.message || 'Error generando el reporte');
    }
  };

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Reportes</CardTitle>
          <CardDescription>Seleccione y descargue los reportes disponibles en PDF.</CardDescription>
        </CardHeader>
        <CardContent className="space-y-6">
          <div className="grid grid-cols-1 gap-4 xl:grid-cols-2">
            <div className="space-y-4 rounded-xl border bg-card/40 p-4">
              <div>
                <h3 className="text-base font-semibold">Planilla</h3>
                <p className="text-sm text-muted-foreground">Filtre por mes y quincena para descargar la planilla.</p>
              </div>
              <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                <Select value={planillaMes} onValueChange={setPlanillaMes} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Mes" />
                  </SelectTrigger>
                  <SelectContent>
                    {monthOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select value={planillaQuincena} onValueChange={setPlanillaQuincena} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Tipo quincena" />
                  </SelectTrigger>
                  <SelectContent>
                    {quincenaOptions.map((quincena) => (
                      <SelectItem key={quincena} value={quincena}>
                        {formatQuincenaLabel(quincena)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                className="w-full sm:w-auto"
                onClick={() => handleDownload(() => reportesService.planilla(Number(planillaId)))}
                disabled={!planillaId}
              >
                Descargar Planilla
              </Button>
            </div>

            <div className="space-y-4 rounded-xl border bg-card/40 p-4">
              <div>
                <h3 className="text-base font-semibold">Colilla de pago</h3>
                <p className="text-sm text-muted-foreground">Seleccione periodo y empleado para descargar la colilla.</p>
              </div>
              <div className="grid grid-cols-1 gap-2 md:grid-cols-3">
                <Select value={colillaMes} onValueChange={setColillaMes} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Mes" />
                  </SelectTrigger>
                  <SelectContent>
                    {monthOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select value={colillaQuincena} onValueChange={setColillaQuincena} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Tipo quincena" />
                  </SelectTrigger>
                  <SelectContent>
                    {quincenaOptions.map((quincena) => (
                      <SelectItem key={quincena} value={quincena}>
                        {formatQuincenaLabel(quincena)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select
                  value={colillaDetalleId}
                  onValueChange={setColillaDetalleId}
                  disabled={!colillaPlanillaId || cargandoColillaDetalles || colillaDetalles.length === 0}
                >
                  <SelectTrigger>
                    <SelectValue placeholder="Empleado" />
                  </SelectTrigger>
                  <SelectContent>
                    {colillaDetalles.map((detalle) => (
                      <SelectItem key={detalle.id} value={String(detalle.id)}>
                        {formatEmpleado(detalle)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                className="w-full sm:w-auto"
                onClick={() => handleDownload(() => reportesService.colilla(Number(colillaDetalleId)))}
                disabled={!colillaDetalleId}
              >
                Descargar Colilla
              </Button>
            </div>

            <div className="space-y-4 rounded-xl border bg-card/40 p-4">
              <div>
                <h3 className="text-base font-semibold">Deducciones</h3>
                <p className="text-sm text-muted-foreground">Descargue deducciones por periodo de planilla.</p>
              </div>
              <div className="grid grid-cols-1 gap-2 md:grid-cols-2">
                <Select value={deduccionesMes} onValueChange={setDeduccionesMes} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Mes" />
                  </SelectTrigger>
                  <SelectContent>
                    {monthOptions.map((option) => (
                      <SelectItem key={option.value} value={option.value}>
                        {option.label}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>

                <Select value={deduccionesQuincena} onValueChange={setDeduccionesQuincena} disabled={cargandoPlanillas}>
                  <SelectTrigger>
                    <SelectValue placeholder="Tipo quincena" />
                  </SelectTrigger>
                  <SelectContent>
                    {quincenaOptions.map((quincena) => (
                      <SelectItem key={quincena} value={quincena}>
                        {formatQuincenaLabel(quincena)}
                      </SelectItem>
                    ))}
                  </SelectContent>
                </Select>
              </div>
              <Button
                className="w-full sm:w-auto"
                onClick={() => handleDownload(() => reportesService.deducciones(Number(deduccionesPlanillaId)))}
                disabled={!deduccionesPlanillaId}
              >
                Descargar Deducciones
              </Button>
            </div>

            <div className="space-y-4 rounded-xl border bg-card/40 p-4">
              <div>
                <h3 className="text-base font-semibold">Liquidación</h3>
                <p className="text-sm text-muted-foreground">Seleccione el empleado liquidado para generar su reporte.</p>
              </div>
              <Select value={liquidacionId} onValueChange={setLiquidacionId} disabled={cargandoLiquidaciones}>
                <SelectTrigger>
                  <SelectValue placeholder="Empleado liquidado" />
                </SelectTrigger>
                <SelectContent>
                  {liquidaciones.map((liquidacion) => (
                    <SelectItem key={liquidacion.id} value={String(liquidacion.id)}>
                      {formatLiquidacion(liquidacion)}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
              <Button
                className="w-full sm:w-auto"
                onClick={() => handleDownload(() => reportesService.liquidacion(Number(liquidacionId)))}
                disabled={!liquidacionId}
              >
                Descargar Liquidación
              </Button>
            </div>
          </div>

          <div className="rounded-xl border bg-card/40 p-4">
            <div className="mb-4">
              <h3 className="text-base font-semibold">Incapacidades (rango)</h3>
              <p className="text-sm text-muted-foreground">Seleccione fecha de inicio y fin para descargar incapacidades.</p>
            </div>
            <div className="grid grid-cols-1 gap-2 md:grid-cols-[1fr_1fr_auto]">
              <Input type="date" value={fechaInicio} onChange={(e) => setFechaInicio(e.target.value)} />
              <Input type="date" value={fechaFin} onChange={(e) => setFechaFin(e.target.value)} />
              <Button
                className="w-full md:w-auto"
                onClick={() => handleDownload(() => reportesService.incapacidades(fechaInicio, fechaFin))}
                disabled={!fechaInicio || !fechaFin}
              >
                Descargar Incapacidades
              </Button>
            </div>
          </div>

          <div className="grid grid-cols-1 gap-4 md:grid-cols-3">
            <div className="space-y-3 rounded-xl border bg-card/40 p-4">
              <h3 className="text-base font-semibold">Vacaciones</h3>
              <p className="text-sm text-muted-foreground">Reporte consolidado de vacaciones.</p>
              <Button className="w-full sm:w-auto" onClick={() => handleDownload(() => reportesService.vacaciones())}>
                Descargar Vacaciones
              </Button>
            </div>

            <div className="space-y-3 rounded-xl border bg-card/40 p-4">
              <h3 className="text-base font-semibold">Antigüedad</h3>
              <p className="text-sm text-muted-foreground">Reporte de años de servicio del personal.</p>
              <Button className="w-full sm:w-auto" onClick={() => handleDownload(() => reportesService.antiguedad())}>
                Descargar Antigüedad
              </Button>
            </div>

            <div className="space-y-3 rounded-xl border bg-card/40 p-4">
              <h3 className="text-base font-semibold">Proyección Cesantía</h3>
              <p className="text-sm text-muted-foreground">Estimación de cesantía del personal activo.</p>
              <Button className="w-full sm:w-auto" onClick={() => handleDownload(() => reportesService.proyeccionCesantia())}>
                Descargar Proyección
              </Button>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
}

export default ReportesView;
