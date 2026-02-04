import { useEffect, useMemo, useState } from 'react';
import { Card } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { Label } from '@/components/ui/label';
import { Input } from '@/components/ui/input';
import { Modal } from '@/components/Modal';
import { ConfirmDialog } from '@/components/ConfirmDialog';
import { configuracionRentaService, type ConfiguracionRenta } from '@/services/apiService';
import { toast } from 'sonner';

interface FormData {
  montoMinimo: string;
  montoMaximo: string;
  porcentaje: string;
}

const formatCurrency = (value: number | undefined) => {
  if (value === undefined || value === null) return '₡0.00';
  return value.toLocaleString('es-CR', {
    style: 'currency',
    currency: 'CRC',
    minimumFractionDigits: 2,
  });
};

export function ConfiguracionRentaView() {
  const [configuraciones, setConfiguraciones] = useState<ConfiguracionRenta[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [isDeleting, setIsDeleting] = useState(false);
  const [editingItem, setEditingItem] = useState<ConfiguracionRenta | null>(null);
  const [itemToDelete, setItemToDelete] = useState<ConfiguracionRenta | null>(null);
  const [formData, setFormData] = useState<FormData>({
    montoMinimo: '',
    montoMaximo: '',
    porcentaje: '',
  });

  const sortedConfiguraciones = useMemo(
    () => [...configuraciones].sort((a, b) => a.montoMinimo - b.montoMinimo),
    [configuraciones]
  );

  useEffect(() => {
    loadConfiguraciones();
  }, []);

  const loadConfiguraciones = async () => {
    try {
      setIsLoading(true);
      const data = await configuracionRentaService.getAllUnpaginated();
      const payload = (data as any).content || data;
      setConfiguraciones(Array.isArray(payload) ? payload : []);
    } catch (error) {
      console.error('Error cargando configuraciones de renta', error);
      toast.error('No se pudo cargar la configuración de renta');
      setConfiguraciones([]);
    } finally {
      setIsLoading(false);
    }
  };

  const openCreateModal = () => {
    setEditingItem(null);
    setFormData({ montoMinimo: '', montoMaximo: '', porcentaje: '' });
    setIsModalOpen(true);
  };

  const openEditModal = (item: ConfiguracionRenta) => {
    setEditingItem(item);
    setFormData({
      montoMinimo: String(item.montoMinimo ?? ''),
      montoMaximo: String(item.montoMaximo ?? ''),
      porcentaje: String(item.porcentaje ?? ''),
    });
    setIsModalOpen(true);
  };

  const closeModal = () => {
    setIsModalOpen(false);
    setEditingItem(null);
    setFormData({ montoMinimo: '', montoMaximo: '', porcentaje: '' });
  };

  const handleSubmit = async () => {
    const montoMinimo = Number(formData.montoMinimo);
    const montoMaximo = Number(formData.montoMaximo);
    const porcentaje = Number(formData.porcentaje);

    if (Number.isNaN(montoMinimo) || Number.isNaN(montoMaximo) || Number.isNaN(porcentaje)) {
      toast.error('Completa todos los campos numéricos');
      return;
    }

    if (montoMinimo < 0 || montoMaximo <= 0) {
      toast.error('Los montos deben ser mayores o iguales a cero');
      return;
    }

    if (montoMaximo <= montoMinimo) {
      toast.error('El monto máximo debe ser mayor al mínimo');
      return;
    }

    if (porcentaje <= 0 || porcentaje > 100) {
      toast.error('El porcentaje debe estar entre 0 y 100');
      return;
    }

    try {
      if (editingItem?.id) {
        await configuracionRentaService.update(editingItem.id, {
          montoMinimo,
          montoMaximo,
          porcentaje,
        });
      } else {
        await configuracionRentaService.create({
          montoMinimo,
          montoMaximo,
          porcentaje,
        });
      }

      await loadConfiguraciones();
      closeModal();
      toast.success('Configuración guardada correctamente');
    } catch (error: any) {
      console.error('Error guardando la configuración de renta', error);
      toast.error(error?.message || 'No se pudo guardar la configuración');
    }
  };

  const handleDelete = (item: ConfiguracionRenta) => {
    setItemToDelete(item);
  };

  const confirmDelete = async () => {
    if (!itemToDelete?.id) return;
    try {
      setIsDeleting(true);
      await configuracionRentaService.delete(itemToDelete.id);
      await loadConfiguraciones();
      setItemToDelete(null);
      toast.success('Configuración eliminada');
    } catch (error: any) {
      console.error('Error eliminando configuración de renta', error);
      toast.error(error?.message || 'No se pudo eliminar');
    } finally {
      setIsDeleting(false);
    }
  };

  return (
    <div className="space-y-6">
      <div className="flex items-start justify-between gap-4 flex-wrap">
        <div>
          <h2 className="text-3xl font-bold tracking-tight">Configuración de Impuesto sobre la Renta</h2>
          <p className="text-muted-foreground">
            Define los tramos de renta que se aplicarán al cálculo de planillas
          </p>
        </div>
        <Button onClick={openCreateModal} className="whitespace-nowrap">
          Nuevo tramo
        </Button>
      </div>

      <Card className="p-6">
        <div className="flex justify-between items-center mb-4">
          <div>
            <h3 className="text-xl font-semibold">Tramos vigentes</h3>
            <p className="text-sm text-muted-foreground">Ordenados por monto mínimo</p>
          </div>
          {isLoading && <span className="text-sm text-muted-foreground">Cargando...</span>}
        </div>

        <div className="overflow-x-auto">
          <table className="w-full">
            <thead className="bg-muted">
              <tr>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wide">Monto mínimo</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wide">Monto máximo</th>
                <th className="px-4 py-3 text-left text-xs font-medium text-muted-foreground uppercase tracking-wide">Porcentaje</th>
                <th className="px-4 py-3 text-right text-xs font-medium text-muted-foreground uppercase tracking-wide">Acciones</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border">
              {sortedConfiguraciones.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-4 py-6 text-center text-muted-foreground">
                    {isLoading ? 'Cargando configuración...' : 'No hay tramos configurados'}
                  </td>
                </tr>
              ) : (
                sortedConfiguraciones.map((item) => (
                  <tr key={item.id ?? `${item.montoMinimo}-${item.montoMaximo}`} className="hover:bg-muted/50 transition-colors">
                    <td className="px-4 py-3 text-sm text-foreground">{formatCurrency(item.montoMinimo)}</td>
                    <td className="px-4 py-3 text-sm text-foreground">{formatCurrency(item.montoMaximo)}</td>
                    <td className="px-4 py-3 text-sm text-foreground">{item.porcentaje}%</td>
                    <td className="px-4 py-3 text-right text-sm">
                      <div className="inline-flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="sm"
                          onClick={() => openEditModal(item)}
                          className="h-8 px-3"
                        >
                          Editar
                        </Button>
                        <Button
                          variant="destructive"
                          size="sm"
                          onClick={() => handleDelete(item)}
                          className="h-8 px-3"
                        >
                          Eliminar
                        </Button>
                      </div>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </Card>

      <Modal
        isOpen={isModalOpen}
        onClose={closeModal}
        onSubmit={handleSubmit}
        submitLabel={editingItem ? 'Actualizar' : 'Guardar'}
        title={editingItem ? 'Editar tramo de renta' : 'Nuevo tramo de renta'}
      >
        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          <div className="space-y-2">
            <Label htmlFor="montoMinimo">Monto mínimo</Label>
            <Input
              id="montoMinimo"
              type="number"
              min="0"
              step="0.01"
              value={formData.montoMinimo}
              onChange={(e) => setFormData((prev) => ({ ...prev, montoMinimo: e.target.value }))}
              placeholder="0.00"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="montoMaximo">Monto máximo</Label>
            <Input
              id="montoMaximo"
              type="number"
              min="0"
              step="0.01"
              value={formData.montoMaximo}
              onChange={(e) => setFormData((prev) => ({ ...prev, montoMaximo: e.target.value }))}
              placeholder="0.00"
            />
          </div>
          <div className="space-y-2">
            <Label htmlFor="porcentaje">Porcentaje (%)</Label>
            <Input
              id="porcentaje"
              type="number"
              min="0"
              max="100"
              step="0.01"
              value={formData.porcentaje}
              onChange={(e) => setFormData((prev) => ({ ...prev, porcentaje: e.target.value }))}
              placeholder="0.00"
            />
          </div>
        </div>
        <p className="text-sm text-muted-foreground">
          Los tramos se aplican en el orden configurado y se usan para el cálculo automático del impuesto.
        </p>
      </Modal>

      <ConfirmDialog
        isOpen={Boolean(itemToDelete)}
        onClose={() => setItemToDelete(null)}
        onConfirm={confirmDelete}
        title="Eliminar configuración"
        message="¿Deseas eliminar este tramo de renta?"
        isLoading={isDeleting}
      />
    </div>
  );
}
