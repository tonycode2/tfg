import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import App from './App.tsx'
import { ThemeProvider } from './hooks/useTheme'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <ThemeProvider>
      <App />
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
