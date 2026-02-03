import { Modal } from './Modal';
import { Button } from './ui/button';

interface JornadaDiariaPreview {
  fecha: string;
  horaEntrada: string;
  horaSalida: string;
  horasRegulares: number;
  horasExtra: number;
  observaciones: string;
  nombreCompleto: string;
}

interface ConfirmClockOutModalProps {
  isOpen: boolean;
  onClose: () => void;
  onConfirm: () => void;
  preview: JornadaDiariaPreview | null;
  isLoading?: boolean;
}

export function ConfirmClockOutModal({
  isOpen,
  onClose,
  onConfirm,
  preview,
  isLoading = false,
}: ConfirmClockOutModalProps) {
  if (!preview) return null;

  const formatDate = (dateString: string) => {
    const [year, month, day] = dateString.split('-');
    return `${day}/${month}/${year}`;
  };

  const formatTime = (timeString: string) => {
    if (!timeString) return '';
    const parts = timeString.split(':');
    return `${parts[0]}:${parts[1]}`;
  };

  return (
    <Modal
      isOpen={isOpen}
      onClose={onClose}
      title="Confirmar Salida"
    >
      <div className="space-y-4">
        <div className="bg-muted/50 rounded-lg p-4 space-y-3">
          <div className="text-center pb-2 border-b">
            <h3 className="font-semibold text-lg">{preview.nombreCompleto}</h3>
            <p className="text-sm text-muted-foreground">{formatDate(preview.fecha)}</p>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <p className="text-xs text-muted-foreground uppercase">Hora de Entrada</p>
              <p className="text-lg font-semibold">{formatTime(preview.horaEntrada)}</p>
            </div>
            <div>
              <p className="text-xs text-muted-foreground uppercase">Hora de Salida</p>
              <p className="text-lg font-semibold">{formatTime(preview.horaSalida)}</p>
            </div>
          </div>

          <div className="pt-3 border-t">
            <div className="grid grid-cols-2 gap-4">
              <div className="text-center p-3 bg-background rounded-md">
                <p className="text-xs text-muted-foreground uppercase mb-1">Horas Regulares</p>
                <p className="text-2xl font-bold text-primary">{preview.horasRegulares.toFixed(2)}</p>
                <p className="text-xs text-muted-foreground">horas</p>
              </div>
              <div className="text-center p-3 bg-background rounded-md">
                <p className="text-xs text-muted-foreground uppercase mb-1">Horas Extra</p>
                <p className={`text-2xl font-bold ${preview.horasExtra > 0 ? 'text-green-600' : 'text-muted-foreground'}`}>
                  {preview.horasExtra.toFixed(2)}
                </p>
                <p className="text-xs text-muted-foreground">horas</p>
              </div>
            </div>
          </div>

          {preview.observaciones && (
            <div className="pt-2 border-t">
              <p className="text-xs text-muted-foreground uppercase mb-1">Observaciones</p>
              <p className="text-sm">{preview.observaciones}</p>
            </div>
          )}
        </div>

        <div className="bg-blue-50 dark:bg-blue-950/20 border border-blue-200 dark:border-blue-900 rounded-lg p-3">
          <p className="text-sm text-blue-900 dark:text-blue-100">
            ℹ️ Al confirmar, se guardará este registro de jornada diaria y se marcará tu salida.
          </p>
        </div>

        <div className="flex justify-end gap-2 pt-2">
          <Button
            type="button"
            variant="outline"
            onClick={onClose}
            disabled={isLoading}
          >
            Cancelar
          </Button>
          <Button
            type="button"
            onClick={onConfirm}
            disabled={isLoading}
          >
            {isLoading ? 'Confirmando...' : 'Confirmar Salida'}
          </Button>
        </div>
      </div>
    </Modal>
  );
}
