import { useCallback, useEffect, useState } from 'react';
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { LiquidacionesCalculoModal } from '@/components/LiquidacionesCalculoModal';
import { Modal } from '@/components/Modal';
import type { DetalleCalculo, LiquidacionCalculada } from '@/services/liquidacionesService';
import { empleadosService, liquidacionesService, type Empleado } from '@/services/apiService';
import { toast } from 'sonner';
import { formatCurrency } from '@/lib/utils';
import { SimpleDataTable, type Column } from '@/components/SimpleDataTable';

const motivoLabels: Record<string, string> = {
  RENUNCIA_VOLUNTARIA: 'Renuncia voluntaria',
  DESPIDO_CON_RESPONSABILIDAD: 'Despido con responsabilidad',
  DESPIDO_SIN_RESPONSABILIDAD: 'Despido sin responsabilidad',
  FINALIZACION_CONTRATO: 'Finalización de contrato',
  JUBILACION: 'Jubilación',
  MUERTE: 'Muerte',
  MUTUO_ACUERDO: 'Mutuo acuerdo',
};

type LiquidacionRow = Partial<LiquidacionCalculada> & {
  id: number;
  idEmpleado?: number | null;
  montoAguinaldoPendiente?: number | null;
};

export function LiquidacionesView() {
  const [isOpen, setIsOpen] = useState(false);
  const [lastResult, setLastResult] = useState<LiquidacionCalculada | null>(null);
  const [detailsOpen, setDetailsOpen] = useState(false);
  const [selectedResult, setSelectedResult] = useState<LiquidacionCalculada | null>(null);
  const [selectedEmpleado, setSelectedEmpleado] = useState<Empleado | null>(null);
  const [loadingEmpleado, setLoadingEmpleado] = useState(false);
  const [liquidaciones, setLiquidaciones] = useState<LiquidacionRow[]>([]);
  const [isLoadingList, setIsLoadingList] = useState(false);
  const [listError, setListError] = useState<string | null>(null);

  const handleOpen = () => setIsOpen(true);
  const handleClose = () => setIsOpen(false);

  const normalizeLiquidacion = useCallback((item: any): LiquidacionRow => ({
    ...item,
    id: Number(item?.id ?? 0),
    idEmpleado: item?.idEmpleado ?? null,
  }), []);

  const loadLiquidaciones = useCallback(async () => {
    setIsLoadingList(true);
    setListError(null);
    try {
      const data = await liquidacionesService.getAllUnpaginated();
      const list = Array.isArray(data) ? data : (data as any)?.content ?? [];
      setLiquidaciones(list.map(normalizeLiquidacion));
    } catch (err) {
      console.error('Error cargando liquidaciones', err);
      const message = err instanceof Error ? err.message : 'No se pudieron cargar las liquidaciones';
      setListError(message);
      toast.error(message);
    } finally {
      setIsLoadingList(false);
    }
  }, [normalizeLiquidacion]);

  useEffect(() => {
    loadLiquidaciones();
  }, [loadLiquidaciones]);

  const handleCalculated = (result: LiquidacionCalculada) => {
    setLastResult(result);
    loadLiquidaciones();
  };

  const calcularDiasPreaviso = (diasTotales: number) => {
    if (diasTotales < 90) return 0;
    if (diasTotales < 180) return 7;
    if (diasTotales < 365) return 15;
    return 30;
  };

  const construirDetallesFallback = (item: LiquidacionRow | LiquidacionCalculada): DetalleCalculo[] => {
    const diasTotales = Number(item.diasTrabajadosTotal ?? 0);
    const salarioDiario = Number(item.salarioPromedioDiario ?? 0);
    const diasPreaviso = calcularDiasPreaviso(diasTotales);
    const montoPreaviso = Number(item.montoPreaviso ?? 0);
    const montoCesantia = Number(item.montoCesantia ?? 0);
    const montoVacaciones = Number(item.montoVacacionesPendientes ?? 0);
    const montoAguinaldoPendiente = 'montoAguinaldoPendiente' in item ? item.montoAguinaldoPendiente : null;
    const montoAguinaldo = Number(item.montoAguinaldoProporcional ?? montoAguinaldoPendiente ?? 0);
    const montoSalarioProporcional = Number(item.montoSalarioProporcional ?? 0);
    const total = Number(item.totalLiquidacion ?? 0);
    const saldoVacaciones = 'saldoVacaciones' in item ? item.saldoVacaciones ?? null : null;

    const anios = Math.floor(diasTotales / 365);
    const meses = Math.floor((diasTotales % 365) / 30);
    const dias = diasTotales % 30;
    const antiguedadStr = `${anios} años, ${meses} meses, ${dias} días`;

    return [
      {
        concepto: 'Antigüedad',
        formula: `${diasTotales} días (${antiguedadStr})`,
        monto: diasTotales,
      },
      {
        concepto: 'Salario diario',
        formula: 'Suma planillas 6 meses / 6 / 30',
        monto: salarioDiario,
      },
      {
        concepto: 'Preaviso',
        formula: item.preaviso_pagado && montoPreaviso > 0
          ? `${diasPreaviso} días × ${formatCurrency(salarioDiario)}`
          : 'No aplica (será trabajado o menos de 3 meses)',
        monto: montoPreaviso,
      },
      {
        concepto: 'Cesantía',
        formula: montoCesantia > 0
          ? 'Tabla Art. 29 Código de Trabajo (máx. 8 años)'
          : 'No aplica para este motivo de salida',
        monto: montoCesantia,
      },
      {
        concepto: 'Aguinaldo proporcional',
        formula: 'Salarios devengados dic-salida / 12',
        monto: montoAguinaldo,
      },
      {
        concepto: 'Vacaciones pendientes',
        formula: saldoVacaciones !== null
          ? `${saldoVacaciones} días × ${formatCurrency(salarioDiario)}`
          : 'Monto registrado',
        monto: montoVacaciones,
      },
      {
        concepto: 'Salario proporcional',
        formula: `Días del mes de salida × ${formatCurrency(salarioDiario)}`,
        monto: montoSalarioProporcional,
      },
      {
        concepto: 'TOTAL LIQUIDACIÓN',
        formula: 'Suma de todos los componentes',
        monto: total,
      },
    ];
  };

  const openDetails = async (item: LiquidacionRow | LiquidacionCalculada) => {
    setSelectedResult(item as LiquidacionCalculada);
    setDetailsOpen(true);
    if (item.idEmpleado) {
      try {
        setLoadingEmpleado(true);
        const emp = await empleadosService.getById(item.idEmpleado);
        setSelectedEmpleado(emp);
      } catch (e) {
        console.error('No se pudo obtener empleado', e);
        setSelectedEmpleado(null);
      } finally {
        setLoadingEmpleado(false);
      }
    } else {
      setSelectedEmpleado(null);
    }
  };

  const columns: Column<LiquidacionRow>[] = [
    { key: 'id', label: 'ID', render: (_v, item) => String(item.id) },
    { key: 'idEmpleado', label: 'Empleado ID', render: (_v, item) => (item.idEmpleado ? String(item.idEmpleado) : '-') },
    { key: 'nombre', label: 'Empleado', render: (_v, item) => `${item.nombreEmpleado || 'Empleado'} ${item.primerApellidoEmpleado || ''} ${item.segundoApellidoEmpleado || ''}`.trim() || 'Empleado' },
    { key: 'motivoSalida', label: 'Motivo salida', render: (_v, item) => (item.motivoSalida ? (motivoLabels[item.motivoSalida] || item.motivoSalida) : '-') },
    { key: 'fechaSalida', label: 'Fecha salida', render: (_v, item) => item.fechaSalida || '-' },
    { key: 'salarioPromedioDiario', label: 'Salario diario', render: (_v, item) => formatCurrency(item.salarioPromedioDiario || 0) },
    { key: 'diasTrabajadosTotal', label: 'Días trabajados', render: (_v, item) => String(item.diasTrabajadosTotal || 0) },
    { key: 'preaviso_pagado', label: 'Preaviso pagado', render: (_v, item) => (item.preaviso_pagado ? 'Sí' : 'No') },
    { key: 'montoPreaviso', label: 'Monto preaviso', render: (_v, item) => formatCurrency(item.montoPreaviso || 0) },
    { key: 'montoCesantia', label: 'Monto cesantía', render: (_v, item) => formatCurrency(item.montoCesantia || 0) },
    { key: 'montoVacacionesPendientes', label: 'Vacaciones pendientes', render: (_v, item) => formatCurrency(item.montoVacacionesPendientes || 0) },
    { key: 'montoAguinaldoProporcional', label: 'Aguinaldo proporcional', render: (_v, item) => formatCurrency(item.montoAguinaldoProporcional ?? item.montoAguinaldoPendiente ?? 0) },
    { key: 'montoSalarioProporcional', label: 'Salario proporcional', render: (_v, item) => formatCurrency(item.montoSalarioProporcional || 0) },
    { key: 'saldoVacaciones', label: 'Saldo vacaciones', render: (_v, item) => `${item.saldoVacaciones ?? 0} días` },
    { key: 'descripcion', label: 'Descripción', render: (_v, item) => item.descripcion || '-' },
    { key: 'detalles', label: 'Detalles', render: (_v, item) => (
      <Button
        variant="outline"
        size="sm"
        onClick={() => openDetails(item)}
      >
        Detalle
      </Button>
    ) },
    { key: 'total', label: 'Total', render: (_v, item) => formatCurrency(item.totalLiquidacion || 0) },
  ];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle>Liquidaciones</CardTitle>
          <CardDescription>Calcular y gestionar liquidaciones por empleado</CardDescription>
        </CardHeader>
        <CardContent className="flex items-center justify-between">
          <div className="text-sm text-muted-foreground">Calcula la liquidación según la legislación vigente y guarda el registro.</div>
          <div>
            <Button onClick={handleOpen}>Calcular liquidación</Button>
          </div>
        </CardContent>
      </Card>

      {lastResult && (
        <Card>
          <CardHeader>
            <CardTitle>Último resultado</CardTitle>
            <CardDescription>Detalle del cálculo reciente</CardDescription>
          </CardHeader>
          <CardContent>
            <SimpleDataTable
              data={[lastResult]}
              columns={columns}
              customActions={(item) => (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={async () => {
                      if (!item.idEmpleado) return;
                      if (!window.confirm('¿Desactivar este empleado?')) return;
                      try {
                        await empleadosService.update(item.idEmpleado, { estaActivo: false });
                        toast.success('Empleado desactivado');
                      } catch (e) {
                        console.error('Error desactivando empleado', e);
                        toast.error('No se pudo desactivar el empleado');
                      }
                    }}
                    className="h-8 px-2 text-destructive"
                    title="Desactivar empleado"
                  >
                    🚫
                  </Button>
                </>
              )}
            />
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle>Historial de liquidaciones</CardTitle>
          <CardDescription>Registros guardados en el sistema</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoadingList ? (
            <div className="text-sm text-muted-foreground">Cargando liquidaciones...</div>
          ) : listError ? (
            <div className="text-sm text-destructive">{listError}</div>
          ) : (
            <SimpleDataTable
              data={liquidaciones}
              columns={columns}
              customActions={(item) => (
                <>
                  <Button
                    variant="ghost"
                    size="sm"
                    onClick={async () => {
                      if (!item.idEmpleado) return;
                      if (!window.confirm('¿Desactivar este empleado?')) return;
                      try {
                        await empleadosService.update(item.idEmpleado, { estaActivo: false });
                        toast.success('Empleado desactivado');
                      } catch (e) {
                        console.error('Error desactivando empleado', e);
                        toast.error('No se pudo desactivar el empleado');
                      }
                    }}
                    className="h-8 px-2 text-destructive"
                    title="Desactivar empleado"
                  >
                    🚫
                  </Button>
                </>
              )}
            />
          )}
        </CardContent>
      </Card>

      <Modal
        isOpen={detailsOpen}
        onClose={() => { setDetailsOpen(false); setSelectedResult(null); }}
        title={`Detalles de la liquidación ${selectedResult ? `#${selectedResult.id}` : ''}`}
      >
        {selectedResult ? (
          <div className="space-y-3">
            <div><strong>Empleado:</strong> {`${selectedResult.nombreEmpleado || ''} ${selectedResult.primerApellidoEmpleado || ''} ${selectedResult.segundoApellidoEmpleado || ''}`}</div>
            <div>
              <strong>Salario mensual:</strong>{' '}
              {loadingEmpleado ? 'Cargando...' : selectedEmpleado ? formatCurrency(selectedEmpleado.puesto?.salarioMinimo || 0) : '—'}
            </div>
            <div><strong>Motivo salida:</strong> {motivoLabels[selectedResult.motivoSalida] || selectedResult.motivoSalida}</div>
            <div><strong>Total:</strong> {formatCurrency(selectedResult.totalLiquidacion || 0)}</div>
            <div>
              <strong>Detalles:</strong>
              {(() => {
                const detalles = selectedResult.detalles && selectedResult.detalles.length > 0
                  ? selectedResult.detalles
                  : construirDetallesFallback(selectedResult);
                return (
                  <ul className="list-disc ml-6 mt-2 space-y-1">
                    {detalles.map((d, idx) => (
                      <li key={idx} className="text-sm">
                        <div><strong>{d.concepto}</strong></div>
                        {d.formula && <div className="text-xs text-muted-foreground">{d.formula}</div>}
                        {/* Antiguedad: mostrar dias en lugar de colones */}
                        {d.concepto.toLowerCase().includes('antig') ? (
                          <div>{`${d.monto} dias`}</div>
                        ) : (
                          <div>{formatCurrency(d.monto)}</div>
                        )}
                        {/* Para aguinaldo mostrar además la suma de salarios (montoAguinaldoProporcional * 12) */}
                        {d.concepto.toLowerCase().includes('aguinal') && (
                          <div className="text-xs text-muted-foreground">Suma salarios (dic→salida): {formatCurrency((selectedResult.montoAguinaldoProporcional || 0) * 12)}</div>
                        )}
                      </li>
                    ))}
                  </ul>
                );
              })()}
            </div>
          </div>
        ) : null}
      </Modal>

      <LiquidacionesCalculoModal isOpen={isOpen} onClose={handleClose} onCalculated={handleCalculated} />
    </div>
  );
}

export default LiquidacionesView;
