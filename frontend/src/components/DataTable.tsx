import { useState, useEffect } from 'react';
import { Button } from '@/components/ui/button';
import { Card } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import {
  Pagination,
  PaginationContent,
  PaginationItem,
  PaginationLink,
  PaginationNext,
  PaginationPrevious,
} from '@/components/ui/pagination';
import type { ApiService } from '@/services/apiService';

export interface Column<T> {
  key: keyof T | string;
  label: string;
  render?: (value: any, item: T) => React.ReactNode;
}

interface DataTableProps<T> {
  service: ApiService<T>;
  columns: Column<T>[];
  title: string;
  onEdit: (item: T) => void;
  onCreate: () => void;
  refreshTrigger?: number;
  customActions?: (item: T) => React.ReactNode;
  hideHeader?: boolean;
}

export function DataTable<T extends { id?: number | string }>({
  service,
  columns,
  title,
  onEdit,
  onCreate,
  refreshTrigger = 0,
  customActions,
  hideHeader = false,
}: DataTableProps<T>) {
  const [data, setData] = useState<T[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isConfirmOpen, setIsConfirmOpen] = useState(false);
  const [itemToDelete, setItemToDelete] = useState<number | string | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);
  const [pageSize, setPageSize] = useState(5);
  const [searchTerm, setSearchTerm] = useState('');
  const [filteredData, setFilteredData] = useState<T[]>([]);
  const [paginatedData, setPaginatedData] = useState<T[]>([]);

  const loadData = async () => {
    try {
      setLoading(true);
      setError(null);
      // Cargar TODOS los datos una sola vez
      const allData = await service.getAllUnpaginated();
      setData(allData);
      setTotalElements(allData.length);
    } catch (err) {
      setError(err instanceof Error ? err.message : 'Error al cargar los datos');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    loadData();
  }, [refreshTrigger]);

  // Filtrar datos cuando cambia el término de búsqueda
  useEffect(() => {
    if (!searchTerm.trim()) {
      setFilteredData(data);
      return;
    }

    const searchLower = searchTerm.toLowerCase();
    const filtered = data.filter((item) => {
      return columns.some((column) => {
        const value = getCellValue(item, column);
        return String(value || '').toLowerCase().includes(searchLower);
      });
    });
    setFilteredData(filtered);
    setPage(0); // Resetear a primera página cuando se busca
  }, [searchTerm, data]);

  // Calcular paginación local y actualizar totalPages
  useEffect(() => {
    const totalPagesCalc = Math.ceil(filteredData.length / pageSize);
    setTotalPages(totalPagesCalc);
    
    // Calcular qué items mostrar en la página actual
    const startIndex = page * pageSize;
    const endIndex = startIndex + pageSize;
    setPaginatedData(filteredData.slice(startIndex, endIndex));
    
    // Si la página actual ya no existe después de filtrar, volver a la primera
    if (page >= totalPagesCalc && totalPagesCalc > 0) {
      setPage(0);
    }
  }, [filteredData, page, pageSize]);

  const handlePageSizeChange = (newSize: string) => {
    setPageSize(Number(newSize));
    setPage(0); // Resetear a la primera página
  };

  const handleDelete = async (id: number | string) => {
    console.log('Intentando eliminar ID:', id);
    setItemToDelete(id);
    setIsConfirmOpen(true);
  };

  const confirmDelete = async () => {
    if (!itemToDelete) return;

    try {
      setIsDeleting(true);
      console.log('Llamando a service.delete con ID:', itemToDelete);
      await service.delete(itemToDelete);
      console.log('Eliminación exitosa, recargando datos');
      setIsConfirmOpen(false);
      setItemToDelete(null);
      loadData();
    } catch (err) {
      console.error('Error al eliminar:', err);
      alert(err instanceof Error ? err.message : 'Error al eliminar el registro');
    } finally {
      setIsDeleting(false);
    }
  };

  const cancelDelete = () => {
    console.log('Eliminación cancelada por el usuario');
    setIsConfirmOpen(false);
    setItemToDelete(null);
  };

  const getCellValue = (item: T, column: Column<T>) => {
    const keys = String(column.key).split('.');
    let value: any = item;
    
    for (const key of keys) {
      value = value?.[key];
    }
    
    return value;
  };

  if (error) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="text-lg text-destructive">Error: {error}</div>
      </div>
    );
  }

  return (
    <div className="space-y-4">
      {!hideHeader && (
        <div className="flex items-center justify-between">
          <div>
            <h2 className="text-2xl font-bold text-foreground">{title}</h2>
            <p className="text-sm text-muted-foreground mt-1">
              Total de registros: {totalElements}
            </p>
          </div>
          <Button onClick={onCreate} className="gap-2">
          <svg
            className="w-4 h-4"
            fill="none"
            stroke="currentColor"
            viewBox="0 0 24 24"
          >
            <path
              strokeLinecap="round"
              strokeLinejoin="round"
              strokeWidth={2}
              d="M12 4v16m8-8H4"
            />
          </svg>
          Nuevo Registro
        </Button>
      </div>
      )}

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
        {loading && (
          <div className="absolute inset-0 bg-background/50 flex items-center justify-center z-10 rounded-lg">
            <div className="text-lg text-muted-foreground">Cargando...</div>
          </div>
        )}
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-muted">
              <tr>
                {columns.map((column, index) => (
                  <th
                    key={index}
                    className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wider"
                  >
                    {column.label}
                  </th>
                ))}
                <th className="px-4 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wider">
                  Acciones
                </th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {filteredData.length === 0 ? (
                <tr>
                  <td
                    colSpan={columns.length + 1}
                    className="px-4 py-8 text-center text-muted-foreground"
                  >
                    {searchTerm ? 'No se encontraron resultados' : 'No hay registros para mostrar'}
                  </td>
                </tr>
              ) : (
                paginatedData.map((item, rowIndex) => (
                  <tr
                    key={item.id || rowIndex}
                    className="hover:bg-muted/50 transition-colors"
                  >
                    {columns.map((column, colIndex) => (
                      <td key={colIndex} className="px-4 py-3 text-sm">
                        {column.render
                          ? column.render(getCellValue(item, column), item)
                          : String(getCellValue(item, column) ?? '')}
                      </td>
                    ))}
                    <td className="px-4 py-3 text-sm text-right">
                      <div className="flex items-center justify-end gap-1">
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            console.log('Botón Editar clickeado, item:', item);
                            onEdit(item);
                          }}
                          className="h-8 w-8 p-0 hover:bg-muted"
                          title="Editar"
                        >
                          <svg
                            className="w-4 h-4"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"
                            />
                          </svg>
                        </Button>
                        {customActions && customActions(item)}
                        <Button
                          variant="ghost"
                          size="sm"
                          onClick={() => {
                            console.log('Botón Eliminar clickeado, item:', item, 'item.id:', item.id);
                            if (item.id) {
                              handleDelete(item.id);
                            } else {
                              console.error('Item no tiene ID!');
                            }
                          }}
                          className="h-8 w-8 p-0 hover:bg-destructive/10 text-destructive"
                          title="Eliminar"
                        >
                          <svg
                            className="w-4 h-4"
                            fill="none"
                            stroke="currentColor"
                            viewBox="0 0 24 24"
                          >
                            <path
                              strokeLinecap="round"
                              strokeLinejoin="round"
                              strokeWidth={2}
                              d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"
                            />
                          </svg>
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
              {/* Agregar filas vacías para mantener altura constante */}
              {paginatedData.length > 0 && Array.from({ length: Math.max(0, pageSize - paginatedData.length) }).map((_, index) => (
                <tr key={`empty-${index}`} className="hover:bg-muted/50 transition-colors" style={{ height: '56px' }}>
                  {columns.map((_, colIndex) => (
                    <td key={colIndex} className="px-4 py-3 text-sm">&nbsp;</td>
                  ))}
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
          ? `Mostrando ${filteredData.length} resultado${filteredData.length !== 1 ? 's' : ''} de ${totalElements} registros`
          : `Página ${page + 1} de ${totalPages} • Total: ${totalElements} registros`
        }
      </div>

      <ConfirmDialog
        isOpen={isConfirmOpen}
        onClose={cancelDelete}
        onConfirm={confirmDelete}
        title="Confirmar Eliminación"
        message={
          <>
            ¿Está seguro de que desea eliminar este registro?
            <br />
            <span className="text-sm text-muted-foreground mt-2 block">
              Esta acción no se puede deshacer.
            </span>
          </>
        }
        confirmText="Eliminar"
        cancelText="Cancelar"
        isLoading={isDeleting}
      />
    </div>
  );
}
