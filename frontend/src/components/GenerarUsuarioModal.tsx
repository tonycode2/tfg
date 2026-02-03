import { useState } from 'react';
import { Modal } from './Modal';
import { Button } from './ui/button';
import { Label } from './ui/label';
import { empleadosService, type Role, type CredencialesResponse } from '@/services/apiService';
import { toast } from 'sonner';

interface GenerarUsuarioModalProps {
  isOpen: boolean;
  onClose: () => void;
  empleadoId: number | null;
  empleadoNombre?: string;
  onSuccess: () => void;
}

export function GenerarUsuarioModal({
  isOpen,
  onClose,
  empleadoId,
  empleadoNombre,
  onSuccess,
}: GenerarUsuarioModalProps) {
  const [selectedRole, setSelectedRole] = useState<Role>('EMPLEADO');
  const [isLoading, setIsLoading] = useState(false);
  const [credenciales, setCredenciales] = useState<CredencialesResponse | null>(null);
  const [showCredentials, setShowCredentials] = useState(false);

  if (!empleadoId) return null;

  const roles: { value: Role; label: string; description: string }[] = [
    {
      value: 'ADMIN',
      label: 'Administrador',
      description: 'Acceso completo al sistema',
    },
    {
      value: 'HR',
      label: 'Recursos Humanos',
      description: 'Gestión de empleados y usuarios',
    },
    {
      value: 'JEFE',
      label: 'Jefe/Supervisor',
      description: 'Supervisión de equipos y reportes',
    },
    {
      value: 'EMPLEADO',
      label: 'Empleado',
      description: 'Acceso básico al sistema',
    },
  ];

  const handleGenerar = async () => {
    try {
      setIsLoading(true);
      const response = await empleadosService.generarUsuario(empleadoId, selectedRole);
      setCredenciales(response);
      setShowCredentials(true);
    } catch (error) {
      console.error('Error al generar usuario:', error);
      toast.error(error instanceof Error ? error.message : 'Error al generar usuario');
    } finally {
      setIsLoading(false);
    }
  };

  const handleClose = () => {
    if (showCredentials) {
      onSuccess();
    }
    setCredenciales(null);
    setShowCredentials(false);
    setSelectedRole('EMPLEADO');
    onClose();
  };

  const copyToClipboard = (text: string, label: string) => {
    navigator.clipboard.writeText(text);
    toast.success(`${label} copiado al portapapeles`);
  };

  return (
    <Modal isOpen={isOpen} onClose={handleClose} title="Generar Usuario">
      <div className="space-y-6">
        {!showCredentials ? (
          <>
            <div>
              <p className="text-sm text-muted-foreground mb-4">
                Generando usuario para: <strong>{empleadoNombre}</strong>
              </p>
              <p className="text-sm text-muted-foreground mb-4">
                El sistema generará automáticamente un nombre de usuario y contraseña. Las
                credenciales serán enviadas al correo del empleado.
              </p>
            </div>

            <div>
              <Label htmlFor="role">Seleccionar Rol</Label>
              <div className="space-y-2 mt-2">
                {roles.map((role) => (
                  <label
                    key={role.value}
                    className={`flex items-start p-3 border rounded-lg cursor-pointer transition-colors ${
                      selectedRole === role.value
                        ? 'border-primary bg-primary/5'
                        : 'border-border hover:border-primary/50'
                    }`}
                  >
                    <input
                      type="radio"
                      name="role"
                      value={role.value}
                      checked={selectedRole === role.value}
                      onChange={(e) => setSelectedRole(e.target.value as Role)}
                      className="mt-1 mr-3"
                    />
                    <div className="flex-1">
                      <div className="font-medium">{role.label}</div>
                      <div className="text-sm text-muted-foreground">{role.description}</div>
                    </div>
                  </label>
                ))}
              </div>
            </div>

            <div className="flex gap-3 justify-end">
              <Button variant="outline" onClick={handleClose} disabled={isLoading}>
                Cancelar
              </Button>
              <Button onClick={handleGenerar} disabled={isLoading}>
                {isLoading ? 'Generando...' : 'Generar Usuario'}
              </Button>
            </div>
          </>
        ) : (
          <>
            <div className="bg-green-50 dark:bg-green-900/20 border border-green-200 dark:border-green-800 rounded-lg p-4">
              <h3 className="text-lg font-semibold text-green-900 dark:text-green-100 mb-2">
                ✅ Usuario creado exitosamente
              </h3>
              <p className="text-sm text-green-700 dark:text-green-300">
                Se ha enviado un correo a{' '}
                <strong>{credenciales?.correoEmpleado}</strong> con las credenciales de
                acceso.
              </p>
            </div>

            <div className="space-y-4">
              <div>
                <Label>Nombre de Usuario</Label>
                <div className="flex gap-2 mt-1">
                  <div className="flex-1 p-3 bg-muted rounded-md font-mono text-lg">
                    {credenciales?.username}
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => copyToClipboard(credenciales?.username || '', 'Usuario')}
                  >
                    📋
                  </Button>
                </div>
              </div>

              <div>
                <Label>Contraseña Temporal</Label>
                <div className="flex gap-2 mt-1">
                  <div className="flex-1 p-3 bg-muted rounded-md font-mono text-lg break-all">
                    {credenciales?.password}
                  </div>
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => copyToClipboard(credenciales?.password || '', 'Contraseña')}
                  >
                    📋
                  </Button>
                </div>
              </div>

              <div className="bg-amber-50 dark:bg-amber-900/20 border border-amber-200 dark:border-amber-800 rounded-lg p-4">
                <p className="text-sm text-amber-900 dark:text-amber-100">
                  <strong>⚠️ Importante:</strong> El usuario deberá cambiar esta contraseña
                  en su primer inicio de sesión.
                </p>
              </div>
            </div>

            <div className="flex justify-end">
              <Button onClick={handleClose}>Cerrar</Button>
            </div>
          </>
        )}
      </div>
    </Modal>
  );
}
