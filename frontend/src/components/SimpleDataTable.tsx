import { Button } from '@/components/ui/button';

export interface Column<T> {
  key: keyof T | string;
  label: string;
  render?: (value: any, item: T) => React.ReactNode;
}

interface SimpleDataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  onEdit?: (item: T) => void;
  onDelete?: (id: number | string) => void;
  customActions?: (item: T) => React.ReactNode;
  hideHorizontalScrollbar?: boolean;
}

export function SimpleDataTable<T extends { id: number | string }>({
  data,
  columns,
  onEdit,
  onDelete,
  customActions,
  hideHorizontalScrollbar = false,
}: SimpleDataTableProps<T>) {
  const hasActions = Boolean(onEdit || onDelete || customActions);

  const getCellValue = (item: T, column: Column<T>) => {
    const keys = String(column.key).split('.');
    let value: any = item;
    
    for (const key of keys) {
      value = value?.[key];
    }
    
    return value;
  };

  return (
    <div
      className={`overflow-x-auto pb-4 ${hideHorizontalScrollbar ? '[scrollbar-width:none] [&::-webkit-scrollbar]:hidden' : ''}`}
    >
      <table className="w-full min-w-full table-auto">
        <thead className="bg-muted">
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                className="px-4 py-4 text-left text-sm font-medium text-muted-foreground align-middle"
                title={column.label}
              >
                <span className="inline-block align-middle truncate max-w-[180px]">{column.label}</span>
              </th>
            ))}
            {hasActions && (
              <th className="px-4 py-4 text-right text-sm font-medium text-muted-foreground align-middle" title="Acciones">
                <span className="inline-block align-middle truncate max-w-[180px]">Acciones</span>
              </th>
            )}
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length + (hasActions ? 1 : 0)}
                className="px-4 py-8 text-center text-muted-foreground"
              >
                No hay registros para mostrar
              </td>
            </tr>
          ) : (
            data.map((item) => (
              <tr key={item.id} className="hover:bg-muted/50 transition-colors">
                {columns.map((column) => (
                  <td
                    key={`${item.id}-${String(column.key)}`}
                    className="px-4 py-3 text-sm text-foreground align-middle whitespace-nowrap"
                  >
                    <div className="max-w-[280px] truncate">
                      {column.render
                        ? column.render(getCellValue(item, column), item)
                        : String(getCellValue(item, column) ?? '')}
                    </div>
                  </td>
                ))}
                {hasActions && (
                  <td className="px-4 py-3 text-right align-middle">
                    <div className="inline-flex items-center justify-end gap-2">
                      {customActions && customActions(item)}
                    </div>
                    {onEdit && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onEdit(item)}
                        className="h-8 px-2"
                        title="Editar"
                      >
                        ✏️
                      </Button>
                    )}
                    {onDelete && (
                      <Button
                        variant="ghost"
                        size="sm"
                        onClick={() => onDelete(item.id)}
                        className="h-8 px-2 text-destructive hover:text-destructive"
                        title="Eliminar"
                      >
                        🗑️
                      </Button>
                    )}
                  </td>
                )}
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
