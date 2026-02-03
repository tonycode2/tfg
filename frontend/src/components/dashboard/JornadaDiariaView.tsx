import { useState, useEffect } from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Modal } from '@/components/Modal';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { DatePicker } from '@/components/ui/date-picker';
import { SearchableSelect } from '@/components/ui/searchable-select';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import { jornadaDiariaService, empleadosService, type JornadaDiaria, type Empleado } from '@/services/apiService';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { toast } from 'sonner';

interface JornadaDiariaFormData {
  fecha: string;
  horaEntrada: string;
  horaSalida: string;
  horasRegulares: number | string;
  horasExtra: number | string;
  observaciones: string;
  idEmpleado: number | string;
}

export function JornadaDiariaView() {
  const [jornadas, setJornadas] = useState<JornadaDiaria[]>([]);
  const [empleados, setEmpleados] = useState<Empleado[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingJornada, setEditingJornada] = useState<JornadaDiaria | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [jornadaToDelete, setJornadaToDelete] = useState<JornadaDiaria | null>(null);
  const [formData, setFormData] = useState<JornadaDiariaFormData>({
    fecha: '',
    horaEntrada: '',
    horaSalida: '',
    horasRegulares: '',
    horasExtra: '',
    observaciones: '',
    idEmpleado: '',
  });
  
  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(10);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredData, setFilteredData] = useState<JornadaDiaria[]>([]);
  const [paginatedData, setPaginatedData] = useState<JornadaDiaria[]>([]);

  useEffect(() => {
    loadJornadas();
    loadEmpleados();
  }, []);

  // Filtrar datos cuando cambia el término de búsqueda
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredData(jornadas);
      return;
    }

    const searchLower = searchTerm.toLowerCase();
    const filtered = jornadas.filter((jornada) => {
      return (
        jornada.nombreCompleto?.toLowerCase().includes(searchLower) ||
        jornada.fecha.includes(searchLower) ||
        (jornada.observaciones && jornada.observaciones.toLowerCase().includes(searchLower))
      );
    });
    setFilteredData(filtered);
    setPage(0);
  }, [searchTerm, jornadas]);

  // Calcular paginación local
  useEffect(() => {
    const totalPagesCalc = Math.ceil(filteredData.length / pageSize);
    
    const startIndex = page * pageSize;
    const endIndex = startIndex + pageSize;
    setPaginatedData(filteredData.slice(startIndex, endIndex));
    
    if (page >= totalPagesCalc && totalPagesCalc > 0) {
      setPage(0);
    }
  }, [filteredData, page, pageSize]);

  const loadJornadas = async () => {
    try {
      setIsLoading(true);
      const data = await jornadaDiariaService.getAllUnpaginated();
      const jornadasArray = (data as any).content || data;
      setJornadas(Array.isArray(jornadasArray) ? jornadasArray : []);
    } catch (error) {
      console.error('Error cargando jornadas diarias:', error);
      setJornadas([]);
    } finally {
      setIsLoading(false);
    }
  };

  const loadEmpleados = async () => {
    try {
      const data = await empleadosService.getAllUnpaginated();
      const empleadosArray = (data as any).content || data;
      setEmpleados(Array.isArray(empleadosArray) ? empleadosArray : []);
    } catch (error) {
      console.error('Error cargando empleados:', error);
      setEmpleados([]);
    }
  };

  const handleCreate = () => {
    setEditingJornada(null);
    setFormData({
      fecha: '',
      horaEntrada: '',
      horaSalida: '',
      horasRegulares: '',
      horasExtra: '',
      observaciones: '',
      idEmpleado: '',
    });
    setIsModalOpen(true);
  };

  const handleEdit = (jornada: JornadaDiaria) => {
    setEditingJornada(jornada);
    setFormData({
      fecha: jornada.fecha,
      horaEntrada: jornada.horaEntrada,
      horaSalida: jornada.horaSalida,
      horasRegulares: jornada.horasRegulares,
      horasExtra: jornada.horasExtra,
      observaciones: jornada.observaciones || '',
      idEmpleado: jornada.idEmpleado,
    });
    setIsModalOpen(true);
  };

  const handleDelete = (jornada: JornadaDiaria) => {
    setJornadaToDelete(jornada);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!jornadaToDelete?.id) return;

    try {
      await jornadaDiariaService.delete(jornadaToDelete.id);
      await loadJornadas();
      setIsDeleteDialogOpen(false);
      setJornadaToDelete(null);
    } catch (error: any) {
      console.error('Error eliminando jornada:', error);
      toast.error(error.message || 'Error al eliminar la jornada diaria');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    if (!formData.fecha) {
      toast.error('La fecha es obligatoria');
      return;
    }

    if (!formData.horaEntrada || !formData.horaSalida) {
      toast.error('La hora de entrada y salida son obligatorias');
      return;
    }

    if (!formData.idEmpleado) {
      toast.error('Debe seleccionar un empleado');
      return;
    }

    if (formData.horasRegulares === '' || Number(formData.horasRegulares) < 0) {
      toast.error('Las horas regulares deben ser un número válido');
      return;
    }

    if (formData.horasExtra === '' || Number(formData.horasExtra) < 0) {
      toast.error('Las horas extra deben ser un número válido');
      return;
    }

    try {
      const payload = {
        fecha: formData.fecha,
        horaEntrada: formData.horaEntrada,
        horaSalida: formData.horaSalida,
        horasRegulares: Number(formData.horasRegulares),
        horasExtra: Number(formData.horasExtra),
        observaciones: formData.observaciones,
        idEmpleado: Number(formData.idEmpleado),
      };

      if (editingJornada?.id) {
        await jornadaDiariaService.update(editingJornada.id, payload);
      } else {
        await jornadaDiariaService.create(payload);
      }

      await loadJornadas();
      setIsModalOpen(false);
      resetForm();
    } catch (error: any) {
      console.error('Error guardando jornada:', error);
      toast.error(error.message || 'Error al guardar la jornada diaria');
    }
  };

  const resetForm = () => {
    setFormData({
      fecha: '',
      horaEntrada: '',
      horaSalida: '',
      horasRegulares: '',
      horasExtra: '',
      observaciones: '',
      idEmpleado: '',
    });
    setEditingJornada(null);
  };

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0);
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '';
    const [year, month, day] = dateString.split('-');
    return `${day}/${month}/${year}`;
  };

  const formatTime = (timeString: string) => {
    if (!timeString) return '';
    // timeString puede venir como "HH:mm:ss" o solo "HH:mm"
    const parts = timeString.split(':');
    return `${parts[0]}:${parts[1]}`;
  };

  const totalPages = Math.ceil(filteredData.length / pageSize);

  const empleadosOptions = empleados
    .filter(emp => emp.estaActivo)
    .map(emp => ({
      value: emp.id!.toString(),
      label: `${emp.nombre} ${emp.primerApellido} ${emp.segundoApellido}`,
    }));

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Jornada Diaria</h2>
          <p className="text-muted-foreground">
            Registro de horas trabajadas por empleado
          </p>
        </div>
        <Button onClick={handleCreate} className="gap-2">
          <Plus className="h-4 w-4" />
          Nueva Jornada
        </Button>
      </div>

      {/* Barra de búsqueda y selector de tamaño */}
      <div className="flex items-center gap-4">
        <div className="relative flex-1">
          <svg
            className="absolute left-3 top-1/2 transform -translate-y-1/2 w-4 h-4 text-muted-foreground"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
            />
          </svg>
          <Input
            type="text"
            placeholder="Buscar en la tabla..."
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
            className="pl-10"
          />
        </div>
        <div className="flex items-center gap-2">
          <span className="text-sm text-muted-foreground whitespace-nowrap">Mostrar:</span>
          <Select value={String(pageSize)} onValueChange={handlePageSizeChange}>
            <SelectTrigger className="w-[100px]">
              <SelectValue />
            </SelectTrigger>
            <SelectContent>
              <SelectItem value="5">5</SelectItem>
              <SelectItem value="10">10</SelectItem>
              <SelectItem value="20">20</SelectItem>
              <SelectItem value="50">50</SelectItem>
            </SelectContent>
          </Select>
        </div>
      </div>

      <Card className="relative overflow-hidden">
        {isLoading && (
          <div className="absolute inset-0 bg-background/50 flex items-center justify-center z-10 rounded-lg">
            <div className="text-lg text-muted-foreground">Cargando...</div>
          </div>
        )}
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-muted">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Fecha
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Empleado
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Entrada
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Salida
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Horas Regulares
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Horas Extra
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Observaciones
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filteredData.length === 0 ? (
                <tr>
                  <td colSpan={8} className="px-4 py-8 text-center text-muted-foreground">
                    {searchTerm ? 'No se encontraron resultados' : 'No hay registros de jornadas diarias'}
                  </td>
                </tr>
              ) : (
                paginatedData.map((jornada) => (
                  <tr key={jornada.id} className="hover:bg-muted/50">
                    <td className="px-4 py-4 text-sm">
                      {formatDate(jornada.fecha)}
                    </td>
                    <td className="px-4 py-4 text-sm">
                      {jornada.nombreCompleto || `Empleado ${jornada.idEmpleado}`}
                    </td>
                    <td className="px-4 py-4 text-sm">
                      {formatTime(jornada.horaEntrada)}
                    </td>
                    <td className="px-4 py-4 text-sm">
                      {formatTime(jornada.horaSalida)}
                    </td>
                    <td className="px-4 py-4 text-sm font-medium">
                      {jornada.horasRegulares.toFixed(2)} hrs
                    </td>
                    <td className="px-4 py-4 text-sm">
                      <span className={jornada.horasExtra > 0 ? 'text-green-600 font-medium' : ''}>
                        {jornada.horasExtra.toFixed(2)} hrs
                      </span>
                    </td>
                    <td className="px-4 py-4 text-sm text-muted-foreground">
                      {jornada.observaciones || '-'}
                    </td>
                    <td className="px-4 py-4 text-sm text-right">
                      <div className="flex justify-end gap-2">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(jornada)}
                          className="h-8 w-8 p-0"
                        >
                          <Pencil className="h-4 w-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(jornada)}
                          className="h-8 w-8 p-0 text-destructive hover:text-destructive"
                        >
                          <Trash2 className="h-4 w-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>

        {/* Paginación */}
        {totalPages > 1 && (
          <div className="flex items-center justify-between px-4 py-4 border-t">
            <div className="text-sm text-muted-foreground">
              Mostrando {page * pageSize + 1} a {Math.min((page + 1) * pageSize, filteredData.length)} de{' '}
              {filteredData.length} registros
            </div>
            <Pagination>
              <PaginationContent>
                <PaginationItem>
                  <PaginationPrevious
                    onClick={() => setPage(Math.max(0, page - 1))}
                    className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                  />
                </PaginationItem>
                {Array.from({ length: totalPages }, (_, i) => (
                  <PaginationItem key={i}>
                    <PaginationLink
                      onClick={() => setPage(i)}
                      isActive={page === i}
                      className="cursor-pointer"
                    >
                      {i + 1}
                    </PaginationLink>
                  </PaginationItem>
                ))}
                <PaginationItem>
                  <PaginationNext
                    onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                    className={page >= totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
                  />
                </PaginationItem>
              </PaginationContent>
            </Pagination>
          </div>
        )}
      </Card>

      {/* Modal de creación/edición */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          resetForm();
        }}
        title={editingJornada ? 'Editar Jornada Diaria' : 'Nueva Jornada Diaria'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="fecha">Fecha *</Label>
              <DatePicker
                value={formData.fecha}
                onChange={(value) => setFormData({ ...formData, fecha: value })}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="idEmpleado">Empleado *</Label>
              <SearchableSelect
                options={empleadosOptions}
                value={formData.idEmpleado.toString()}
                onChange={(value) => setFormData({ ...formData, idEmpleado: Number(value) })}
                placeholder="Seleccionar empleado"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="horaEntrada">Hora de Entrada *</Label>
              <Input
                id="horaEntrada"
                type="time"
                value={formData.horaEntrada}
                onChange={(e) => setFormData({ ...formData, horaEntrada: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
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

          <div className="grid grid-cols-2 gap-4">
            <div className="space-y-2">
              <Label htmlFor="horasRegulares">Horas Regulares *</Label>
              <Input
                id="horasRegulares"
                type="number"
                step="0.01"
                min="0"
                value={formData.horasRegulares}
                onChange={(e) => setFormData({ ...formData, horasRegulares: e.target.value })}
                required
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="horasExtra">Horas Extra *</Label>
              <Input
                id="horasExtra"
                type="number"
                step="0.01"
                min="0"
                value={formData.horasExtra}
                onChange={(e) => setFormData({ ...formData, horasExtra: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="space-y-2">
            <Label htmlFor="observaciones">Observaciones</Label>
            <Textarea
              id="observaciones"
              value={formData.observaciones}
              onChange={(e) => setFormData({ ...formData, observaciones: e.target.value })}
              rows={3}
              placeholder="Notas adicionales sobre la jornada..."
            />
          </div>

          <div className="flex justify-end gap-2 pt-4">
            <Button
              type="button"
              variant="outline"
              onClick={() => {
                setIsModalOpen(false);
                resetForm();
              }}
            >
              Cancelar
            </Button>
            <Button type="submit">
              {editingJornada ? 'Actualizar' : 'Crear'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Diálogo de confirmación de eliminación */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => setIsDeleteDialogOpen(false)}
        onConfirm={confirmDelete}
        title="Eliminar Jornada Diaria"
        message={`¿Estás seguro de que deseas eliminar el registro de jornada del ${jornadaToDelete ? formatDate(jornadaToDelete.fecha) : ''}?`}
      />
    </div>
  );
}
