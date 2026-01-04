import { useState } from 'react';
import { DataTable, type Column } from '@/components/DataTable';
import { Modal } from '@/components/Modal';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card } from '@/components/ui/card';
import {
  empleadosService,
  departamentosService,
  direccionesService,
  puestosService,
  configuracionRentaService,
  asistenciasService,
  aguinaldosService,
  horasExtraService,
  permisosService,
  liquidacionesService,
  planillasService,
  evaluacionesService,
} from '@/services/apiService';

type EntityType =
  | 'empleados'
  | 'departamentos'
  | 'direcciones'
  | 'puestos'
  | 'configuracion-renta'
  | 'asistencias'
  | 'aguinaldos'
  | 'horas-extra'
  | 'permisos'
  | 'liquidaciones'
  | 'planillas'
  | 'evaluaciones';

interface EntityConfig {
  name: string;
  icon: string;
}

const entities: Record<EntityType, EntityConfig> = {
  empleados: { name: 'Empleados', icon: '👥' },
  departamentos: { name: 'Departamentos', icon: '🏢' },
  direcciones: { name: 'Direcciones', icon: '📍' },
  puestos: { name: 'Puestos', icon: '💼' },
  'configuracion-renta': { name: 'Configuración de Renta', icon: '💰' },
  asistencias: { name: 'Asistencias', icon: '⏰' },
  aguinaldos: { name: 'Aguinaldos', icon: '🎁' },
  'horas-extra': { name: 'Horas Extra', icon: '⏱️' },
  permisos: { name: 'Permisos', icon: '📋' },
  liquidaciones: { name: 'Liquidaciones', icon: '💵' },
  planillas: { name: 'Planillas', icon: '📊' },
  evaluaciones: { name: 'Evaluaciones de Desempeño', icon: '⭐' },
};

export function MantenimientosView() {
  const [selectedEntity, setSelectedEntity] = useState<EntityType | null>(null);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingItem, setEditingItem] = useState<any>(null);
  const [formData, setFormData] = useState<any>({});
  const [refreshTrigger, setRefreshTrigger] = useState(0);
  const [isLoading, setIsLoading] = useState(false);

  const handleCreate = () => {
    setEditingItem(null);
    setFormData({});
    setIsModalOpen(true);
  };

  const handleEdit = (item: any) => {
    setEditingItem(item);
    setFormData({ ...item });
    setIsModalOpen(true);
  };

  const handleCloseModal = () => {
    setIsModalOpen(false);
    setEditingItem(null);
    setFormData({});
  };

  const handleSubmit = async () => {
    if (!selectedEntity) return;

    try {
      setIsLoading(true);
      const service = getServiceForEntity(selectedEntity);

      if (editingItem?.id) {
        await service.update(editingItem.id, formData);
      } else {
        await service.create(formData);
      }

      setRefreshTrigger((prev) => prev + 1);
      handleCloseModal();
    } catch (error) {
      alert(error instanceof Error ? error.message : 'Error al guardar');
    } finally {
      setIsLoading(false);
    }
  };

  const getServiceForEntity = (entity: EntityType) => {
    switch (entity) {
      case 'empleados':
        return empleadosService;
      case 'departamentos':
        return departamentosService;
      case 'direcciones':
        return direccionesService;
      case 'puestos':
        return puestosService;
      case 'configuracion-renta':
        return configuracionRentaService;
      case 'asistencias':
        return asistenciasService;
      case 'aguinaldos':
        return aguinaldosService;
      case 'horas-extra':
        return horasExtraService;
      case 'permisos':
        return permisosService;
      case 'liquidaciones':
        return liquidacionesService;
      case 'planillas':
        return planillasService;
      case 'evaluaciones':
        return evaluacionesService;
      default:
        throw new Error('Entidad no soportada');
    }
  };

  const getColumnsForEntity = (entity: EntityType): Column<any>[] => {
    switch (entity) {
      case 'empleados':
        return [
          { key: 'cedula', label: 'Cédula' },
          { key: 'nombre', label: 'Nombre' },
          { key: 'primerApellido', label: 'Primer Apellido' },
          { key: 'segundoApellido', label: 'Segundo Apellido' },
          { key: 'correoPersonal', label: 'Correo' },
          {
            key: 'salarioBase',
            label: 'Salario',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'estaActivo',
            label: 'Estado',
            render: (value) => (value ? '✅ Activo' : '❌ Inactivo'),
          },
        ];
      case 'departamentos':
        return [
          { key: 'id', label: 'ID' },
          { key: 'nombre', label: 'Nombre' },
        ];
      case 'direcciones':
        return [
          { key: 'provincia', label: 'Provincia' },
          { key: 'canton', label: 'Cantón' },
          { key: 'distrito', label: 'Distrito' },
          { key: 'direccionExacta', label: 'Dirección Exacta' },
        ];
      case 'puestos':
        return [
          { key: 'nombre', label: 'Nombre' },
          {
            key: 'salarioMinimo',
            label: 'Salario Mínimo',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          { key: 'horaEntrada', label: 'Hora Entrada' },
          { key: 'horaSalida', label: 'Hora Salida' },
        ];
      case 'configuracion-renta':
        return [
          {
            key: 'montoMinimo',
            label: 'Monto Mínimo',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'montoMaximo',
            label: 'Monto Máximo',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'porcentaje',
            label: 'Porcentaje',
            render: (value) => `${value}%`,
          },
        ];
      case 'asistencias':
        return [
          { key: 'fecha', label: 'Fecha' },
          { key: 'horaEntrada', label: 'Hora Entrada' },
          { key: 'horaSalida', label: 'Hora Salida' },
          { key: 'horasTrabajadas', label: 'Horas Trabajadas' },
        ];
      case 'aguinaldos':
        return [
          { key: 'anio', label: 'Año' },
          { key: 'fechaInicioPeriodo', label: 'Fecha Inicio' },
          { key: 'fechaFinPeriodo', label: 'Fecha Fin' },
          {
            key: 'montoAguinaldo',
            label: 'Monto',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          { key: 'fechaPago', label: 'Fecha Pago' },
        ];
      case 'horas-extra':
        return [
          { key: 'fechaSolicitud', label: 'Fecha Solicitud' },
          { key: 'cantidadDeHoras', label: 'Horas' },
          { key: 'motivo', label: 'Motivo' },
          { key: 'tipoTarifa', label: 'Tipo Tarifa' },
          {
            key: 'estadoSolicitud',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                PENDIENTE: '🟡 Pendiente',
                APROBADA: '✅ Aprobada',
                RECHAZADA: '❌ Rechazada',
              };
              return estados[value] || value;
            },
          },
          {
            key: 'procesado',
            label: 'Procesado',
            render: (value) => (value ? '✅' : '❌'),
          },
        ];
      case 'permisos':
        return [
          { key: 'fechaInicio', label: 'Fecha Inicio' },
          { key: 'fechaFin', label: 'Fecha Fin' },
          { key: 'diasTotales', label: 'Días' },
          { key: 'tipoPermiso', label: 'Tipo' },
          { key: 'motivo', label: 'Motivo' },
          {
            key: 'estadoSolicitud',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                PENDIENTE: '🟡 Pendiente',
                APROBADA: '✅ Aprobada',
                RECHAZADA: '❌ Rechazada',
              };
              return estados[value] || value;
            },
          },
        ];
      case 'liquidaciones':
        return [
          { key: 'fechaSalida', label: 'Fecha Salida' },
          { key: 'motivoSalida', label: 'Motivo' },
          {
            key: 'montoPreaviso',
            label: 'Preaviso',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'montoCesantia',
            label: 'Cesantía',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'totalLiquidacion',
            label: 'Total',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
        ];
      case 'planillas':
        return [
          { key: 'fechaInicioPeriodo', label: 'Fecha Inicio' },
          { key: 'fechaFinPeriodo', label: 'Fecha Fin' },
          { key: 'fechaPago', label: 'Fecha Pago' },
          {
            key: 'totalPlanillaBruto',
            label: 'Total Bruto',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'totalPlanillaNeto',
            label: 'Total Neto',
            render: (value) => `₡${value?.toLocaleString()}`,
          },
          {
            key: 'estadoPlanilla',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                BORRADOR: '📝 Borrador',
                PROCESADA: '✅ Procesada',
                PAGADA: '💰 Pagada',
              };
              return estados[value] || value;
            },
          },
        ];
      case 'evaluaciones':
        return [
          { key: 'fechaEvaluacion', label: 'Fecha Evaluación' },
          { key: 'periodoEvaluado', label: 'Período' },
          {
            key: 'puntuacionFinal',
            label: 'Puntuación',
            render: (value) => `⭐ ${value}/100`,
          },
          { key: 'observaciones', label: 'Observaciones' },
        ];
      default:
        return [];
    }
  };

  const renderForm = () => {
    if (!selectedEntity) return null;

    switch (selectedEntity) {
      case 'departamentos':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="nombre">Nombre del Departamento</Label>
              <Input
                id="nombre"
                value={formData.nombre || ''}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                placeholder="Ej: Recursos Humanos"
              />
            </div>
          </div>
        );

      case 'configuracion-renta':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="montoMinimo">Monto Mínimo</Label>
              <Input
                id="montoMinimo"
                type="number"
                value={formData.montoMinimo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, montoMinimo: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoMaximo">Monto Máximo</Label>
              <Input
                id="montoMaximo"
                type="number"
                value={formData.montoMaximo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, montoMaximo: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="porcentaje">Porcentaje</Label>
              <Input
                id="porcentaje"
                type="number"
                step="0.01"
                value={formData.porcentaje || ''}
                onChange={(e) =>
                  setFormData({ ...formData, porcentaje: parseFloat(e.target.value) })
                }
              />
            </div>
          </div>
        );

      case 'direcciones':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="provincia">Provincia</Label>
              <Input
                id="provincia"
                value={formData.provincia || ''}
                onChange={(e) =>
                  setFormData({ ...formData, provincia: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="canton">Cantón</Label>
              <Input
                id="canton"
                value={formData.canton || ''}
                onChange={(e) => setFormData({ ...formData, canton: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="distrito">Distrito</Label>
              <Input
                id="distrito"
                value={formData.distrito || ''}
                onChange={(e) => setFormData({ ...formData, distrito: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="direccionExacta">Dirección Exacta</Label>
              <Input
                id="direccionExacta"
                value={formData.direccionExacta || ''}
                onChange={(e) =>
                  setFormData({ ...formData, direccionExacta: e.target.value })
                }
              />
            </div>
          </div>
        );

      case 'puestos':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="nombre">Nombre del Puesto</Label>
              <Input
                id="nombre"
                value={formData.nombre || ''}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                placeholder="Ej: Gerente de Ventas"
              />
            </div>
            <div>
              <Label htmlFor="salarioMinimo">Salario Mínimo</Label>
              <Input
                id="salarioMinimo"
                type="number"
                step="0.01"
                value={formData.salarioMinimo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, salarioMinimo: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="horaEntrada">Hora de Entrada</Label>
              <Input
                id="horaEntrada"
                type="time"
                value={formData.horaEntrada || ''}
                onChange={(e) =>
                  setFormData({ ...formData, horaEntrada: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="horaSalida">Hora de Salida</Label>
              <Input
                id="horaSalida"
                type="time"
                value={formData.horaSalida || ''}
                onChange={(e) =>
                  setFormData({ ...formData, horaSalida: e.target.value })
                }
              />
            </div>
          </div>
        );

      case 'asistencias':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fecha">Fecha</Label>
              <Input
                id="fecha"
                type="date"
                value={formData.fecha || ''}
                onChange={(e) => setFormData({ ...formData, fecha: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="horaEntrada">Hora de Entrada</Label>
              <Input
                id="horaEntrada"
                type="time"
                value={formData.horaEntrada || ''}
                onChange={(e) =>
                  setFormData({ ...formData, horaEntrada: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="horaSalida">Hora de Salida</Label>
              <Input
                id="horaSalida"
                type="time"
                value={formData.horaSalida || ''}
                onChange={(e) =>
                  setFormData({ ...formData, horaSalida: e.target.value })
                }
              />
            </div>
          </div>
        );

      case 'aguinaldos':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="anio">Año</Label>
              <Input
                id="anio"
                type="number"
                value={formData.anio || ''}
                onChange={(e) =>
                  setFormData({ ...formData, anio: parseInt(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaInicioPeriodo">Fecha Inicio Período</Label>
              <Input
                id="fechaInicioPeriodo"
                type="date"
                value={formData.fechaInicioPeriodo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaInicioPeriodo: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaFinPeriodo">Fecha Fin Período</Label>
              <Input
                id="fechaFinPeriodo"
                type="date"
                value={formData.fechaFinPeriodo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaFinPeriodo: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoAguinaldo">Monto Aguinaldo</Label>
              <Input
                id="montoAguinaldo"
                type="number"
                step="0.01"
                value={formData.montoAguinaldo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, montoAguinaldo: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaPago">Fecha de Pago</Label>
              <Input
                id="fechaPago"
                type="date"
                value={formData.fechaPago || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaPago: e.target.value })
                }
              />
            </div>
          </div>
        );

      case 'horas-extra':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaSolicitud">Fecha de Solicitud</Label>
              <Input
                id="fechaSolicitud"
                type="date"
                value={formData.fechaSolicitud || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaSolicitud: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="cantidadDeHoras">Cantidad de Horas</Label>
              <Input
                id="cantidadDeHoras"
                type="number"
                step="0.5"
                value={formData.cantidadDeHoras || ''}
                onChange={(e) =>
                  setFormData({ ...formData, cantidadDeHoras: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="motivo">Motivo</Label>
              <Input
                id="motivo"
                value={formData.motivo || ''}
                onChange={(e) => setFormData({ ...formData, motivo: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="tipoTarifa">Tipo de Tarifa</Label>
              <select
                id="tipoTarifa"
                value={formData.tipoTarifa || 'NORMAL'}
                onChange={(e) =>
                  setFormData({ ...formData, tipoTarifa: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="NORMAL">Normal (1.5x)</option>
                <option value="FERIADO">Feriado (2x)</option>
              </select>
            </div>
            <div>
              <Label htmlFor="estadoSolicitud">Estado</Label>
              <select
                id="estadoSolicitud"
                value={formData.estadoSolicitud || 'PENDIENTE'}
                onChange={(e) =>
                  setFormData({ ...formData, estadoSolicitud: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="PENDIENTE">Pendiente</option>
                <option value="APROBADA">Aprobada</option>
                <option value="RECHAZADA">Rechazada</option>
              </select>
            </div>
          </div>
        );

      case 'permisos':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaInicio">Fecha Inicio</Label>
              <Input
                id="fechaInicio"
                type="date"
                value={formData.fechaInicio || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaInicio: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaFin">Fecha Fin</Label>
              <Input
                id="fechaFin"
                type="date"
                value={formData.fechaFin || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaFin: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="diasTotales">Días Totales</Label>
              <Input
                id="diasTotales"
                type="number"
                value={formData.diasTotales || ''}
                onChange={(e) =>
                  setFormData({ ...formData, diasTotales: parseInt(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="tipoPermiso">Tipo de Permiso</Label>
              <select
                id="tipoPermiso"
                value={formData.tipoPermiso || 'VACACIONES'}
                onChange={(e) =>
                  setFormData({ ...formData, tipoPermiso: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="VACACIONES">Vacaciones</option>
                <option value="ENFERMEDAD">Enfermedad</option>
                <option value="PERSONAL">Personal</option>
                <option value="MATERNIDAD">Maternidad</option>
                <option value="PATERNIDAD">Paternidad</option>
              </select>
            </div>
            <div>
              <Label htmlFor="motivo">Motivo</Label>
              <Input
                id="motivo"
                value={formData.motivo || ''}
                onChange={(e) => setFormData({ ...formData, motivo: e.target.value })}
              />
            </div>
            <div>
              <Label htmlFor="estadoSolicitud">Estado</Label>
              <select
                id="estadoSolicitud"
                value={formData.estadoSolicitud || 'PENDIENTE'}
                onChange={(e) =>
                  setFormData({ ...formData, estadoSolicitud: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="PENDIENTE">Pendiente</option>
                <option value="APROBADA">Aprobada</option>
                <option value="RECHAZADA">Rechazada</option>
              </select>
            </div>
          </div>
        );

      case 'liquidaciones':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaSalida">Fecha de Salida</Label>
              <Input
                id="fechaSalida"
                type="date"
                value={formData.fechaSalida || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaSalida: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="motivoSalida">Motivo de Salida</Label>
              <Input
                id="motivoSalida"
                value={formData.motivoSalida || ''}
                onChange={(e) =>
                  setFormData({ ...formData, motivoSalida: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoPreaviso">Monto Preaviso</Label>
              <Input
                id="montoPreaviso"
                type="number"
                step="0.01"
                value={formData.montoPreaviso || ''}
                onChange={(e) =>
                  setFormData({ ...formData, montoPreaviso: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoCesantia">Monto Cesantía</Label>
              <Input
                id="montoCesantia"
                type="number"
                step="0.01"
                value={formData.montoCesantia || ''}
                onChange={(e) =>
                  setFormData({ ...formData, montoCesantia: parseFloat(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoVacacionesPendientes">Vacaciones Pendientes</Label>
              <Input
                id="montoVacacionesPendientes"
                type="number"
                step="0.01"
                value={formData.montoVacacionesPendientes || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    montoVacacionesPendientes: parseFloat(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <Label htmlFor="montoAguinaldoPendiente">Aguinaldo Pendiente</Label>
              <Input
                id="montoAguinaldoPendiente"
                type="number"
                step="0.01"
                value={formData.montoAguinaldoPendiente || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    montoAguinaldoPendiente: parseFloat(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <Label htmlFor="totalLiquidacion">Total Liquidación</Label>
              <Input
                id="totalLiquidacion"
                type="number"
                step="0.01"
                value={formData.totalLiquidacion || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    totalLiquidacion: parseFloat(e.target.value),
                  })
                }
              />
            </div>
          </div>
        );

      case 'planillas':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaInicioPeriodo">Fecha Inicio Período</Label>
              <Input
                id="fechaInicioPeriodo"
                type="date"
                value={formData.fechaInicioPeriodo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaInicioPeriodo: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaFinPeriodo">Fecha Fin Período</Label>
              <Input
                id="fechaFinPeriodo"
                type="date"
                value={formData.fechaFinPeriodo || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaFinPeriodo: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="fechaPago">Fecha de Pago</Label>
              <Input
                id="fechaPago"
                type="date"
                value={formData.fechaPago || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaPago: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="totalPlanillaBruto">Total Planilla Bruto</Label>
              <Input
                id="totalPlanillaBruto"
                type="number"
                step="0.01"
                value={formData.totalPlanillaBruto || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    totalPlanillaBruto: parseFloat(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <Label htmlFor="totalPlanillaNeto">Total Planilla Neto</Label>
              <Input
                id="totalPlanillaNeto"
                type="number"
                step="0.01"
                value={formData.totalPlanillaNeto || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    totalPlanillaNeto: parseFloat(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <Label htmlFor="estadoPlanilla">Estado de Planilla</Label>
              <select
                id="estadoPlanilla"
                value={formData.estadoPlanilla || 'BORRADOR'}
                onChange={(e) =>
                  setFormData({ ...formData, estadoPlanilla: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="BORRADOR">Borrador</option>
                <option value="PROCESADA">Procesada</option>
                <option value="PAGADA">Pagada</option>
              </select>
            </div>
          </div>
        );

      case 'evaluaciones':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaEvaluacion">Fecha de Evaluación</Label>
              <Input
                id="fechaEvaluacion"
                type="date"
                value={formData.fechaEvaluacion || ''}
                onChange={(e) =>
                  setFormData({ ...formData, fechaEvaluacion: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="periodoEvaluado">Período Evaluado</Label>
              <Input
                id="periodoEvaluado"
                value={formData.periodoEvaluado || ''}
                onChange={(e) =>
                  setFormData({ ...formData, periodoEvaluado: e.target.value })
                }
                placeholder="Ej: Enero-Junio 2026"
              />
            </div>
            <div>
              <Label htmlFor="puntuacionFinal">Puntuación Final (0-100)</Label>
              <Input
                id="puntuacionFinal"
                type="number"
                min="0"
                max="100"
                value={formData.puntuacionFinal || ''}
                onChange={(e) =>
                  setFormData({
                    ...formData,
                    puntuacionFinal: parseFloat(e.target.value),
                  })
                }
              />
            </div>
            <div>
              <Label htmlFor="observaciones">Observaciones</Label>
              <Input
                id="observaciones"
                value={formData.observaciones || ''}
                onChange={(e) =>
                  setFormData({ ...formData, observaciones: e.target.value })
                }
              />
            </div>
            <div>
              <Label htmlFor="planDeMejora">Plan de Mejora</Label>
              <Input
                id="planDeMejora"
                value={formData.planDeMejora || ''}
                onChange={(e) =>
                  setFormData({ ...formData, planDeMejora: e.target.value })
                }
              />
            </div>
          </div>
        );

      default:
        return (
          <div className="text-center text-muted-foreground py-8">
            Formulario no implementado para esta entidad.
            <br />
            Por favor, implementa el formulario específico.
          </div>
        );
    }
  };

  if (!selectedEntity) {
    return (
      <div className="space-y-6">
        <div>
          <h2 className="text-3xl font-bold text-foreground">
            Mantenimientos y Consultas
          </h2>
          <p className="text-muted-foreground mt-2">
            Selecciona una entidad para gestionar sus datos
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
          {(Object.keys(entities) as EntityType[]).map((key) => (
            <Card
              key={key}
              className="p-6 hover:shadow-lg transition-shadow cursor-pointer"
              onClick={() => setSelectedEntity(key)}
            >
              <div className="flex items-center gap-4">
                <div className="text-4xl">{entities[key].icon}</div>
                <div>
                  <h3 className="text-lg font-semibold text-foreground">
                    {entities[key].name}
                  </h3>
                  <p className="text-sm text-muted-foreground">
                    Ver y gestionar registros
                  </p>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <button
          onClick={() => setSelectedEntity(null)}
          className="text-muted-foreground hover:text-foreground transition-colors"
        >
          <svg
            className="w-6 h-6"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M15 19l-7-7 7-7"
            />
          </svg>
        </button>
        <div>
          <h2 className="text-3xl font-bold text-foreground">
            {entities[selectedEntity].name}
          </h2>
          <p className="text-muted-foreground mt-1">
            Gestiona los registros de {entities[selectedEntity].name.toLowerCase()}
          </p>
        </div>
      </div>

      <DataTable
        service={getServiceForEntity(selectedEntity)}
        columns={getColumnsForEntity(selectedEntity)}
        title={entities[selectedEntity].name}
        onEdit={handleEdit}
        onCreate={handleCreate}
        refreshTrigger={refreshTrigger}
      />

      <Modal
        isOpen={isModalOpen}
        onClose={handleCloseModal}
        title={
          editingItem
            ? `Editar ${entities[selectedEntity].name}`
            : `Nuevo ${entities[selectedEntity].name}`
        }
        onSubmit={handleSubmit}
        isLoading={isLoading}
      >
        {renderForm()}
      </Modal>
    </div>
  );
}
