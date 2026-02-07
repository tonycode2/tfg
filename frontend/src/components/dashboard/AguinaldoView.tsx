import { useMemo, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Alert, AlertDescription } from '@/components/ui/alert';
import { StatsCard } from '@/components/StatsCard';
import { SimpleDataTable, type Column } from '@/components/SimpleDataTable';
import { calcularAguinaldos, type AguinaldoCalculado } from '@/services/aguinaldoService';
import { toast } from 'sonner';

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

const UsersIcon = () => (
  <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
    <path
      strokeLinecap="round"
      strokeLinejoin="round"
      strokeWidth={2}
      d="M17 20h5v-2a4 4 0 00-4-4h-1m-6 6H2v-2a4 4 0 014-4h1m5-4a4 4 0 11-8 0 4 4 0 018 0zm6 4a4 4 0 10-8 0 4 4 0 008 0z"
    />
  </svg>
);

const formatCurrency = (value: number): string => {
  const safeValue = Number.isFinite(value) ? value : 0;
  return `₡${safeValue.toLocaleString('es-CR', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`;
};

const formatDate = (value?: string | null): string => {
  if (!value) return 'Sin definir';
  const date = new Date(`${value}T00:00:00`);
  return date.toLocaleDateString('es-CR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  });
};

const getNombreCompleto = (item: AguinaldoCalculado): string => {
  const parts = [item.nombreEmpleado, item.primerApellidoEmpleado, item.segundoApellidoEmpleado]
    .filter((part) => part && part.trim().length > 0);
  return parts.join(' ') || `Empleado ${item.idEmpleado}`;
};

export function AguinaldoView() {
  const [aguinaldos, setAguinaldos] = useState<AguinaldoCalculado[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const currentYear = new Date().getFullYear();
  const periodoInicio = useMemo(() => new Date(currentYear - 1, 11, 1), [currentYear]);
  const periodoFin = useMemo(() => new Date(currentYear, 10, 30), [currentYear]);

  const handleCalcular = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await calcularAguinaldos();
      const aguinaldosArray = Array.isArray(data) ? data : [];
      setAguinaldos(aguinaldosArray);
      toast.success('Aguinaldos calculados correctamente');
    } catch (err) {
      const message = err instanceof Error ? err.message : 'Error al calcular aguinaldos';
      setError(message);
      toast.error(message);
    } finally {
      setLoading(false);
    }
  };

  const totals = useMemo(() => {
    const totalSalarios = aguinaldos.reduce((sum, item) => sum + (item.totalSalariosDevengados || 0), 0);
    const totalAguinaldo = aguinaldos.reduce((sum, item) => sum + (item.montoAguinaldo || 0), 0);
    return {
      empleados: aguinaldos.length,
      totalSalarios,
      totalAguinaldo,
    };
  }, [aguinaldos]);

  const columns: Column<AguinaldoCalculado>[] = [
    {
      key: 'nombreEmpleado',
      label: 'Empleado',
      render: (_value, item) => getNombreCompleto(item),
    },
    {
      key: 'totalSalariosDevengados',
      label: 'Total salarios',
      render: (value) => formatCurrency(Number(value) || 0),
    },
    {
      key: 'montoAguinaldo',
      label: 'Aguinaldo',
      render: (value) => formatCurrency(Number(value) || 0),
    },
    {
      key: 'fechaCalculo',
      label: 'Fecha calculo',
      render: (value) => formatDate(value),
    },
  ];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <GiftIcon />
            Calculo de Aguinaldo
          </CardTitle>
          <CardDescription>
            Periodo actual: {periodoInicio.toLocaleDateString('es-CR')} - {periodoFin.toLocaleDateString('es-CR')}
          </CardDescription>
        </CardHeader>
        <CardContent className="flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div className="text-sm text-muted-foreground">
            Calcula el aguinaldo con base en la suma de salarios devengados entre diciembre y noviembre.
          </div>
          <Button onClick={handleCalcular} disabled={loading} className="gap-2">
            {loading ? 'Calculando...' : 'Calcular aguinaldo'}
          </Button>
        </CardContent>
      </Card>

      {error && (
        <Alert variant="destructive">
          <AlertDescription>{error}</AlertDescription>
        </Alert>
      )}

      <div className="grid gap-4 md:grid-cols-3">
        <StatsCard
          title="Empleados procesados"
          value={totals.empleados}
          icon={<UsersIcon />}
          description="Con aguinaldo calculado"
        />
        <StatsCard
          title="Total salarios"
          value={formatCurrency(totals.totalSalarios)}
          icon={<GiftIcon />}
          description="Salarios devengados"
        />
        <StatsCard
          title="Total aguinaldo"
          value={formatCurrency(totals.totalAguinaldo)}
          icon={<GiftIcon />}
          description="Monto global"
        />
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Detalle por empleado</CardTitle>
          <CardDescription>
            Aguinaldos calculados para el periodo vigente.
          </CardDescription>
        </CardHeader>
        <CardContent>
          <SimpleDataTable data={aguinaldos} columns={columns} />
        </CardContent>
      </Card>
    </div>
  );
}
