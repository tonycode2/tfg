import { useState, useEffect } from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Modal } from '@/components/Modal';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/textarea';
import { DatePicker } from '@/components/ui/date-picker';
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
import { diasFeriadosService, type DiaFeriado } from '@/services/apiService';
import { Plus, Pencil, Trash2 } from 'lucide-react';
import { ConfirmDialog } from '@/components/ConfirmDialog';

interface DiaFeriadoFormData {
  nombre: string;
  fecha: string;
  descripcion: string;
}

export function DiasFeriadosView() {
  const [feriados, setFeriados] = useState<DiaFeriado[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingFeriado, setEditingFeriado] = useState<DiaFeriado | null>(null);
  const [isDeleteDialogOpen, setIsDeleteDialogOpen] = useState(false);
  const [feriadoToDelete, setFeriadoToDelete] = useState<DiaFeriado | null>(null);
  const [formData, setFormData] = useState<DiaFeriadoFormData>({
    nombre: '',
    fecha: '',
    descripcion: '',
  });
  
  // Pagination state
  const [page, setPage] = useState(0);
  const [pageSize, setPageSize] = useState(5);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredData, setFilteredData] = useState<DiaFeriado[]>([]);
  const [paginatedData, setPaginatedData] = useState<DiaFeriado[]>([]);

  useEffect(() => {
    loadFeriados();
  }, []);

  // Filtrar datos cuando cambia el término de búsqueda
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredData(feriados);
      return;
    }

    const searchLower = searchTerm.toLowerCase();
    const filtered = feriados.filter((feriado) => {
      return (
        feriado.nombre.toLowerCase().includes(searchLower) ||
        feriado.fecha.includes(searchLower) ||
        (feriado.descripcion && feriado.descripcion.toLowerCase().includes(searchLower))
      );
    });
    setFilteredData(filtered);
    setPage(0); // Resetear a primera página cuando se busca
  }, [searchTerm, feriados]);

  // Calcular paginación local y actualizar totalPages
  useEffect(() => {
    const totalPagesCalc = Math.ceil(filteredData.length / pageSize);
    
    // Calcular qué items mostrar en la página actual
    const startIndex = page * pageSize;
    const endIndex = startIndex + pageSize;
    setPaginatedData(filteredData.slice(startIndex, endIndex));
    
    // Si la página actual ya no existe después de filtrar, volver a la primera
    if (page >= totalPagesCalc && totalPagesCalc > 0) {
      setPage(0);
    }
  }, [filteredData, page, pageSize]);

  const loadFeriados = async () => {
    try {
      setIsLoading(true);
      const data = await diasFeriadosService.getAllUnpaginated();
      const feriadosArray = (data as any).content || data;
      console.log('📅 Fechas recibidas del backend:', feriadosArray.map((f: any) => ({ nombre: f.nombre, fecha: f.fecha })));
      setFeriados(Array.isArray(feriadosArray) ? feriadosArray : []);
    } catch (error) {
      console.error('Error cargando días feriados:', error);
      setFeriados([]);
    } finally {
      setIsLoading(false);
    }
  };

  const handleCreate = () => {
    setEditingFeriado(null);
    setFormData({
      nombre: '',
      fecha: '',
      descripcion: '',
    });
    setIsModalOpen(true);
  };

  const handleEdit = (feriado: DiaFeriado) => {
    setEditingFeriado(feriado);
    setFormData({
      nombre: feriado.nombre,
      fecha: feriado.fecha,
      descripcion: feriado.descripcion || '',
    });
    setIsModalOpen(true);
  };

  const handleDelete = (feriado: DiaFeriado) => {
    setFeriadoToDelete(feriado);
    setIsDeleteDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!feriadoToDelete?.id) return;

    try {
      await diasFeriadosService.delete(feriadoToDelete.id);
      await loadFeriados();
      setIsDeleteDialogOpen(false);
      setFeriadoToDelete(null);
    } catch (error: any) {
      console.error('Error eliminando feriado:', error);
      alert(error.message || 'Error al eliminar el día feriado');
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();

    // Validaciones
    if (!formData.nombre.trim()) {
      alert('El nombre del feriado es obligatorio');
      return;
    }

    if (!formData.fecha) {
      alert('La fecha del feriado es obligatoria');
      return;
    }

    // Validar que la fecha sea futura
    const hoy = new Date();
    const hoyStr = `${hoy.getFullYear()}-${String(hoy.getMonth() + 1).padStart(2, '0')}-${String(hoy.getDate()).padStart(2, '0')}`;
    
    if (formData.fecha <= hoyStr) {
      alert('Solo se pueden registrar feriados con fechas futuras');
      return;
    }

    try {
      console.log('📅 Enviando fecha al backend:', formData.fecha);
      
      if (editingFeriado?.id) {
        // Actualizar
        await diasFeriadosService.update(editingFeriado.id, {
          nombre: formData.nombre,
          fecha: formData.fecha,
          descripcion: formData.descripcion,
        });
      } else {
        // Crear
        await diasFeriadosService.create({
          nombre: formData.nombre,
          fecha: formData.fecha,
          descripcion: formData.descripcion,
        });
      }

      await loadFeriados();
      setIsModalOpen(false);
      resetForm();
    } catch (error: any) {
      console.error('Error guardando feriado:', error);
      alert(error.message || 'Error al guardar el día feriado');
    }
  };

  const resetForm = () => {
    setFormData({
      nombre: '',
      fecha: '',
      descripcion: '',
    });
    setEditingFeriado(null);
  };

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0); // Resetear a la primera página
  };

  const formatDate = (dateString: string) => {
    if (!dateString) return '';
    const [year, month, day] = dateString.split('-');
    return `${day}/${month}/${year}`;
  };

  const totalPages = Math.ceil(filteredData.length / pageSize);

  return (
    <div className="space-y-6">
      <div className="flex justify-between items-center">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Días Feriados</h2>
          <p className="text-muted-foreground">
            Gestiona los días feriados nacionales del año
          </p>
        </div>
        <Button onClick={handleCreate} className="gap-2">
          <Plus className="h-4 w-4" />
          Nuevo Feriado
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
                  Nombre del Feriado
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Fecha
                </th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Descripción
                </th>
                <th className="px-4 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filteredData.length === 0 ? (
                <tr>
                  <td
                    colSpan={4}
                    className="px-4 py-8 text-center text-muted-foreground"
                  >
                    {searchTerm ? 'No se encontraron resultados' : 'No hay feriados registrados'}
                  </td>
                </tr>
              ) : (
                paginatedData.map((feriado) => (
                  <tr
                    key={feriado.id}
                    className="hover:bg-muted/50 transition-colors"
                  >
                    <td className="px-4 py-3 text-sm">{feriado.nombre}</td>
                    <td className="px-4 py-3 text-sm">{formatDate(feriado.fecha)}</td>
                    <td className="px-4 py-3 text-sm">{feriado.descripcion || '-'}</td>
                    <td className="px-4 py-3 text-sm text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleEdit(feriado)}
                          className="h-8 w-8 p-0 hover:bg-muted"
                          title="Editar"
                        >
                          <Pencil className="w-4 h-4" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => handleDelete(feriado)}
                          className="h-8 w-8 p-0 hover:bg-destructive/10 text-destructive"
                          title="Eliminar"
                        >
                          <Trash2 className="w-4 h-4" />
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
              {/* Agregar filas vacías para mantener altura constante */}
              {paginatedData.length > 0 && Array.from({ length: Math.max(0, pageSize - paginatedData.length) }).map((_, index) => (
                <tr key={`empty-${index}`} className="hover:bg-muted/50 transition-colors" style={{ height: '56px' }}>
                  <td className="px-4 py-3 text-sm">&nbsp;</td>
                  <td className="px-4 py-3 text-sm">&nbsp;</td>
                  <td className="px-4 py-3 text-sm">&nbsp;</td>
                  <td className="px-4 py-3 text-sm">&nbsp;</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </Card>

      {totalPages > 1 && (
        <Pagination>
          <PaginationContent>
            <PaginationItem>
              <PaginationPrevious 
                onClick={() => setPage(Math.max(0, page - 1))}
                className={page === 0 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
              />
            </PaginationItem>
            
            {Array.from({ length: totalPages }, (_, i) => i).map((pageNum) => {
              // Mostrar solo algunas páginas alrededor de la actual
              if (
                pageNum === 0 || // Primera página
                pageNum === totalPages - 1 || // Última página
                (pageNum >= page - 1 && pageNum <= page + 1) // Páginas cercanas a la actual
              ) {
                return (
                  <PaginationItem key={pageNum}>
                    <PaginationLink
                      onClick={() => setPage(pageNum)}
                      isActive={pageNum === page}
                      className="cursor-pointer"
                    >
                      {pageNum + 1}
                    </PaginationLink>
                  </PaginationItem>
                );
              } else if (pageNum === page - 2 || pageNum === page + 2) {
                return (
                  <PaginationItem key={pageNum}>
                    <span className="flex h-9 w-9 items-center justify-center">...</span>
                  </PaginationItem>
                );
              }
              return null;
            })}
            
            <PaginationItem>
              <PaginationNext 
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                className={page === totalPages - 1 ? 'pointer-events-none opacity-50' : 'cursor-pointer'}
              />
            </PaginationItem>
          </PaginationContent>
        </Pagination>
      )}

      <div className="text-sm text-muted-foreground text-center">
        {searchTerm.trim() 
          ? `Mostrando ${filteredData.length} resultado${filteredData.length !== 1 ? 's' : ''} de ${feriados.length} registros`
          : `Página ${page + 1} de ${totalPages} • Total: ${feriados.length} registros`
        }
      </div>

      {/* Modal Crear/Editar */}
      <Modal
        isOpen={isModalOpen}
        onClose={() => {
          setIsModalOpen(false);
          resetForm();
        }}
        title={editingFeriado ? 'Editar Día Feriado' : 'Nuevo Día Feriado'}
      >
        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <Label htmlFor="nombre">Nombre del Feriado *</Label>
            <Input
              id="nombre"
              value={formData.nombre}
              onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
              placeholder="Ej: Día de la Independencia"
              required
            />
          </div>

          <div>
            <Label htmlFor="fecha">Fecha *</Label>
            <DatePicker
              value={formData.fecha}
              onChange={(date) => setFormData({ ...formData, fecha: date })}
              placeholder="Seleccionar fecha del feriado"
            />
            <p className="text-sm text-muted-foreground mt-1">
              Solo se pueden registrar feriados con fechas futuras
            </p>
          </div>

          <div>
            <Label htmlFor="descripcion">Descripción (Opcional)</Label>
            <Textarea
              id="descripcion"
              value={formData.descripcion}
              onChange={(e) => setFormData({ ...formData, descripcion: e.target.value })}
              placeholder="Descripción adicional del feriado..."
              rows={3}
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
              {editingFeriado ? 'Actualizar' : 'Crear'}
            </Button>
          </div>
        </form>
      </Modal>

      {/* Diálogo de Confirmación de Eliminación */}
      <ConfirmDialog
        isOpen={isDeleteDialogOpen}
        onClose={() => {
          setIsDeleteDialogOpen(false);
          setFeriadoToDelete(null);
        }}
        onConfirm={confirmDelete}
        title="Eliminar Día Feriado"
        message={`¿Está seguro que desea eliminar el feriado "${feriadoToDelete?.nombre}"? Esta acción no se puede deshacer.`}
        confirmText="Eliminar"
        cancelText="Cancelar"
      />
    </div>
  );
}
