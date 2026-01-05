import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { SimpleDataTable } from '@/components/SimpleDataTable';
import { Modal } from '@/components/Modal';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { empleadosService, puestosService, direccionesService, type Empleado, type Puesto } from '@/services/apiService';
import { GenerarUsuarioModal } from '@/components/GenerarUsuarioModal';

interface EmpleadoFormData {
  cedula: string;
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  correoPersonal: string;
  fechaNacimiento: string;
  fechaIngreso: string;
  salarioBase: string | number;
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
    salarioBase: '',
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
      salarioBase: '',
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
      salarioBase: empleado.salarioBase.toString(),
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
        alert('Error al eliminar el empleado');
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
    } else if (new Date(formData.fechaNacimiento) >= new Date()) {
      newErrors.fechaNacimiento = 'La fecha de nacimiento debe ser anterior a hoy';
    }

    // Fecha de ingreso: requerida
    if (!formData.fechaIngreso) {
      newErrors.fechaIngreso = 'La fecha de ingreso es requerida';
    }

    // Salario base: debe ser positivo
    const salario = typeof formData.salarioBase === 'string' ? parseFloat(formData.salarioBase) : formData.salarioBase;
    if (!salario || salario <= 0) {
      newErrors.salarioBase = 'El salario debe ser mayor a 0';
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
      alert('Por favor corrija los errores en el formulario');
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
        salarioBase: typeof formData.salarioBase === 'string' 
          ? parseFloat(formData.salarioBase) 
          : formData.salarioBase,
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
      alert('Error al guardar el empleado');
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

  return (
    <div className="space-y-6">
      <Card>
        <CardHeader>
          <div className="flex justify-between items-center">
            <div>
              <CardTitle>Gestión de Empleados</CardTitle>
              <CardDescription>
                Administra empleados y genera usuarios del sistema
              </CardDescription>
            </div>
            <Button onClick={handleAdd}>
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
            <SimpleDataTable
              data={empleados}
              columns={columns}
              onEdit={handleEdit}
              onDelete={handleDelete}
              customActions={(empleado) => (
                !empleado.nombreUsuario && (
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => handleGenerarUsuario(empleado.id)}
                    className="mr-2"
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
            <Label htmlFor="cedula">Cédula * (9-12 caracteres)</Label>
            <Input
              id="cedula"
              value={formData.cedula}
              onChange={(e) => {
                setFormData({ ...formData, cedula: e.target.value });
                setErrors({ ...errors, cedula: '' });
              }}
              className={errors.cedula ? 'border-red-500' : ''}
              required
            />
            {errors.cedula && <p className="text-xs text-red-500 mt-1">{errors.cedula}</p>}
          </div>
          <div>
            <Label htmlFor="nombre">Nombre Completo * (2-100 caracteres)</Label>
            <Input
              id="nombre"
              value={formData.nombre}
              onChange={(e) => {
                setFormData({ ...formData, nombre: e.target.value });
                setErrors({ ...errors, nombre: '' });
              }}
              className={errors.nombre ? 'border-red-500' : ''}
              required
            />
            {errors.nombre && <p className="text-xs text-red-500 mt-1">{errors.nombre}</p>}
          </div>
          <div>
            <Label htmlFor="primerApellido">Primer Apellido * (2-100 caracteres)</Label>
            <Input
              id="primerApellido"
              value={formData.primerApellido}
              onChange={(e) => {
                setFormData({ ...formData, primerApellido: e.target.value });
                setErrors({ ...errors, primerApellido: '' });
              }}
              className={errors.primerApellido ? 'border-red-500' : ''}
              required
            />
            {errors.primerApellido && <p className="text-xs text-red-500 mt-1">{errors.primerApellido}</p>}
          </div>
          <div>
            <Label htmlFor="segundoApellido">Segundo Apellido * (2-100 caracteres)</Label>
            <Input
              id="segundoApellido"
              value={formData.segundoApellido}
              onChange={(e) => {
                setFormData({ ...formData, segundoApellido: e.target.value });
                setErrors({ ...errors, segundoApellido: '' });
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
            <Input
              id="fechaNacimiento"
              type="date"
              value={formData.fechaNacimiento}
              onChange={(e) => {
                setFormData({ ...formData, fechaNacimiento: e.target.value });
                setErrors({ ...errors, fechaNacimiento: '' });
              }}
              className={errors.fechaNacimiento ? 'border-red-500' : ''}
              required
            />
            {errors.fechaNacimiento && <p className="text-xs text-red-500 mt-1">{errors.fechaNacimiento}</p>}
          </div>
          <div>
            <Label htmlFor="fechaIngreso">Fecha de Ingreso *</Label>
            <Input
              id="fechaIngreso"
              type="date"
              value={formData.fechaIngreso}
              onChange={(e) => {
                setFormData({ ...formData, fechaIngreso: e.target.value });
                setErrors({ ...errors, fechaIngreso: '' });
              }}
              className={errors.fechaIngreso ? 'border-red-500' : ''}
              required
            />
            {errors.fechaIngreso && <p className="text-xs text-red-500 mt-1">{errors.fechaIngreso}</p>}
          </div>
          <div>
            <Label htmlFor="salarioBase">Salario Base * (automático del puesto)</Label>
            <Input
              id="salarioBase"
              type="number"
              step="0.01"
              value={formData.salarioBase}
              onChange={(e) => {
                setFormData({ ...formData, salarioBase: e.target.value });
                setErrors({ ...errors, salarioBase: '' });
              }}
              className={`bg-muted ${errors.salarioBase ? 'border-red-500' : ''}`}
              readOnly
              required
            />
            {errors.salarioBase && <p className="text-xs text-red-500 mt-1">{errors.salarioBase}</p>}
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
            <Label htmlFor="saldoVacaciones">Saldo de Vacaciones (días) *</Label>
            <Input
              id="saldoVacaciones"
              type="number"
              value={formData.saldoVacaciones}
              onChange={(e) => {
                setFormData({ ...formData, saldoVacaciones: parseInt(e.target.value) || 0 });
                setErrors({ ...errors, saldoVacaciones: '' });
              }}
              className={errors.saldoVacaciones ? 'border-red-500' : ''}
              required
            />
            {errors.saldoVacaciones && <p className="text-xs text-red-500 mt-1">{errors.saldoVacaciones}</p>}
          </div>
          <div>
            <Label htmlFor="cuentaIban">Cuenta IBAN (22 caracteres)</Label>
            <Input
              id="cuentaIban"
              value={formData.cuentaIban}
              maxLength={22}
              onChange={(e) => {
                setFormData({ ...formData, cuentaIban: e.target.value });
                setErrors({ ...errors, cuentaIban: '' });
              }}
              className={errors.cuentaIban ? 'border-red-500' : ''}
              placeholder="Opcional - CRxxxxxxxxxxxxxxxxxx"
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
          <div className="flex items-center gap-4">
            <div>
              <label className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={formData.estaActivo}
                  onChange={(e) => setFormData({ ...formData, estaActivo: e.target.checked })}
                />
                <span>Activo</span>
              </label>
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
          </div>
          <div>
            <Label htmlFor="idPuesto">Puesto *</Label>
            <select
              id="idPuesto"
              className={`flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ${errors.idPuesto ? 'border-red-500' : ''}`}
              value={formData.idPuesto}
              onChange={(e) => {
                const puestoId = parseInt(e.target.value);
                const puestoSeleccionado = puestos.find(p => p.id === puestoId);
                if (puestoSeleccionado) {
                  setFormData({ 
                    ...formData, 
                    idPuesto: puestoId,
                    salarioBase: puestoSeleccionado.salarioMinimo,
                  });
                } else {
                  setFormData({ ...formData, idPuesto: puestoId });
                }
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
            <Label htmlFor="provincia">Provincia * (5-100 caracteres)</Label>
            <select
              id="provincia"
              className={`flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm ${errors.provincia ? 'border-red-500' : ''}`}
              value={formData.provincia}
              onChange={(e) => {
                setFormData({ ...formData, provincia: e.target.value });
                setErrors({ ...errors, provincia: '' });
              }}
              required
            >
              <option value="">Seleccione una provincia</option>
              <option value="San José">San José</option>
              <option value="Alajuela">Alajuela</option>
              <option value="Cartago">Cartago</option>
              <option value="Heredia">Heredia</option>
              <option value="Guanacaste">Guanacaste</option>
              <option value="Puntarenas">Puntarenas</option>
              <option value="Limón">Limón</option>
            </select>
            {errors.provincia && <p className="text-xs text-red-500 mt-1">{errors.provincia}</p>}
          </div>

          <div>
            <Label htmlFor="canton">Cantón * (5-100 caracteres)</Label>
            <Input
              id="canton"
              value={formData.canton}
              onChange={(e) => {
                setFormData({ ...formData, canton: e.target.value });
                setErrors({ ...errors, canton: '' });
              }}
              className={errors.canton ? 'border-red-500' : ''}
              required
            />
            {errors.canton && <p className="text-xs text-red-500 mt-1">{errors.canton}</p>}
          </div>

          <div>
            <Label htmlFor="distrito">Distrito * (5-100 caracteres)</Label>
            <Input
              id="distrito"
              value={formData.distrito}
              onChange={(e) => {
                setFormData({ ...formData, distrito: e.target.value });
                setErrors({ ...errors, distrito: '' });
              }}
              className={errors.distrito ? 'border-red-500' : ''}
              required
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
