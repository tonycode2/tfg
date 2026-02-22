import { createContext, useContext, useEffect, useState } from 'react';

export type Theme = 'light' | 'dark' | 'colorblind-protanopia' | 'high-contrast';

export const THEMES: Array<{ value: Theme; label: string }> = [
  { value: 'light', label: 'Claro' },
  { value: 'dark', label: 'Oscuro' },
  { value: 'colorblind-protanopia', label: 'Daltónico (Protanopia)' },
  { value: 'high-contrast', label: 'Alto contraste' },
];

const THEME_CLASS_MAP: Record<Theme, string> = {
  light: 'light',
  dark: 'dark',
  'colorblind-protanopia': 'theme-colorblind-protanopia',
  'high-contrast': 'theme-high-contrast',
};

export const isDarkTheme = (theme: Theme) =>
  theme === 'dark' || theme === 'high-contrast';

const isValidTheme = (theme: string | null): theme is Theme =>
  theme !== null && THEMES.some((option) => option.value === theme);

type ThemeContextType = {
  theme: Theme;
  setTheme: (theme: Theme) => void;
  toggleTheme: () => void;
  themes: Array<{ value: Theme; label: string }>;
};

const ThemeContext = createContext<ThemeContextType | undefined>(undefined);

export function ThemeProvider({ children }: { children: React.ReactNode }) {
  const [theme, setTheme] = useState<Theme>(() => {
    const storedTheme = localStorage.getItem('theme');
    return isValidTheme(storedTheme) ? storedTheme : 'light';
  });

  useEffect(() => {
    const root = window.document.documentElement;
    root.classList.remove(...Object.values(THEME_CLASS_MAP));
    root.classList.add(THEME_CLASS_MAP[theme]);
    root.dataset.theme = theme;
    localStorage.setItem('theme', theme);
  }, [theme]);

  const toggleTheme = () => {
    setTheme((prev) => (isDarkTheme(prev) ? 'light' : 'dark'));
  };

  return (
    <ThemeContext.Provider value={{ theme, setTheme, toggleTheme, themes: THEMES }}>
      {children}
    </ThemeContext.Provider>
  );
}

export function useTheme() {
  const context = useContext(ThemeContext);
  if (context === undefined) {
    throw new Error('useTheme must be used within a ThemeProvider');
  }
  return context;
}
