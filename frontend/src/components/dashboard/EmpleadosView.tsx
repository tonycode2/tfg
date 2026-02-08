import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { DataTable } from '@/components/DataTable';
import { Modal } from '@/components/Modal';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { SearchableSelect } from '@/components/ui/searchable-select';
import { DatePicker } from '@/components/ui/date-picker';
import { getProvincias, getCantonesByProvincia, getDistritosByCanton } from '@/data/costaRicaLocations';
import { empleadosService, puestosService, direccionesService, type Empleado, type Puesto } from '@/services/apiService';
import { GenerarUsuarioModal } from '@/components/GenerarUsuarioModal';
import { toast } from 'sonner';

interface EmpleadoFormData {
  cedula: string;
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  correoPersonal: string;
  fechaNacimiento: string;
  fechaIngreso: string;
  cantidadDeHijos: number;
  saldoVacaciones: number;
  cuentaIban: string;
  estaActivo: boolean;
  estaCasado: boolean;
  tipoDeJornada: string;
  idPuesto: number;
  // Campos de dirección
  provincia: string;
  canton: string;
  distrito: string;
  direccionExacta: string;
}

export function EmpleadosView() {
  const [empleados, setEmpleados] = useState<Empleado[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isGenerarUsuarioModalOpen, setIsGenerarUsuarioModalOpen] = useState(false);
  const [selectedEmpleadoId, setSelectedEmpleadoId] = useState<number | null>(null);
  const [editingEmpleado, setEditingEmpleado] = useState<Empleado | null>(null);
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [errors, setErrors] = useState<Record<string, string>>({});
  const [formData, setFormData] = useState<EmpleadoFormData>({
    cedula: '',
    nombre: '',
    primerApellido: '',
    segundoApellido: '',
    correoPersonal: '',
    fechaNacimiento: '',
    fechaIngreso: '',
    cantidadDeHijos: 0,
    saldoVacaciones: 0,
    cuentaIban: '',
    estaActivo: true,
    estaCasado: false,
    tipoDeJornada: 'COMPLETA',
    idPuesto: 0,
    provincia: '',
    canton: '',
    distrito: '',
    direccionExacta: '',
  });

  useEffect(() => {
    loadEmpleados();
    loadPuestos();
  }, []);

  const loadEmpleados = async () => {
    try {
      setIsLoading(true);
      const data = await empleadosService.getAllUnpaginated();
      console.log('Datos recibidos de empleados:', data);
      
      // Si el backend devuelve un objeto paginado, extraer el array content
      const empleadosArray = (data as any).content || data;
      setEmpleados(Array.isArray(empleadosArray) ? empleadosArray : []);
    } catch (error) {
      console.error('Error cargando empleados:', error);
      setEmpleados([]);
    } finally {
      setIsLoading(false);
    }
  };

  const loadPuestos = async () => {
    try {
      const data = await puestosService.getAllUnpaginated();
      // Si el backend devuelve un objeto paginado, extraer el array content
      const puestosArray = (data as any).content || data;
      setPuestos(Array.isArray(puestosArray) ? puestosArray : []);
    } catch (error) {
      console.error('Error cargando puestos:', error);
      setPuestos([]);
    }
  };

  const handleAdd = () => {
    setEditingEmpleado(null);
    setErrors({});
    setFormData({
      cedula: '',
      nombre: '',
      primerApellido: '',
      segundoApellido: '',
      correoPersonal: '',
      fechaNacimiento: '',
      fechaIngreso: '',
      cantidadDeHijos: 0,
      saldoVacaciones: 0,
      cuentaIban: '',
      estaActivo: true,
      estaCasado: false,
      tipoDeJornada: 'COMPLETA',
      idPuesto: 0,
      provincia: '',
      canton: '',
      distrito: '',
      direccionExacta: '',
    });
    setIsModalOpen(true);
  };

  const handleEdit = (empleado: Empleado) => {
    setEditingEmpleado(empleado);
    setErrors({});

    setFormData({
      cedula: empleado.cedula,
      nombre: empleado.nombre,
      primerApellido: empleado.primerApellido,
      segundoApellido: empleado.segundoApellido,
      correoPersonal: empleado.correoPersonal,
      fechaNacimiento: empleado.fechaNacimiento,
      fechaIngreso: empleado.fechaIngreso,
      cantidadDeHijos: empleado.cantidadDeHijos,
      saldoVacaciones: empleado.saldoVacaciones,
      cuentaIban: empleado.cuentaIban || '',
      estaActivo: empleado.estaActivo,
      estaCasado: empleado.estaCasado,
      tipoDeJornada: empleado.tipoDeJornada,
      idPuesto: empleado.puesto.id,
      provincia: empleado.direccion?.provincia || '',
      canton: empleado.direccion?.canton || '',
      distrito: empleado.direccion?.distrito || '',
      direccionExacta: empleado.direccion?.direccionExacta || '',
    });
    setIsModalOpen(true);
  };

  const handleDelete = async (id: number | string) => {
    if (window.confirm('¿Estás seguro de eliminar este empleado?')) {
      try {
        await empleadosService.delete(Number(id));
        loadEmpleados();
      } catch (error) {
        console.error('Error eliminando empleado:', error);
        toast.error('Error al eliminar el empleado');
      }
    }
  };

  const validateForm = (): boolean => {
    const newErrors: Record<string, string> = {};

    // Cédula: 9-12 caracteres
    if (!formData.cedula || formData.cedula.trim().length < 9 || formData.cedula.trim().length > 12) {
      newErrors.cedula = 'La cédula debe tener entre 9 y 12 caracteres';
    }

    // Nombre: 2-100 caracteres
    if (!formData.nombre || formData.nombre.trim().length < 2 || formData.nombre.trim().length > 100) {
      newErrors.nombre = 'El nombre debe tener entre 2 y 100 caracteres';
    }

    // Primer apellido: 2-100 caracteres
    if (!formData.primerApellido || formData.primerApellido.trim().length < 2 || formData.primerApellido.trim().length > 100) {
      newErrors.primerApellido = 'El primer apellido debe tener entre 2 y 100 caracteres';
    }

    // Segundo apellido: 2-100 caracteres
    if (!formData.segundoApellido || formData.segundoApellido.trim().length < 2 || formData.segundoApellido.trim().length > 100) {
      newErrors.segundoApellido = 'El segundo apellido debe tener entre 2 y 100 caracteres';
    }

    // Correo: formato email válido, 2-100 caracteres
    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
    if (!formData.correoPersonal || !emailRegex.test(formData.correoPersonal)) {
      newErrors.correoPersonal = 'Ingrese un correo electrónico válido';
    } else if (formData.correoPersonal.length < 2 || formData.correoPersonal.length > 100) {
      newErrors.correoPersonal = 'El correo debe tener entre 2 y 100 caracteres';
    }

    // Fecha de nacimiento: debe ser una fecha pasada
    if (!formData.fechaNacimiento) {
      newErrors.fechaNacimiento = 'La fecha de nacimiento es requerida';
    } else if (formData.fechaNacimiento >= new Date().toISOString().split('T')[0]) {
      newErrors.fechaNacimiento = 'La fecha de nacimiento debe ser anterior a hoy';
    }

    // Fecha de ingreso: requerida
    if (!formData.fechaIngreso) {
      newErrors.fechaIngreso = 'La fecha de ingreso es requerida';
    }

    // Cantidad de hijos: debe ser positivo o cero
    if (formData.cantidadDeHijos < 0) {
      newErrors.cantidadDeHijos = 'La cantidad de hijos no puede ser negativa';
    }

    // Saldo de vacaciones: requerido
    if (formData.saldoVacaciones === undefined || formData.saldoVacaciones === null) {
      newErrors.saldoVacaciones = 'El saldo de vacaciones es requerido';
    }

    // Cuenta IBAN: exactamente 22 caracteres si se proporciona
    if (formData.cuentaIban && formData.cuentaIban.length !== 22) {
      newErrors.cuentaIban = 'La cuenta IBAN debe tener exactamente 22 caracteres';
    }

    // Tipo de jornada: requerido
    if (!formData.tipoDeJornada || formData.tipoDeJornada.trim().length === 0) {
      newErrors.tipoDeJornada = 'El tipo de jornada es requerido';
    }

    // Puesto: debe ser seleccionado
    if (!formData.idPuesto || formData.idPuesto <= 0) {
      newErrors.idPuesto = 'Debe seleccionar un puesto';
    }

    // Validaciones de dirección
    // Provincia: 5-100 caracteres
    if (!formData.provincia || formData.provincia.trim().length < 5 || formData.provincia.trim().length > 100) {
      newErrors.provincia = 'La provincia debe tener entre 5 y 100 caracteres';
    }

    // Cantón: 5-100 caracteres
    if (!formData.canton || formData.canton.trim().length < 5 || formData.canton.trim().length > 100) {
      newErrors.canton = 'El cantón debe tener entre 5 y 100 caracteres';
    }

    // Distrito: 5-100 caracteres
    if (!formData.distrito || formData.distrito.trim().length < 5 || formData.distrito.trim().length > 100) {
      newErrors.distrito = 'El distrito debe tener entre 5 y 100 caracteres';
    }

    // Dirección exacta (indicaciones): 5-100 caracteres
    if (!formData.direccionExacta || formData.direccionExacta.trim().length < 5 || formData.direccionExacta.trim().length > 100) {
      newErrors.direccionExacta = 'La dirección exacta debe tener entre 5 y 100 caracteres';
    }

    setErrors(newErrors);
    return Object.keys(newErrors).length === 0;
  };

  const handleSubmit = async () => {
    // Validar formulario antes de enviar
    if (!validateForm()) {
      toast.error('Por favor corrija los errores en el formulario');
      return;
    }

    try {
      // Paso 1: Crear dirección primero
      const direccionData = {
        provincia: formData.provincia,
        canton: formData.canton,
        distrito: formData.distrito,
        indicaciones: formData.direccionExacta, // Backend usa "indicaciones"
      };

      const direccionResponse = await direccionesService.create(direccionData);
      const idDireccion = direccionResponse.id;

      // Paso 2: Preparar datos del empleado con el ID de dirección
      const dataToSend = {
        cedula: formData.cedula,
        nombre: formData.nombre,
        primerApellido: formData.primerApellido,
        segundoApellido: formData.segundoApellido,
        correoPersonal: formData.correoPersonal,
        fechaNacimiento: formData.fechaNacimiento,
        fechaIngreso: formData.fechaIngreso,
        cantidadDeHijos: formData.cantidadDeHijos,
        saldoVacaciones: formData.saldoVacaciones,
        cuentaIban: formData.cuentaIban || undefined, // Opcional
        estaActivo: formData.estaActivo,
        estaCasado: formData.estaCasado,
        tipoDeJornada: formData.tipoDeJornada,
        idPuesto: formData.idPuesto,
        idDireccion: idDireccion,
        // idUsuario se asigna después con el botón "Generar Usuario"
      };

      // Paso 3: Crear empleado
      if (editingEmpleado) {
        await empleadosService.update(editingEmpleado.id, dataToSend);
      } else {
        await empleadosService.create(dataToSend);
      }
      setIsModalOpen(false);
      loadEmpleados();
    } catch (error) {
      console.error('Error guardando empleado:', error);
      toast.error('Error al guardar el empleado');
    }
  };

  const handleGenerarUsuario = (id: number) => {
    setSelectedEmpleadoId(id);
    setIsGenerarUsuarioModalOpen(true);
  };

  const columns = [
    { key: 'cedula' as keyof Empleado, label: 'Cédula' },
    { key: 'nombre' as keyof Empleado, label: 'Nombre' },
    { key: 'primerApellido' as keyof Empleado, label: 'Primer Apellido' },
    { key: 'correoPersonal' as keyof Empleado, label: 'Correo' },
    { 
      key: 'puesto' as keyof Empleado, 
      label: 'Puesto',
      render: (value: any) => value?.nombre || 'N/A'
    },
    { 
      key: 'puesto.departamento' as any, 
      label: 'Departamento',
      render: (_value: any, item: Empleado) => item.puesto?.departamento?.nombre || 'N/A'
    },
    {
      key: 'nombreUsuario' as keyof Empleado,
      label: 'Usuario',
      render: (value: any) => value || 'Sin usuario'
    },
  ];

  const selectedPuesto = puestos.find((puesto) => puesto.id === formData.idPuesto);
  const salarioPuesto = selectedPuesto?.salarioMinimo ?? '';

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex flex-col sm:flex-row sm:justify-between sm:items-center gap-3">
            <div>
              <CardTitle>Gestión de Empleados</CardTitle>
              <CardDescription>
                Administra empleados y genera usuarios del sistema
              </CardDescription>
            </div>
            <Button onClick={handleAdd} className="self-end sm:self-auto">
              <svg className="w-4 h-4 mr-2" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
              </svg>
              Nuevo Empleado
            </Button>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="text-center py-8">Cargando...</div>
          ) : (
            <DataTable
              service={empleadosService}
              columns={columns}
              title="Gestión de Empleados"
              hideHeader={true}
              onEdit={handleEdit}
              onCreate={handleAdd}
              refreshTrigger={0}
              customActions={(empleado: any) => (
                !empleado.nombreUsuario && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={(e: React.MouseEvent) => {
                      e.stopPropagation();
                      handleGenerarUsuario(empleado.id);
                    }}
                  >
                    👤 Generar Usuario
                  </Button>
                )
              )}
            />
          )}
        </CardContent>
      </Card>

      <Modal
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        title={editingEmpleado ? 'Editar Empleado' : 'Nuevo Empleado'}
        onSubmit={handleSubmit}
      >
        <div className="grid grid-cols-2 gap-4">
          <div>
            <Label htmlFor="cedula">Cédula *</Label>
            <Input
              id="cedula"
              value={formData.cedula}
              maxLength={12}
              onChange={(e) => {
                const value = e.target.value.replace(/[^0-9]/g, '');
                setFormData({ ...formData, cedula: value });
                setErrors({ ...errors, cedula: '' });
              }}
              onBlur={(e) => {
                const value = e.target.value;
                if (value && (value.length < 9 || value.length > 12)) {
                  setErrors({ ...errors, cedula: 'La cédula debe tener entre 9 y 12 dígitos' });
                }
              }}
              className={errors.cedula ? 'border-red-500' : ''}
              placeholder="123456789"
              required
            />
            {errors.cedula && <p className="text-xs text-red-500 mt-1">{errors.cedula}</p>}
          </div>
          <div>
            <Label htmlFor="nombre">Nombre Completo *</Label>
            <Input
              id="nombre"
              value={formData.nombre}
              onChange={(e) => {
                setFormData({ ...formData, nombre: e.target.value });
                setErrors({ ...errors, nombre: '' });
              }}
              onBlur={(e) => {
                const value = e.target.value.trim();
                if (value && (value.length < 2 || value.length > 100)) {
                  setErrors({ ...errors, nombre: 'El nombre debe tener entre 2 y 100 caracteres' });
                }
              }}
              className={errors.nombre ? 'border-red-500' : ''}
              required
            />
            {errors.nombre && <p className="text-xs text-red-500 mt-1">{errors.nombre}</p>}
          </div>
          <div>
            <Label htmlFor="primerApellido">Primer Apellido *</Label>
            <Input
              id="primerApellido"
              value={formData.primerApellido}
              onChange={(e) => {
                setFormData({ ...formData, primerApellido: e.target.value });
                setErrors({ ...errors, primerApellido: '' });
              }}
              onBlur={(e) => {
                const value = e.target.value.trim();
                if (value && (value.length < 2 || value.length > 100)) {
                  setErrors({ ...errors, primerApellido: 'El primer apellido debe tener entre 2 y 100 caracteres' });
                }
              }}
              className={errors.primerApellido ? 'border-red-500' : ''}
              required
            />
            {errors.primerApellido && <p className="text-xs text-red-500 mt-1">{errors.primerApellido}</p>}
          </div>
          <div>
            <Label htmlFor="segundoApellido">Segundo Apellido *</Label>
            <Input
              id="segundoApellido"
              value={formData.segundoApellido}
              onChange={(e) => {
                setFormData({ ...formData, segundoApellido: e.target.value });
                setErrors({ ...errors, segundoApellido: '' });
              }}
              onBlur={(e) => {
                const value = e.target.value.trim();
                if (value && (value.length < 2 || value.length > 100)) {
                  setErrors({ ...errors, segundoApellido: 'El segundo apellido debe tener entre 2 y 100 caracteres' });
                }
              }}
              className={errors.segundoApellido ? 'border-red-500' : ''}
              required
            />
            {errors.segundoApellido && <p className="text-xs text-red-500 mt-1">{errors.segundoApellido}</p>}
          </div>
          <div>
            <Label htmlFor="correoPersonal">Correo Electrónico *</Label>
            <Input
              id="correoPersonal"
              type="email"
              value={formData.correoPersonal}
              onChange={(e) => {
                setFormData({ ...formData, correoPersonal: e.target.value });
                setErrors({ ...errors, correoPersonal: '' });
              }}
              className={errors.correoPersonal ? 'border-red-500' : ''}
              required
            />
            {errors.correoPersonal && <p className="text-xs text-red-500 mt-1">{errors.correoPersonal}</p>}
          </div>
          <div>
            <Label htmlFor="fechaNacimiento">Fecha de Nacimiento *</Label>
            <DatePicker
              value={formData.fechaNacimiento}
              onChange={(date) => {
                setFormData({ ...formData, fechaNacimiento: date });
                setErrors({ ...errors, fechaNacimiento: '' });
                
                if (date) {
                  const [year, month, day] = date.split('-').map(Number);
                  const fechaNacimiento = new Date(Date.UTC(year, month - 1, day));
                  const hoy = new Date();
                  const edad = Math.floor((hoy.getTime() - fechaNacimiento.getTime()) / (365.25 * 24 * 60 * 60 * 1000));
                  if (edad < 18) {
                    setErrors({ ...errors, fechaNacimiento: 'El empleado debe tener al menos 18 años de edad' });
                  }
                }
              }}
              placeholder="Seleccionar fecha de nacimiento"
              className={errors.fechaNacimiento ? 'border-red-500' : ''}
            />
            {errors.fechaNacimiento && <p className="text-xs text-red-500 mt-1">{errors.fechaNacimiento}</p>}
          </div>
          <div>
            <Label htmlFor="fechaIngreso">Fecha de Ingreso *</Label>
            <DatePicker
              value={formData.fechaIngreso}
              onChange={(date) => {
                setFormData({ ...formData, fechaIngreso: date });
                setErrors({ ...errors, fechaIngreso: '' });
              }}
              placeholder="Seleccionar fecha de ingreso"
              className={errors.fechaIngreso ? 'border-red-500' : ''}
            />
            {errors.fechaIngreso && <p className="text-xs text-red-500 mt-1">{errors.fechaIngreso}</p>}
          </div>
          <div>
            <Label htmlFor="salarioPuesto">Salario del Puesto</Label>
            <Input
              id="salarioPuesto"
              type="number"
              step="0.01"
              value={salarioPuesto}
              className="bg-muted"
              readOnly
            />
          </div>
          <div>
            <Label htmlFor="cantidadDeHijos">Cantidad de Hijos *</Label>
            <Input
              id="cantidadDeHijos"
              type="number"
              min="0"
              value={formData.cantidadDeHijos}
              onChange={(e) => {
                setFormData({ ...formData, cantidadDeHijos: parseInt(e.target.value) || 0 });
                setErrors({ ...errors, cantidadDeHijos: '' });
              }}
              className={errors.cantidadDeHijos ? 'border-red-500' : ''}
              required
            />
            {errors.cantidadDeHijos && <p className="text-xs text-red-500 mt-1">{errors.cantidadDeHijos}</p>}
          </div>
          <div>
            <Label htmlFor="saldoVacaciones">Saldo de Vacaciones (días)</Label>
            <Input
              id="saldoVacaciones"
              type="number"
              value={formData.saldoVacaciones}
              className="bg-muted"
              disabled
            />
          </div>
          <div>
            <Label htmlFor="cuentaIban">Cuenta IBAN</Label>
            <Input
              id="cuentaIban"
              value={formData.cuentaIban}
              maxLength={22}
              onChange={(e) => {
                setFormData({ ...formData, cuentaIban: e.target.value });
                setErrors({ ...errors, cuentaIban: '' });
              }}
              onBlur={(e) => {
                const value = e.target.value.trim();
                if (value && value.length !== 22) {
                  setErrors({ ...errors, cuentaIban: 'La cuenta IBAN debe tener exactamente 22 caracteres' });
                }
              }}
              className={errors.cuentaIban ? 'border-red-500' : ''}
              placeholder="CRxxxxxxxxxxxxxxxxxx"
            />
            {errors.cuentaIban && <p className="text-xs text-red-500 mt-1">{errors.cuentaIban}</p>}
          </div>
          <div>
            <Label htmlFor="tipoDeJornada">Tipo de Jornada *</Label>
            <select
              id="tipoDeJornada"
              className={`flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ${errors.tipoDeJornada ? 'border-red-500' : ''}`}
              value={formData.tipoDeJornada}
              onChange={(e) => {
                setFormData({ ...formData, tipoDeJornada: e.target.value });
                setErrors({ ...errors, tipoDeJornada: '' });
              }}
              required
            >
              <option value="COMPLETA">Completa</option>
              <option value="PARCIAL">Parcial</option>
              <option value="MEDIO_TIEMPO">Medio Tiempo</option>
            </select>
            {errors.tipoDeJornada && <p className="text-xs text-red-500 mt-1">{errors.tipoDeJornada}</p>}
          </div>
          <div>
            <label className="flex items-center gap-2">
              <input
                type="checkbox"
                checked={formData.estaCasado}
                onChange={(e) => setFormData({ ...formData, estaCasado: e.target.checked })}
              />
              <span>Casado</span>
            </label>
          </div>
          <div>
            <Label htmlFor="idPuesto">Puesto *</Label>
            <select
              id="idPuesto"
              className={`flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ${errors.idPuesto ? 'border-red-500' : ''}`}
              value={formData.idPuesto}
              onChange={(e) => {
                const puestoId = parseInt(e.target.value);
                setFormData({ ...formData, idPuesto: puestoId });
                setErrors({ ...errors, idPuesto: '' });
              }}
              required
            >
              <option value={0}>Seleccione un puesto</option>
              {Array.isArray(puestos) && puestos.map((puesto) => (
                <option key={puesto.id} value={puesto.id}>
                  {puesto.nombre}
                </option>
              ))}
            </select>
            {errors.idPuesto && <p className="text-xs text-red-500 mt-1">{errors.idPuesto}</p>}
          </div>

          {/* Sección de Dirección */}
          <div className="col-span-2">
            <h3 className="text-lg font-semibold mb-3 border-b pb-2">Dirección</h3>
          </div>

          <div>
            <Label htmlFor="provincia">Provincia *</Label>
            <SearchableSelect
              options={getProvincias().map(p => ({ value: p, label: p }))}
              value={formData.provincia}
              onChange={(value) => {
                setFormData({ 
                  ...formData, 
                  provincia: value as string,
                  canton: '', // Reset cantón when provincia changes
                  distrito: '' // Reset distrito when provincia changes
                });
                setErrors({ ...errors, provincia: '' });
              }}
              placeholder="Seleccionar provincia..."
              searchPlaceholder="Buscar provincia..."
            />
            {errors.provincia && <p className="text-xs text-red-500 mt-1">{errors.provincia}</p>}
          </div>

          <div>
            <Label htmlFor="canton">Cantón *</Label>
            <SearchableSelect
              options={formData.provincia ? getCantonesByProvincia(formData.provincia).map(c => ({ value: c, label: c })) : []}
              value={formData.canton}
              onChange={(value) => {
                setFormData({ 
                  ...formData, 
                  canton: value as string,
                  distrito: '' // Reset distrito when cantón changes
                });
                setErrors({ ...errors, canton: '' });
              }}
              placeholder="Seleccionar cantón..."
              searchPlaceholder="Buscar cantón..."
              disabled={!formData.provincia}
            />
            {errors.canton && <p className="text-xs text-red-500 mt-1">{errors.canton}</p>}
          </div>

          <div>
            <Label htmlFor="distrito">Distrito *</Label>
            <SearchableSelect
              options={formData.provincia && formData.canton ? getDistritosByCanton(formData.provincia, formData.canton).map(d => ({ value: d, label: d })) : []}
              value={formData.distrito}
              onChange={(value) => {
                setFormData({ 
                  ...formData, 
                  distrito: value as string
                });
                setErrors({ ...errors, distrito: '' });
              }}
              placeholder="Seleccionar distrito..."
              searchPlaceholder="Buscar distrito..."
              disabled={!formData.provincia || !formData.canton}
            />
            {errors.distrito && <p className="text-xs text-red-500 mt-1">{errors.distrito}</p>}
          </div>

          <div>
            <Label htmlFor="direccionExacta">Dirección Exacta * (5-100 caracteres)</Label>
            <textarea
              id="direccionExacta"
              className={`flex min-h-[80px] w-full rounded-md border border-input bg-background px-3 py-2 text-sm ${errors.direccionExacta ? 'border-red-500' : ''}`}
              value={formData.direccionExacta}
              onChange={(e) => {
                setFormData({ ...formData, direccionExacta: e.target.value });
                setErrors({ ...errors, direccionExacta: '' });
              }}
              placeholder="Indique señas exactas de la dirección..."
              required
            />
            {errors.direccionExacta && <p className="text-xs text-red-500 mt-1">{errors.direccionExacta}</p>}
          </div>
        </div>
      </Modal>

      <GenerarUsuarioModal
        isOpen={isGenerarUsuarioModalOpen}
        onClose={() => {
          setIsGenerarUsuarioModalOpen(false);
          setSelectedEmpleadoId(null);
        }}
        empleadoId={selectedEmpleadoId}
        onSuccess={loadEmpleados}
      />
    </div>
  );
}
