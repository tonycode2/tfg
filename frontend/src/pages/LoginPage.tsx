import { useState, useEffect, useCallback } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Card, CardContent, CardDescription, CardFooter, CardHeader, CardTitle } from '@/components/ui/card';
import { authService } from '@/services/authService';
import { useTheme } from '@/hooks/useTheme';
import { useForm } from '@/hooks/useForm';

interface LoginFormData {
  username: string;
  password: string;
}

export default function LoginPage() {
  const [successMessage, setSuccessMessage] = useState('');
  const [error, setError] = useState('');
  const navigate = useNavigate();
  const location = useLocation();
  const { theme, toggleTheme } = useTheme();

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
      <Button
        variant="ghost"
        size="icon"
        onClick={toggleTheme}
        className="absolute top-4 right-4 h-9 w-9"
        title="Cambiar tema"
        aria-label="Cambiar tema"
      >
        {theme === 'light' ? (
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
          </svg>
        ) : (
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
            <circle cx="12" cy="12" r="4" strokeWidth={2} />
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41m11.32 11.32l1.41 1.41M2 12h2m16 0h2M6.34 17.66l-1.41 1.41M17.66 6.34l1.41-1.41" />
          </svg>
        )}
      </Button>
      <Card className="w-full max-w-md">
        <CardHeader className="space-y-1">
          <div className="flex justify-center mb-4">
            <img 
              src={theme === 'light' ? '/logo.png' : '/logo_invertido.png'} 
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
