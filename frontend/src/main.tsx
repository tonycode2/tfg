import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ThemeProvider } from './hooks/useTheme'
import { ToastProvider } from './components/ui/Toast'

// Register Spanish locale for react-datepicker
import { registerLocale, setDefaultLocale } from 'react-datepicker'
import { es } from 'date-fns/locale'
registerLocale('es', es)
setDefaultLocale('es')

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <ToastProvider>
        <App />
      </ToastProvider>
    </ThemeProvider>
  </StrictMode>,
)

// Ocultar el loader después de que React monte
setTimeout(() => {
  const loader = document.getElementById('app-loader');
  if (loader) {
    loader.classList.add('hide');
    setTimeout(() => loader.remove(), 300);
  }
}, 100);
