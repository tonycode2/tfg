import { useState, useEffect, useCallback, useMemo } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { authService } from '@/services/authService';
import { useForm } from '@/hooks/useForm';

interface ChangePasswordFormData {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
}

// Password validation function extracted for reusability
const validatePassword = (password: string): string | null => {
  if (password.length < 8) {
    return 'La contraseña debe tener al menos 8 caracteres';
  }
  if (!/[A-Z]/.test(password)) {
    return 'La contraseña debe contener al menos una letra mayúscula';
  }
  if (!/[a-z]/.test(password)) {
    return 'La contraseña debe contener al menos una letra minúscula';
  }
  if (!/[0-9]/.test(password)) {
    return 'La contraseña debe contener al menos un número';
  }
  if (!/[!@#$%&*]/.test(password)) {
    return 'La contraseña debe contener al menos un carácter especial (!@#$%&*)';
  }
  return null;
};

export default function ChangePasswordPage() {
  const [error, setError] = useState('');
  const navigate = useNavigate();

  useEffect(() => {
    document.title = 'Cambiar Contraseña - Sistema de RH';
  }, []);

  // Memoized validation function
  const validate = useCallback((values: ChangePasswordFormData): Record<string, string> => {
    const errors: Record<string, string> = {};

    if (values.newPassword !== values.confirmPassword) {
      errors.confirmPassword = 'Las contraseñas no coinciden';
    }

    const passwordError = validatePassword(values.newPassword);
    if (passwordError) {
      errors.newPassword = passwordError;
    }

    if (values.currentPassword === values.newPassword) {
      errors.newPassword = 'La nueva contraseña debe ser diferente a la actual';
    }

    return errors;
  }, []);

  const handlePasswordChange = useCallback(async (values: ChangePasswordFormData) => {
    setError('');

    try {
      await authService.changePassword({
        currentPassword: values.currentPassword,
        newPassword: values.newPassword
      });
      
      // Logout and redirect to login
      authService.removeToken();
      navigate('/login', { 
        state: { message: 'Contraseña actualizada. Por favor inicie sesión nuevamente.' },
        replace: true
      });
    } catch (err: any) {
      setError(err.message || 'Error al cambiar la contraseña');
      throw err; // Re-throw to let useForm handle the submission state
    }
  }, [navigate]);

  const { values, errors, handleChange, handleSubmit, isSubmitting } = useForm<ChangePasswordFormData>({
    initialValues: {
      currentPassword: '',
      newPassword: '',
      confirmPassword: '',
    },
    validate,
    onSubmit: handlePasswordChange,
  });

  // Memoize password requirements UI
  const passwordRequirements = useMemo(() => (
    <div className="text-xs text-muted-foreground space-y-1">
      <p className="font-semibold">La contraseña debe contener:</p>
      <ul className="list-disc list-inside space-y-1">
        <li>Mínimo 8 caracteres</li>
        <li>Al menos una letra mayúscula</li>
        <li>Al menos una letra minúscula</li>
        <li>Al menos un número</li>
        <li>Al menos un carácter especial (!@#$%&*)</li>
      </ul>
    </div>
  ), []);

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4">
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <CardTitle className="text-2xl font-bold text-center">
            Cambiar Contraseña
          </CardTitle>
          <CardDescription className="text-center">
            Por motivos de seguridad, debe cambiar su contraseña antes de continuar
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="currentPassword">Contraseña Actual</Label>
              <Input
                id="currentPassword"
                name="currentPassword"
                type="password"
                placeholder="Ingrese su contraseña actual"
                value={values.currentPassword}
                onChange={handleChange}
                disabled={isSubmitting}
                required
                autoComplete="current-password"
              />
              {errors.currentPassword && (
                <p className="text-sm text-red-600">{errors.currentPassword}</p>
              )}
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="newPassword">Nueva Contraseña</Label>
              <Input
                id="newPassword"
                name="newPassword"
                type="password"
                placeholder="Ingrese su nueva contraseña"
                value={values.newPassword}
                onChange={handleChange}
                disabled={isSubmitting}
                required
                autoComplete="new-password"
              />
              {errors.newPassword && (
                <p className="text-sm text-red-600">{errors.newPassword}</p>
              )}
            </div>
            
            <div className="space-y-2">
              <Label htmlFor="confirmPassword">Confirmar Nueva Contraseña</Label>
              <Input
                id="confirmPassword"
                name="confirmPassword"
                type="password"
                placeholder="Confirme su nueva contraseña"
                value={values.confirmPassword}
                onChange={handleChange}
                disabled={isSubmitting}
                required
                autoComplete="new-password"
              />
              {errors.confirmPassword && (
                <p className="text-sm text-red-600">{errors.confirmPassword}</p>
              )}
            </div>

            {passwordRequirements}

            {error && (
              <div className="text-sm text-red-600 bg-red-50 border border-red-200 rounded p-3" role="alert">
                {error}
              </div>
            )}

            <Button 
              type="submit" 
              className="w-full"
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Cambiando contraseña...' : 'Cambiar Contraseña'}
            </Button>
          </CardContent>
        </form>
      </Card>
    </div>
  );
}
