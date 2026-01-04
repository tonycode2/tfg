import { useState, useEffect } from 'react';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { SimpleDataTable } from '@/components/SimpleDataTable';
import { Modal } from '@/components/Modal';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { empleadosService, departamentosService, puestosService, type Empleado, type Departamento, type Puesto } from '@/services/apiService';
import { GenerarUsuarioModal } from '@/components/GenerarUsuarioModal';

interface EmpleadoFormData {
  primerNombre: string;
  segundoNombre: string;
  primerApellido: string;
  segundoApellido: string;
  numeroIdentificacion: string;
  fechaNacimiento: string;
  fechaContratacion: string;
  salarioBase: string | number;
  correoPersonal: string;
  correoEmpresarial: string;
  puesto: { id: number };
  departamento: { id: number };
  horaEntrada: string;
  horaSalida: string;
}

const formatTimeForBackend = (time: string): string => {
  if (!time) return '';
  if (time.includes(':')) {
    const parts = time.split(':');
    if (parts.length === 2) {
      return `${time}:00`;
    }
  }
  return time;
};

export function EmpleadosView() {
  const [empleados, setEmpleados] = useState<Empleado[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isGenerarUsuarioModalOpen, setIsGenerarUsuarioModalOpen] = useState(false);
  const [selectedEmpleadoId, setSelectedEmpleadoId] = useState<number | null>(null);
  const [editingEmpleado, setEditingEmpleado] = useState<Empleado | null>(null);
  const [departamentos, setDepartamentos] = useState<Departamento[]>([]);
  const [puestos, setPuestos] = useState<Puesto[]>([]);
  const [formData, setFormData] = useState<EmpleadoFormData>({
    primerNombre: '',
    segundoNombre: '',
    primerApellido: '',
    segundoApellido: '',
    numeroIdentificacion: '',
    fechaNacimiento: '',
    fechaContratacion: '',
    salarioBase: '',
    correoPersonal: '',
    correoEmpresarial: '',
    puesto: { id: 0 },
    departamento: { id: 0 },
    horaEntrada: '',
    horaSalida: '',
  });

  useEffect(() => {
    loadEmpleados();
    loadDepartamentos();
    loadPuestos();
  }, []);

  const loadEmpleados = async () => {
    try {
      setIsLoading(true);
      const data = await empleadosService.getAllUnpaginated();
      setEmpleados(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando empleados:', error);
      setEmpleados([]);
    } finally {
      setIsLoading(false);
    }
  };

  const loadDepartamentos = async () => {
    try {
      const data = await departamentosService.getAllUnpaginated();
      setDepartamentos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando departamentos:', error);
      setDepartamentos([]);
    }
  };

  const loadPuestos = async () => {
    try {
      const data = await puestosService.getAllUnpaginated();
      setPuestos(Array.isArray(data) ? data : []);
    } catch (error) {
      console.error('Error cargando puestos:', error);
      setPuestos([]);
    }
  };

  const handleAdd = () => {
    setEditingEmpleado(null);
    setFormData({
      primerNombre: '',
      segundoNombre: '',
      primerApellido: '',
      segundoApellido: '',
      numeroIdentificacion: '',
      fechaNacimiento: '',
      fechaContratacion: '',
      salarioBase: '',
      correoPersonal: '',
      correoEmpresarial: '',
      puesto: { id: 0 },
      departamento: { id: 0 },
      horaEntrada: '',
      horaSalida: '',
    });
    setIsModalOpen(true);
  };

  const handleEdit = (empleado: Empleado) => {
    setEditingEmpleado(empleado);
    
    const formatTimeForInput = (time: string): string => {
      if (!time) return '';
      if (time.includes(':')) {
        const parts = time.split(':');
        return `${parts[0]}:${parts[1]}`;
      }
      return time;
    };

    setFormData({
      primerNombre: empleado.primerNombre,
      segundoNombre: empleado.segundoNombre || '',
      primerApellido: empleado.primerApellido,
      segundoApellido: empleado.segundoApellido || '',
      numeroIdentificacion: empleado.numeroIdentificacion,
      fechaNacimiento: empleado.fechaNacimiento,
      fechaContratacion: empleado.fechaContratacion,
      salarioBase: empleado.salarioBase.toString(),
      correoPersonal: empleado.correoPersonal,
      correoEmpresarial: empleado.correoEmpresarial,
      puesto: { id: empleado.puesto.id },
      departamento: { id: empleado.departamento.id },
      horaEntrada: formatTimeForInput(empleado.horaEntrada),
      horaSalida: formatTimeForInput(empleado.horaSalida),
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

  const handleSubmit = async () => {
    const dataToSend: any = {
      ...formData,
      salarioBase: typeof formData.salarioBase === 'string' 
        ? parseFloat(formData.salarioBase) 
        : formData.salarioBase,
      horaEntrada: formatTimeForBackend(formData.horaEntrada),
      horaSalida: formatTimeForBackend(formData.horaSalida),
    };

    try {
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
    { key: 'numeroIdentificacion' as keyof Empleado, label: 'Identificación' },
    { key: 'primerNombre' as keyof Empleado, label: 'Primer Nombre' },
    { key: 'primerApellido' as keyof Empleado, label: 'Primer Apellido' },
    { key: 'correoEmpresarial' as keyof Empleado, label: 'Correo' },
    { 
      key: 'puesto' as keyof Empleado, 
      label: 'Puesto',
      render: (value: any) => value?.nombre || 'N/A'
    },
    { 
      key: 'departamento' as keyof Empleado, 
      label: 'Departamento',
      render: (value: any) => value?.nombre || 'N/A'
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
            <Label htmlFor="primerNombre">Primer Nombre *</Label>
            <Input
              id="primerNombre"
              value={formData.primerNombre}
              onChange={(e) => setFormData({ ...formData, primerNombre: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="segundoNombre">Segundo Nombre</Label>
            <Input
              id="segundoNombre"
              value={formData.segundoNombre}
              onChange={(e) => setFormData({ ...formData, segundoNombre: e.target.value })}
            />
          </div>
          <div>
            <Label htmlFor="primerApellido">Primer Apellido *</Label>
            <Input
              id="primerApellido"
              value={formData.primerApellido}
              onChange={(e) => setFormData({ ...formData, primerApellido: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="segundoApellido">Segundo Apellido</Label>
            <Input
              id="segundoApellido"
              value={formData.segundoApellido}
              onChange={(e) => setFormData({ ...formData, segundoApellido: e.target.value })}
            />
          </div>
          <div>
            <Label htmlFor="numeroIdentificacion">Identificación *</Label>
            <Input
              id="numeroIdentificacion"
              value={formData.numeroIdentificacion}
              onChange={(e) => setFormData({ ...formData, numeroIdentificacion: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="fechaNacimiento">Fecha de Nacimiento *</Label>
            <Input
              id="fechaNacimiento"
              type="date"
              value={formData.fechaNacimiento}
              onChange={(e) => setFormData({ ...formData, fechaNacimiento: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="fechaContratacion">Fecha de Contratación *</Label>
            <Input
              id="fechaContratacion"
              type="date"
              value={formData.fechaContratacion}
              onChange={(e) => setFormData({ ...formData, fechaContratacion: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="salarioBase">Salario Base *</Label>
            <Input
              id="salarioBase"
              type="number"
              step="0.01"
              value={formData.salarioBase}
              onChange={(e) => setFormData({ ...formData, salarioBase: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="correoPersonal">Correo Personal *</Label>
            <Input
              id="correoPersonal"
              type="email"
              value={formData.correoPersonal}
              onChange={(e) => setFormData({ ...formData, correoPersonal: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="correoEmpresarial">Correo Empresarial *</Label>
            <Input
              id="correoEmpresarial"
              type="email"
              value={formData.correoEmpresarial}
              onChange={(e) => setFormData({ ...formData, correoEmpresarial: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="departamento">Departamento *</Label>
            <select
              id="departamento"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={formData.departamento.id}
              onChange={(e) => setFormData({ ...formData, departamento: { id: parseInt(e.target.value) } })}
              required
            >
              <option value={0}>Seleccione un departamento</option>
              {Array.isArray(departamentos) && departamentos.map((dept) => (
                <option key={dept.id} value={dept.id}>
                  {dept.nombre}
                </option>
              ))}
            </select>
          </div>
          <div>
            <Label htmlFor="puesto">Puesto *</Label>
            <select
              id="puesto"
              className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              value={formData.puesto.id}
              onChange={(e) => setFormData({ ...formData, puesto: { id: parseInt(e.target.value) } })}
              required
            >
              <option value={0}>Seleccione un puesto</option>
              {Array.isArray(puestos) && puestos.map((puesto) => (
                <option key={puesto.id} value={puesto.id}>
                  {puesto.nombre}
                </option>
              ))}
            </select>
          </div>
          <div>
            <Label htmlFor="horaEntrada">Hora de Entrada *</Label>
            <Input
              id="horaEntrada"
              type="time"
              value={formData.horaEntrada}
              onChange={(e) => setFormData({ ...formData, horaEntrada: e.target.value })}
              required
            />
          </div>
          <div>
            <Label htmlFor="horaSalida">Hora de Salida *</Label>
            <Input
              id="horaSalida"
              type="time"
              value={formData.horaSalida}
              onChange={(e) => setFormData({ ...formData, horaSalida: e.target.value })}
              required
            />
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
