# Navegación Horizontal Superior

**Branch:** `feature/horizontal-navigation`
**Description:** Reemplazar la barra lateral de navegación por una barra horizontal superior para todos los roles

## Goal
Transformar la navegación del sistema de una barra lateral (sidebar) a una barra horizontal superior que contenga el logo, los elementos de menú y las acciones del usuario. Esto aplica para todos los roles (ADMIN, HR, JEFE, EMPLEADO) sin excepción, mejorando la experiencia visual y liberando espacio horizontal en la interfaz.

## Análisis del Estado Actual

### Estructura Actual
```
┌──────────────────────────────────────────────────────────────┐
│ ┌──────────────┬───────────────────────────────────────────┐ │
│ │   SIDEBAR    │  HEADER (Logo + Usuario)                  │ │
│ │   (w-64)     ├───────────────────────────────────────────┤ │
│ │              │                                           │ │
│ │  - Menú      │  CONTENIDO PRINCIPAL                      │ │
│ │              │                                           │ │
│ └──────────────┴───────────────────────────────────────────┘ │
└──────────────────────────────────────────────────────────────┘
```

### Estructura Propuesta
```
┌──────────────────────────────────────────────────────────────┐
│  NAVBAR HORIZONTAL                                           │
│  [Logo] [Menú Items...                    ] [Usuario][☀️][👤] │
├──────────────────────────────────────────────────────────────┤
│                                                              │
│  CONTENIDO PRINCIPAL                                         │
│  (Ocupa todo el ancho disponible)                           │
│                                                              │
└──────────────────────────────────────────────────────────────┘
```

### Elementos de Menú por Rol
| Rol | Items | Cantidad |
|-----|-------|----------|
| EMPLEADO | Inicio, Mi Planilla, Mis Solicitudes, Asistencia | 4 |
| JEFE | + Horas Extra, Solicitudes Pendientes, Reportes | 7 |
| HR | + Planilla General, Liquidaciones, Aguinaldo, Empleados | 11 |
| ADMIN | Mantenimientos y Consultas (único) | 1 |

## Decisiones de Diseño

### [NEEDS CLARIFICATION] Manejo de múltiples items
Para HR con 11 items, la barra podría verse saturada. Opciones:
1. **Mostrar todos los items** en una sola línea (con scroll horizontal si es necesario)
2. **Agrupar en dropdowns** (ej: "Planilla" → Mi Planilla, Planilla General)
3. **Menú "Más"** para items menos frecuentes
4. **Icons only** con tooltips para ahorrar espacio

¿Cuál prefiere el cliente?

### [NEEDS CLARIFICATION] Comportamiento móvil
Para pantallas pequeñas (<768px):
1. **Hamburger menu** que abre un drawer/sidebar temporal
2. **Dropdown completo** bajo el logo
3. **Otra opción**

¿Cuál es la preferencia?

## Implementation Steps

### Step 1: Crear componente TopNavbar
**Files:** 
- `frontend/src/components/TopNavbar.tsx` (nuevo)

**What:** Crear un nuevo componente `TopNavbar` que integre:
- Logo a la izquierda
- Items de menú filtrados por rol en el centro
- Acciones de usuario a la derecha (nombre, logout, theme toggle, avatar)
- Soporte para estado activo en items
- Mismos estilos y tokens de color actuales

**Testing:** 
- Verificar que el componente renderiza correctamente en aislamiento
- Probar filtrado de items por rol (EMPLEADO ve 4 items, HR ve 11, etc.)

### Step 2: Modificar DashboardPage para usar TopNavbar
**Files:** 
- `frontend/src/pages/DashboardPage.tsx`

**What:** 
- Remover el import de `Sidebar`
- Importar el nuevo `TopNavbar`
- Cambiar el layout de `flex` horizontal a `flex-col` vertical
- La navbar va primero, luego el contenido principal ocupa el resto

**Testing:** 
- Navegar al dashboard y verificar que la navbar aparece arriba
- Verificar que la navegación entre vistas funciona
- Probar con diferentes roles

### Step 3: Eliminar componentes obsoletos y limpiar
**Files:** 
- `frontend/src/components/Sidebar.tsx` (eliminar o deprecar)
- `frontend/src/components/DashboardHeader.tsx` (eliminar - funcionalidad movida a TopNavbar)

**What:** 
- Eliminar o marcar como deprecated el `Sidebar.tsx`
- Eliminar `DashboardHeader.tsx` ya que su funcionalidad está integrada en `TopNavbar`
- Verificar que no hay imports huérfanos

**Testing:** 
- Build del frontend sin errores
- Todas las rutas funcionan correctamente
- No hay warnings de imports no utilizados

### Step 4: Agregar soporte responsivo (móvil)
**Files:** 
- `frontend/src/components/TopNavbar.tsx`

**What:** 
- Agregar media query breakpoint para móviles (<768px)
- Implementar hamburger menu con estado colapsado/expandido
- El menú móvil puede ser un drawer o dropdown
- Mantener accesibilidad (aria labels, keyboard navigation)

**Testing:** 
- Reducir tamaño de ventana y verificar que aparece hamburger
- Click en hamburger abre/cierra menú
- Items siguen funcionando en versión móvil
- Probar en device toolbar de Chrome DevTools

## Archivos Afectados (Resumen)

| Archivo | Acción |
|---------|--------|
| `TopNavbar.tsx` | CREAR |
| `DashboardPage.tsx` | MODIFICAR |
| `Sidebar.tsx` | ELIMINAR |
| `DashboardHeader.tsx` | ELIMINAR |

## Notas Técnicas

- Reutilizar la misma estructura de `menuItems` del Sidebar actual
- Mantener el patrón de `memo` y `useMemo` para optimización
- Usar los mismos tokens de color (`bg-card`, `border-border`, etc.)
- El componente `Button` de shadcn/ui se puede usar para los items
- Considerar usar `Popover` para dropdowns si se implementa agrupación
