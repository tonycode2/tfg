import { useEffect, useState } from 'react';
import { Modal } from './Modal';
import { Label } from './ui/label';
import { Input } from './ui/input';
import { DatePicker } from './ui/date-picker';
import { SearchableSelect, type SearchableSelectOption } from './ui/searchable-select';
import { empleadosService } from '@/services/apiService';
import { calcularLiquidacion, type SolicitudCalculoLiquidacion, type LiquidacionCalculada } from '@/services/liquidacionesService';
import { toast } from 'sonner';

interface Props {
  isOpen: boolean;
  onClose: () => void;
  onCalculated: (result: LiquidacionCalculada) => void;
}

const motivos = [
  { value: 'RENUNCIA_VOLUNTARIA', label: 'Renuncia voluntaria' },
  { value: 'DESPIDO_CON_RESPONSABILIDAD', label: 'Despido con responsabilidad' },
  { value: 'DESPIDO_SIN_RESPONSABILIDAD', label: 'Despido sin responsabilidad' },
  { value: 'FINALIZACION_CONTRATO', label: 'Finalización de contrato' },
  { value: 'JUBILACION', label: 'Jubilación' },
  { value: 'MUERTE', label: 'Muerte' },
  { value: 'MUTUO_ACUERDO', label: 'Mutuo acuerdo' },
];

export function LiquidacionesCalculoModal({ isOpen, onClose, onCalculated }: Props) {
  const [empleadosOptions, setEmpleadosOptions] = useState<SearchableSelectOption[]>([]);
  const [empleadoId, setEmpleadoId] = useState<number | null>(null);
  const [fechaSalida, setFechaSalida] = useState<string>('');
  const [motivo, setMotivo] = useState<string>(motivos[0].value);
  const [preavisoPagado, setPreavisoPagado] = useState<boolean>(true);
  const [descripcion, setDescripcion] = useState<string>('');
  const [isLoading, setIsLoading] = useState(false);

  useEffect(() => {
    if (!isOpen) return;
    (async () => {
      try {
        const list = await empleadosService.getAllUnpaginated();
        const options = list.map((e) => ({ value: e.id, label: `${e.nombre} ${e.primerApellido} ${e.segundoApellido || ''}` }));
        setEmpleadosOptions(options as SearchableSelectOption[]);
      } catch (err) {
        console.error('Error cargando empleados', err);
        toast.error('No se pudieron cargar los empleados');
      }
    })();
  }, [isOpen]);

  const resetForm = () => {
    setEmpleadoId(null);
    setFechaSalida('');
    setMotivo(motivos[0].value);
    setPreavisoPagado(true);
    setDescripcion('');
  };

  const handleSubmit = async () => {
    if (!empleadoId) return toast.error('Seleccione un empleado');
    if (!fechaSalida) return toast.error('Seleccione la fecha de salida');

    const payload: SolicitudCalculoLiquidacion = {
      idEmpleado: Number(empleadoId),
      fechaSalida,
      motivoSalida: motivo,
      preaviso_pagado: preavisoPagado,
      descripcion: descripcion || null,
    };

    try {
      setIsLoading(true);
      const result = await calcularLiquidacion(payload);
      toast.success('Liquidación calculada correctamente');
      onCalculated(result);
      resetForm();
      onClose();
    } catch (err) {
      console.error('Error calculando liquidación', err);
      toast.error(err instanceof Error ? err.message : 'Error al calcular');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <Modal isOpen={isOpen} onClose={onClose} title="Calcular Liquidación" onSubmit={handleSubmit} isLoading={isLoading} submitLabel="Calcular">
      <div className="space-y-4">
        <div>
          <Label>Empleado</Label>
          <div className="mt-2">
            <SearchableSelect
              options={empleadosOptions}
              value={empleadoId ?? null}
              onChange={(v) => setEmpleadoId(Number(v))}
              placeholder="Seleccionar empleado"
            />
          </div>
        </div>

        <div>
          <Label>Fecha de salida</Label>
          <DatePicker
            value={fechaSalida}
            onChange={(date) => setFechaSalida(date)}
            placeholder="Seleccionar fecha de salida"
          />
        </div>

        <div>
          <Label>Motivo de salida</Label>
          <div className="mt-2">
            <SearchableSelect
              options={motivos.slice(0, 3)}
              value={motivo}
              onChange={(v) => setMotivo(String(v))}
              placeholder="Seleccionar motivo"
              searchPlaceholder="Buscar motivo..."
            />
          </div>
        </div>

        <div className="flex items-center gap-3">
          <input id="preaviso" type="checkbox" checked={preavisoPagado} onChange={(e) => setPreavisoPagado(e.target.checked)} />
          <Label htmlFor="preaviso">Preaviso pagado</Label>
        </div>

        <div>
          <Label>Descripción (opcional)</Label>
          <textarea className="w-full p-2 border rounded mt-2" rows={3} value={descripcion} onChange={(e) => setDescripcion(e.target.value)} />
        </div>
      </div>
    </Modal>
  );
}

export default LiquidacionesCalculoModal;
