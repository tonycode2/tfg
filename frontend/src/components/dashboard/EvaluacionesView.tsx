import { useEffect, useState } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { SimpleDataTable } from '@/components/SimpleDataTable';
import { Modal } from '@/components/Modal';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { DatePicker } from '@/components/ui/date-picker';
import { obtenerDepartamentosAccesibles } from '@/services/asistenciaService';
import evaluacionesService, { type EmpleadoEvaluacionResumen, obtenerEvaluacionesPorEmpleado } from '@/services/evaluacionesService';
import { evaluacionesService as apiEvaluaciones, departamentosService, type EvaluacionDesempeno } from '@/services/apiService';
import { toast } from 'sonner';

export function EvaluacionesView() {
  const [departamentos, setDepartamentos] = useState<{ id: number; nombre: string }[]>([]);
  const [selectedDep, setSelectedDep] = useState<number | null>(null);
  const [resumen, setResumen] = useState<(EmpleadoEvaluacionResumen & { id: number })[]>([]);
  const [isLoading, setIsLoading] = useState(false);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [evaluadoSeleccionado, setEvaluadoSeleccionado] = useState<EmpleadoEvaluacionResumen | null>(null);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [isDetailsOpen, setIsDetailsOpen] = useState(false);
  const [detalles, setDetalles] = useState<EvaluacionDesempeno[]>([]);
  const [detailsLoading, setDetailsLoading] = useState(false);

  const [errors, setErrors] = useState<Record<string, string>>({});

  // Form state
  const [fecha, setFecha] = useState<string>('');
  const [periodo, setPeriodo] = useState<string>('');
  const [puntuacion, setPuntuacion] = useState<number | ''>('');
  const [observaciones, setObservaciones] = useState<string>('');
  const [planMejora, setPlanMejora] = useState<string>('');

  useEffect(() => {
    loadDepartamentos();
  }, []);

  async function loadDepartamentos() {
    try {
      const acces = await obtenerDepartamentosAccesibles();

      const depsData = await departamentosService.getAllUnpaginated();
      const depsArray = (depsData as any).content || depsData;
      const filtered = Array.isArray(depsArray) ? depsArray.filter((d: any) => acces.includes(d.id)) : [];
      setDepartamentos(filtered);
      if (filtered.length > 0) {
        setSelectedDep(filtered[0].id);
      }
    } catch (error) {
      console.error('Error cargando departamentos accesibles', error);
      setDepartamentos([]);
    }
  }

  useEffect(() => {
    if (selectedDep != null) {
      loadResumen(selectedDep);
    }
  }, [selectedDep]);

  async function loadResumen(depId: number) {
    setIsLoading(true);
    try {
      const res = await evaluacionesService.obtenerResumenDepartamento(depId);
      const mapped = (res.empleados || []).map((e) => ({ ...e, id: e.empleadoId }));
      setResumen(mapped);
    } catch (error) {
      console.error('Error cargando resumen de evaluaciones', error);
      setResumen([]);
    } finally {
      setIsLoading(false);
    }
  }

  const handleEvaluar = (empleado: EmpleadoEvaluacionResumen & { id: number }) => {
    setEvaluadoSeleccionado(empleado);
    setFecha(new Date().toISOString().split('T')[0]);
    setPeriodo('');
    setPuntuacion('');
    setObservaciones('');
    setPlanMejora('');
    setIsModalOpen(true);
  };

  const handleVerDetalles = async (empleado: EmpleadoEvaluacionResumen & { id: number }) => {
    setEvaluadoSeleccionado(empleado);
    setIsDetailsOpen(true);
    setDetailsLoading(true);
    try {
      const lista = await obtenerEvaluacionesPorEmpleado(empleado.empleadoId);
      setDetalles(Array.isArray(lista) ? lista : []);
    } catch (error) {
      console.error('Error cargando evaluaciones del empleado', error);
      setDetalles([]);
    } finally {
      setDetailsLoading(false);
    }
  };

  const handleSubmit = async () => {
    if (!evaluadoSeleccionado) return;
    if (!validate()) return;
    if (!evaluadoSeleccionado) return;
    setIsSubmitting(true);
    try {
      const payload = {
        fechaEvaluacion: fecha,
        periodoEvaluado: periodo,
        puntuacionFinal: Number(puntuacion),
        observaciones,
        planDeMejora: planMejora,
        idEmpleado: evaluadoSeleccionado.empleadoId,
      };

      await apiEvaluaciones.create(payload);
      setIsModalOpen(false);
      if (selectedDep) await loadResumen(selectedDep);
      toast.success('Evaluación creada correctamente');
    } catch (error: any) {
      console.error('Error creando evaluación', error);
      const validation = (error as any)?.validationErrors;
      if (Array.isArray(validation)) {
        const map: Record<string, string> = {};
        validation.forEach((v: any) => { if (v.field) map[v.field] = v.message; });
        setErrors(map);
      } else {
        const msg = error?.message || 'Error al crear la evaluación';
        toast.error(msg);
      }
    } finally {
      setIsSubmitting(false);
    }
  };

  function validate() {
    const newErrors: Record<string, string> = {};
    if (!fecha) newErrors.fecha = 'La fecha es obligatoria';
    else if (new Date(fecha) > new Date()) newErrors.fecha = 'La fecha no puede ser futura';

    if (!periodo) newErrors.periodo = 'El periodo es obligatorio';
    else if (!/^\d{4}-Q[1-4]$/.test(periodo)) newErrors.periodo = 'Formato inválido. Ej: 2025-Q4';

    if (puntuacion === '' || puntuacion === null) newErrors.puntuacion = 'La puntuación es obligatoria';
    else if (Number(puntuacion) < 0 || Number(puntuacion) > 100) newErrors.puntuacion = 'La puntuación debe estar entre 0 y 100';

    if (!observaciones || observaciones.trim().length < 10) newErrors.observaciones = 'Las observaciones deben tener al menos 10 caracteres';
    if (!planMejora || planMejora.trim().length < 10) newErrors.planMejora = 'El plan de mejora debe tener al menos 10 caracteres';

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  }

  const columns = [
    { key: 'nombre', label: 'Empleado', render: (_v: any, item: EmpleadoEvaluacionResumen & { id: number }) => `${item.nombre} ${item.primerApellido} ${item.segundoApellido}` },
    { key: 'puestoNombre', label: 'Puesto', render: (_v: any, item: EmpleadoEvaluacionResumen & { id: number }) => item.puestoNombre || '-' },
    { key: 'promedioPuntuacion', label: 'Promedio', render: (v: any) => (v != null ? Number(v).toFixed(2) : '-') },
    { key: 'cantidadEvaluaciones', label: 'Cantidad', render: (v: any) => v ?? 0 },
  ];

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
            <div>
              <CardTitle>Evaluaciones de Desempeño</CardTitle>
              <CardDescription>Crear evaluaciones para empleados de sus departamentos</CardDescription>
            </div>
            <div className="flex items-center gap-2">
              {departamentos.length > 1 ? (
                <select aria-label="Seleccionar departamento" value={selectedDep ?? ''} onChange={(e) => setSelectedDep(Number(e.target.value))} className="border rounded px-2 py-1">
                  {departamentos.map((d) => (
                    <option key={d.id} value={d.id}>{d.nombre}</option>
                  ))}
                </select>
              ) : departamentos.length === 1 ? (
                <div className="px-2 py-1">{`Departamento de ${departamentos[0].nombre}`}</div>
              ) : (
                <div className="text-sm text-neutral-400">Sin departamentos accesibles</div>
              )}
            </div>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-8">Cargando...</div>
          ) : (
            <SimpleDataTable
              data={resumen}
              columns={columns}
              customActions={(item: EmpleadoEvaluacionResumen & { id: number }) => (
                <div className="inline-flex items-center gap-2">
                  <Button variant="ghost" size="sm" onClick={() => handleEvaluar(item)}>Evaluar</Button>
                  <Button variant="outline" size="sm" onClick={() => handleVerDetalles(item)}>Ver</Button>
                </div>
              )}
            />
          )}
        </CardContent>
      </Card>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title={evaluadoSeleccionado ? `Evaluar: ${evaluadoSeleccionado.nombre} ${evaluadoSeleccionado.primerApellido}` : 'Evaluar'}>
        <div className="space-y-3">
          <div>
            <Label>Fecha</Label>
            <DatePicker
              value={fecha}
              onChange={(date) => {
                setFecha(date);
                setErrors(prev => { const c = { ...prev }; delete c.fecha; return c; });
              }}
              placeholder="Seleccionar fecha de evaluación"
            />
            {errors.fecha ? <p className="text-xs text-red-400 mt-1">{errors.fecha}</p> : null}
          </div>
          <div>
            <Label>Periodo</Label>
            <Input value={periodo} onChange={(e) => { setPeriodo(e.target.value); setErrors(prev => { const c = { ...prev }; delete c.periodo; return c; }); }} placeholder="Ej: 2025-Q4" />
            {errors.periodo ? <p className="text-xs text-red-400 mt-1">{errors.periodo}</p> : <p className="text-xs text-neutral-400 mt-1">Ej: 2025-Q4</p>}
          </div>
          <div>
            <Label>Puntuación Final</Label>
            <Input value={puntuacion as any} onChange={(e) => { setPuntuacion(e.target.value === '' ? '' : Number(e.target.value)); setErrors(prev => { const c = { ...prev }; delete c.puntuacion; return c; }); }} type="number" min={0} max={100} />
            {errors.puntuacion ? <p className="text-xs text-red-400 mt-1">{errors.puntuacion}</p> : <p className="text-xs text-neutral-400 mt-1">Valor entre 0 y 100</p>}
          </div>
          <div>
            <Label>Observaciones</Label>
            <Input value={observaciones} onChange={(e) => { setObservaciones(e.target.value); setErrors(prev => { const c = { ...prev }; delete c.observaciones; return c; }); }} />
            {errors.observaciones ? <p className="text-xs text-red-400 mt-1">{errors.observaciones}</p> : <p className="text-xs text-neutral-400 mt-1">Mínimo 10 caracteres</p>}
          </div>
          <div>
            <Label>Plan de Mejora</Label>
            <Input value={planMejora} onChange={(e) => { setPlanMejora(e.target.value); setErrors(prev => { const c = { ...prev }; delete c.planMejora; return c; }); }} />
            {errors.planMejora ? <p className="text-xs text-red-400 mt-1">{errors.planMejora}</p> : <p className="text-xs text-neutral-400 mt-1">Mínimo 10 caracteres</p>}
          </div>
          <div className="flex justify-end gap-2">
            <Button variant="ghost" onClick={() => setIsModalOpen(false)} disabled={isSubmitting}>Cancelar</Button>
            <Button onClick={handleSubmit} disabled={isSubmitting}>{isSubmitting ? 'Guardando...' : 'Guardar'}</Button>
          </div>
        </div>
      </Modal>

      <Modal isOpen={isDetailsOpen} onClose={() => setIsDetailsOpen(false)} title={evaluadoSeleccionado ? `Evaluaciones` : 'Evaluaciones'}>
        <div className="space-y-3">
          {detailsLoading ? (
            <div className="text-center py-6">Cargando...</div>
          ) : detalles.length === 0 ? (
            <div className="text-center py-6">Sin evaluaciones para este empleado.</div>
          ) : (
            <div className="overflow-auto max-h-96">
              <table className="w-full text-left table-auto">
                <thead>
                  <tr className="text-sm text-neutral-400">
                    <th className="py-2 pr-4">Fecha</th>
                    <th className="py-2 pr-4">Periodo</th>
                    <th className="py-2 pr-4">Puntuación</th>
                    <th className="py-2 pr-4">Observaciones</th>
                    <th className="py-2 pr-4">Plan de mejora</th>
                  </tr>
                </thead>
                <tbody>
                  {detalles.map((d) => (
                    <tr key={d.id} className="border-t">
                      <td className="py-2 pr-4 text-sm">{d.fechaEvaluacion}</td>
                      <td className="py-2 pr-4 text-sm">{d.periodoEvaluado}</td>
                      <td className="py-2 pr-4 text-sm">{d.puntuacionFinal}</td>
                      <td className="py-2 pr-4 text-sm">{d.observaciones}</td>
                      <td className="py-2 pr-4 text-sm">{d.planDeMejora}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
          <div className="flex justify-end">
            <Button variant="ghost" onClick={() => setIsDetailsOpen(false)}>Cerrar</Button>
          </div>
        </div>
      </Modal>
    </div>
  );
}

export default EvaluacionesView;
