import { Button } from './ui/button';
import { authService } from '../services/authService';
import { useNavigate } from 'react-router-dom';
import { isDarkTheme, useTheme } from '../hooks/useTheme';
import { ThemeMenuButton } from './ThemeMenuButton';

interface DashboardHeaderProps {
  username: string;
}

export function DashboardHeader({ username }: DashboardHeaderProps) {
  const navigate = useNavigate();
  const { theme } = useTheme();
  const darkModeEnabled = isDarkTheme(theme);

  const handleLogout = () => {
    authService.logout();
    navigate('/login');
  };

  return (
    <header className="bg-card border-b border-border px-6 py-4 flex items-center justify-between">
      <div className="flex items-center gap-4">
        <img 
          src={darkModeEnabled ? '/logo_invertido.png' : '/logo.png'} 
          alt="Logo Sastrería Gerson Andre" 
          className="h-10 w-auto"
        />
        <h2 className="text-lg font-semibold text-foreground">Panel de Control</h2>
      </div>
      
      <div className="flex items-center gap-3">
        <span className="text-sm font-medium text-foreground">{username}</span>

        <ThemeMenuButton buttonClassName="h-8 w-8" />
        
        <Button
          variant="ghost"
          size="icon"
          onClick={handleLogout}
          className="h-8 w-8"
          title="Cerrar sesión"
        >
          <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M17 16l4-4m0 0l-4-4m4 4H7m6 4v1a3 3 0 01-3 3H6a3 3 0 01-3-3V7a3 3 0 013-3h4a3 3 0 013 3v1" />
          </svg>
        </Button>
        
        <div className="w-8 h-8 rounded-full bg-primary flex items-center justify-center text-primary-foreground font-medium text-sm">
          {username.charAt(0).toUpperCase()}
        </div>
      </div>
    </header>
  );
}
