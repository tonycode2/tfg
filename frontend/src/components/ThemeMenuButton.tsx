import { useMemo } from 'react';
import { Button } from '@/components/ui/button';
import { Popover, PopoverContent, PopoverTrigger } from '@/components/ui/popover';
import { useTheme } from '@/hooks/useTheme';
import type { Theme } from '@/hooks/useTheme';
import { cn } from '@/lib/utils';

interface ThemeMenuButtonProps {
  className?: string;
  align?: 'start' | 'center' | 'end';
  buttonClassName?: string;
}

function ThemeIcon({ theme, className }: { theme: Theme; className?: string }) {
  switch (theme) {
    case 'light':
      return (
        <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="4" strokeWidth={2} />
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 2v2m0 16v2M4.93 4.93l1.41 1.41m11.32 11.32l1.41 1.41M2 12h2m16 0h2M6.34 17.66l-1.41 1.41M17.66 6.34l1.41-1.41" />
        </svg>
      );
    case 'dark':
      return (
        <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 12.79A9 9 0 1 1 11.21 3 7 7 0 0 0 21 12.79z" />
        </svg>
      );
    case 'colorblind-protanopia':
      return (
        <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M2 12s3.5-6 10-6 10 6 10 6-3.5 6-10 6-10-6-10-6z" />
          <circle cx="12" cy="12" r="2.75" strokeWidth={2} />
        </svg>
      );
    case 'high-contrast':
      return (
        <svg className={className} fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
          <circle cx="12" cy="12" r="9" strokeWidth={2} />
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 3v18" />
        </svg>
      );
  }
}

export function ThemeMenuButton({ className, align = 'end', buttonClassName }: ThemeMenuButtonProps) {
  const { theme, setTheme, themes } = useTheme();

  const activeThemeLabel = useMemo(
    () => themes.find((option) => option.value === theme)?.label ?? 'Tema',
    [theme, themes]
  );

  return (
    <Popover>
      <PopoverTrigger asChild>
        <Button
          variant="ghost"
          size="icon"
          className={cn('h-9 w-9', buttonClassName)}
          title={`Tema actual: ${activeThemeLabel}`}
          aria-label={`Cambiar tema. Actual: ${activeThemeLabel}`}
        >
          <ThemeIcon theme={theme} className="h-5 w-5" />
        </Button>
      </PopoverTrigger>
      <PopoverContent align={align} className={cn('w-64 p-2', className)}>
        <div className="flex flex-col gap-1">
          {themes.map((option) => {
            const selected = option.value === theme;
            return (
              <button
                key={option.value}
                onClick={() => setTheme(option.value)}
                className={cn(
                  'w-full flex items-center justify-between rounded-md px-2 py-2 text-sm transition-colors',
                  selected
                    ? 'bg-primary text-primary-foreground'
                    : 'text-foreground hover:bg-accent hover:text-accent-foreground'
                )}
              >
                <span className="flex items-center gap-2">
                  <ThemeIcon theme={option.value} className="h-4 w-4" />
                  <span>{option.label}</span>
                </span>
                {selected && (
                  <svg className="h-4 w-4" fill="none" stroke="currentColor" viewBox="0 0 24 24" aria-hidden="true">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M5 13l4 4L19 7" />
                  </svg>
                )}
              </button>
            );
          })}
        </div>
      </PopoverContent>
    </Popover>
  );
}
