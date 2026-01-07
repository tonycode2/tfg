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
}

export function SimpleDataTable<T extends { id: number | string }>({
  data,
  columns,
  onEdit,
  onDelete,
  customActions,
}: SimpleDataTableProps<T>) {
  const getCellValue = (item: T, column: Column<T>) => {
    const keys = String(column.key).split('.');
    let value: any = item;
    
    for (const key of keys) {
      value = value?.[key];
    }
    
    return value;
  };

  return (
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead className="bg-muted">
          <tr>
            {columns.map((column) => (
              <th
                key={String(column.key)}
                className="px-4 py-3 text-left text-sm font-medium text-muted-foreground"
              >
                {column.label}
              </th>
            ))}
            <th className="px-4 py-3 text-right text-sm font-medium text-muted-foreground">
              Acciones
            </th>
          </tr>
        </thead>
        <tbody className="divide-y divide-border">
          {data.length === 0 ? (
            <tr>
              <td
                colSpan={columns.length + 1}
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
                    className="px-4 py-3 text-sm text-foreground"
                  >
                    {column.render
                      ? column.render(getCellValue(item, column), item)
                      : String(getCellValue(item, column) ?? '')}
                  </td>
                ))}
                <td className="px-4 py-3 text-right space-x-1">
                  {customActions && customActions(item)}
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
              </tr>
            ))
          )}
        </tbody>
      </table>
    </div>
  );
}
