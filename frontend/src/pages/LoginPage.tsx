import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { authService } from '@/services/authService';
import { isDarkTheme, useTheme } from '@/hooks/useTheme';
import { useForm } from '@/hooks/useForm';
import { ThemeMenuButton } from '@/components/ThemeMenuButton';

interface LoginFormData {
  username: string;
  password: string;
}

export default function LoginPage() {
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const { theme } = useTheme();
  const darkModeEnabled = isDarkTheme(theme);

  useEffect(() => {
    document.title = 'Iniciar Sesión - Sistema de RH';
    // Check for success message from navigation state
    if (location.state?.message) {
      setSuccessMessage(location.state.message);
      // Clear the state
      window.history.replaceState({}, document.title);
    }
  }, [location]);

  const handleLogin = useCallback(async (values: LoginFormData) => {
    setError('');
    setSuccessMessage('');

    try {
      const response = await authService.login(values);
      authService.saveToken(response.token);
      
      // Check if password change is required
      if (response.passwordChangeRequired) {
        navigate('/change-password');
      } else {
        navigate('/dashboard');
      }
    } catch (err) {
      setError('Usuario o contraseña incorrectos');
      throw err; // Re-throw to let useForm handle the submission state
    }
  }, [navigate]);

  const { values, handleChange, handleSubmit, isSubmitting } = useForm<LoginFormData>({
    initialValues: {
      username: '',
      password: '',
    },
    onSubmit: handleLogin,
  });

  return (
    <div className="min-h-screen flex items-center justify-center bg-background px-4 relative">
      <div className="absolute top-4 right-4">
        <ThemeMenuButton buttonClassName="h-9 w-9" />
      </div>
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <div className="flex justify-center mb-4">
            <img 
              src={darkModeEnabled ? '/logo_invertido.png' : '/logo.png'} 
              alt="Logo Sastrería Gerson Andre" 
              className="h-20 w-auto"
            />
          </div>
          <CardTitle className="text-2xl font-bold text-center">
            Iniciar Sesión
          </CardTitle>
          <CardDescription className="text-center">
            Sistema de Gestión de RH - Sastrería Gerson Andre
          </CardDescription>
        </CardHeader>
        <form onSubmit={handleSubmit}>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="username">Usuario</Label>
              <Input
                id="username"
                name="username"
                type="text"
                placeholder="Ingrese su usuario"
                value={values.username}
                onChange={handleChange}
                disabled={isSubmitting}
                required
                autoComplete="username"
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="password">Contraseña</Label>
              <Input
                id="password"
                name="password"
                type="password"
                placeholder="Ingrese su contraseña"
                value={values.password}
                onChange={handleChange}
                disabled={isSubmitting}
                required
                autoComplete="current-password"
              />
            </div>
            
            {successMessage && (
              <div 
                className="text-sm text-green-600 bg-green-50 border border-green-200 rounded p-3 text-center"
                role="alert"
              >
                {successMessage}
              </div>
            )}
            
            {error && (
              <div className="text-sm text-red-600 text-center" role="alert">
                {error}
              </div>
            )}
          </CardContent>
          <CardFooter className="flex flex-col space-y-4">
            <Button 
              type="submit" 
              className="w-full" 
              disabled={isSubmitting}
            >
              {isSubmitting ? 'Iniciando sesión...' : 'Iniciar Sesión'}
            </Button>
          </CardFooter>
        </form>
      </Card>
    </div>
  );
}
