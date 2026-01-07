# Custom Hooks - Sistema de RH

Esta carpeta contiene hooks personalizados reutilizables para la aplicación.

## 📦 Hooks Disponibles

### `useFetch<T>`
Hook para carga de datos con manejo automático de estados y cancelación de peticiones.

```typescript
import { useFetch } from '@/hooks';
import { empleadosService } from '@/services/apiService';

function EmpleadosList() {
  const { data: empleados, loading, error, refetch } = useFetch(empleadosService);

  if (loading) return <div>Cargando...</div>;
  if (error) return <div>Error: {error}</div>;

  return (
    <div>
      {empleados.map(emp => <div key={emp.id}>{emp.nombre}</div>)}
      <button onClick={refetch}>Recargar</button>
    </div>
  );
}
```

**Características:**
- ✅ Manejo automático de loading, error y data
- ✅ Cancelación automática con AbortController
- ✅ Prevención de memory leaks
- ✅ Función refetch() para recargar datos
- ✅ Soporte para respuestas paginadas

### `useForm<T>`
Hook para manejo de formularios con validación integrada.

```typescript
import { useForm } from '@/hooks';

interface LoginFormData {
  username: string;
  password: string;
}

function LoginForm() {
  const { 
    values, 
    errors, 
    handleChange, 
    handleSubmit, 
    isSubmitting 
  } = useForm<LoginFormData>({
    initialValues: {
      username: '',
      password: '',
    },
    validate: (values) => {
      const errors: Record<string, string> = {};
      if (!values.username) errors.username = 'Requerido';
      if (!values.password) errors.password = 'Requerido';
      return errors;
    },
    onSubmit: async (values) => {
      await authService.login(values);
    },
  });

  return (
    <form onSubmit={handleSubmit}>
      <input 
        name="username" 
        value={values.username} 
        onChange={handleChange}
        disabled={isSubmitting}
      />
      {errors.username && <span>{errors.username}</span>}
      
      <button type="submit" disabled={isSubmitting}>
        {isSubmitting ? 'Enviando...' : 'Login'}
      </button>
    </form>
  );
}
```

**Características:**
- ✅ Manejo centralizado de estado del formulario
- ✅ Validación integrada con función customizable
- ✅ Auto-limpieza de errores al escribir
- ✅ Estado de envío (isSubmitting)
- ✅ Funciones optimizadas con useCallback
- ✅ Soporte para inputs, textarea y select

### `useTheme`
Hook para manejo del tema (light/dark mode) de la aplicación.

```typescript
import { useTheme } from '@/hooks';

function ThemeToggle() {
  const { theme, toggleTheme } = useTheme();

  return (
    <button onClick={toggleTheme}>
      {theme === 'light' ? '🌙' : '☀️'}
    </button>
  );
}

// Envolver la app con ThemeProvider
import { ThemeProvider } from '@/hooks';

function App() {
  return (
    <ThemeProvider>
      <YourApp />
    </ThemeProvider>
  );
}
```

**Características:**
- ✅ Persistencia en localStorage
- ✅ Aplicación automática de clase CSS
- ✅ Context API para compartir estado

## 🎯 Beneficios de usar estos hooks

1. **Reducción de código duplicado**: Lógica común centralizada
2. **Mejor testabilidad**: Hooks pueden testearse independientemente
3. **Prevención de bugs**: Manejo consistente de estados y efectos
4. **Performance optimizada**: Uso correcto de memoización
5. **Mantenibilidad**: Código más limpio y organizado

## 📖 Patrones de Uso

### Composición de Hooks
Los hooks pueden combinarse para funcionalidad compleja:

```typescript
function EmpleadosPage() {
  const { data: empleados, loading, refetch } = useFetch(empleadosService);
  const { data: puestos } = useFetch(puestosService);
  
  const { values, handleChange, handleSubmit } = useForm({
    initialValues: { nombre: '', idPuesto: 0 },
    onSubmit: async (values) => {
      await empleadosService.create(values);
      refetch(); // Recargar lista después de crear
    }
  });

  // ... render
}
```

### Hooks Condicionales (❌ Incorrecto)
```typescript
// ❌ NO HACER ESTO
if (condition) {
  const data = useFetch(service);
}
```

### Hooks Condicionales (✅ Correcto)
```typescript
// ✅ HACER ESTO
const { data } = useFetch(service, { autoLoad: condition });
```

## 🔧 Extensión de Hooks

Para crear nuevos hooks basados en los existentes:

```typescript
// Ejemplo: Hook específico para empleados
export function useEmpleados() {
  const { data, loading, error, refetch } = useFetch(empleadosService);
  
  const activos = useMemo(
    () => data.filter(emp => emp.estaActivo),
    [data]
  );

  return { 
    empleados: data, 
    empleadosActivos: activos,
    loading, 
    error, 
    refetch 
  };
}
```

## 📚 Recursos Adicionales

- [React Hooks Documentation](https://react.dev/reference/react)
- [Custom Hooks Best Practices](https://react.dev/learn/reusing-logic-with-custom-hooks)
- [TypeScript + React Hooks](https://react-typescript-cheatsheet.netlify.app/docs/basic/getting-started/hooks/)
