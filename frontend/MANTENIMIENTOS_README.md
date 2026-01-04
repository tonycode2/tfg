# Módulo de Mantenimientos y Consultas

## Descripción

El módulo de **Mantenimientos y Consultas** es una interfaz administrativa que permite a usuarios con rol **ADMIN** gestionar todas las entidades de la base de datos mediante operaciones CRUD (Crear, Leer, Actualizar, Eliminar).

## Características

### Acceso Restringido
- ✅ Solo visible para usuarios con rol `ADMIN`
- ✅ Menú filtrado automáticamente según el rol del usuario autenticado

### Entidades Gestionables

El módulo soporta las siguientes entidades:

1. **Empleados** 👥
   - Gestión completa de información de empleados
   - Campos: cédula, nombre, apellidos, correo, salario, estado activo

2. **Departamentos** 🏢
   - Creación y edición de departamentos organizacionales
   - Campos: nombre

3. **Direcciones** 📍
   - Administración de direcciones
   - Campos: provincia, cantón, distrito, dirección exacta

4. **Puestos** 💼
   - Configuración de puestos de trabajo
   - Campos: nombre, salario mínimo, horarios de entrada/salida

5. **Configuración de Renta** 💰
   - Definición de rangos de renta
   - Campos: monto mínimo, monto máximo, porcentaje

6. **Asistencias** ⏰
   - Registro de asistencias de empleados
   - Campos: fecha, hora entrada, hora salida, horas trabajadas

7. **Aguinaldos** 🎁
   - Gestión de aguinaldos anuales
   - Campos: año, fecha inicio/fin período, monto, fecha de pago

## Arquitectura del Módulo

### Componentes Principales

#### 1. MantenimientosView.tsx
Componente principal que gestiona:
- Selección de entidad mediante cards visuales
- Switch dinámico entre vista de selección y vista de tabla
- Integración con DataTable y Modal para CRUD

#### 2. DataTable.tsx
Componente genérico reutilizable con:
- Paginación automática (10 registros por página)
- Columnas configurables con renderizado personalizado
- Botones de acción: Editar y Eliminar
- Estados de carga y error
- Confirmación antes de eliminar

#### 3. Modal.tsx
Modal reutilizable para formularios:
- Overlay con fondo semitransparente
- Cierre con botón X o clic fuera del modal
- Estados de carga durante submit
- Cancelar y Guardar acciones

#### 4. apiService.ts
Servicio genérico de API:
- Clase `ApiService<T>` para operaciones CRUD
- Autenticación automática con JWT
- Manejo de errores estandarizado
- Interfaces TypeScript para cada entidad
- Instancias pre-configuradas para cada servicio

### Flujo de Trabajo

```
1. Usuario Admin accede al menú
2. Selecciona "Mantenimientos y Consultas"
3. Ve grid con todas las entidades disponibles
4. Selecciona una entidad (ej: Departamentos)
5. Se muestra tabla con registros paginados
6. Puede:
   - Ver todos los registros
   - Crear nuevo registro (botón "Nuevo Registro")
   - Editar registro existente (botón "Editar")
   - Eliminar registro (botón "Eliminar" con confirmación)
   - Navegar entre páginas (botones Anterior/Siguiente)
7. Al crear/editar, se abre modal con formulario
8. Al guardar, se actualiza la tabla automáticamente
9. Puede volver a selección de entidades (botón "atrás")
```

## Integración con Backend

### Endpoints Utilizados

Para cada entidad existe un controlador REST con los siguientes endpoints:

```
GET    /api/v1/{entidad}?page=0&size=10  - Listar con paginación
GET    /api/v1/{entidad}/{id}             - Obtener por ID
POST   /api/v1/{entidad}                  - Crear nuevo
PUT    /api/v1/{entidad}/{id}             - Actualizar existente
DELETE /api/v1/{entidad}/{id}             - Eliminar
```

### Formato de Respuesta Paginada

```json
{
  "content": [...],          // Array de entidades
  "totalElements": 25,       // Total de registros
  "totalPages": 3,           // Total de páginas
  "size": 10,                // Tamaño de página
  "number": 0,               // Página actual (0-based)
  "first": true,             // Si es la primera página
  "last": false              // Si es la última página
}
```

### Autenticación

Todas las peticiones incluyen el header:
```
Authorization: Bearer {JWT_TOKEN}
```

El token se obtiene del `localStorage` mediante `authService.getToken()`.

## Formularios por Entidad

### Departamentos
- **Nombre** (text): Nombre del departamento

### Configuración de Renta
- **Monto Mínimo** (number): Límite inferior del rango
- **Monto Máximo** (number): Límite superior del rango
- **Porcentaje** (number): Porcentaje de renta aplicable

### Direcciones
- **Provincia** (text): Provincia de Costa Rica
- **Cantón** (text): Cantón
- **Distrito** (text): Distrito
- **Dirección Exacta** (text): Descripción detallada

### Puestos
- **Nombre** (text): Nombre del puesto
- **Salario Mínimo** (number): Salario base del puesto
- **Hora Entrada** (time): Hora de inicio de jornada
- **Hora Salida** (time): Hora de fin de jornada

### Asistencias
- **Fecha** (date): Fecha de asistencia
- **Hora Entrada** (time): Hora de entrada registrada
- **Hora Salida** (time): Hora de salida registrada

### Aguinaldos
- **Año** (number): Año del aguinaldo
- **Fecha Inicio Período** (date): Inicio del período de cálculo
- **Fecha Fin Período** (date): Fin del período de cálculo
- **Monto Aguinaldo** (number): Monto calculado
- **Fecha Pago** (date): Fecha de pago programada

## Personalización de Columnas

Las columnas se configuran en el método `getColumnsForEntity()`:

```typescript
{
  key: 'salarioBase',               // Propiedad del objeto
  label: 'Salario',                 // Texto del encabezado
  render: (value) => `₡${value?.toLocaleString()}`,  // Formato personalizado
}
```

### Formatos Disponibles
- **Moneda**: `₡${value?.toLocaleString()}`
- **Estado**: `value ? '✅ Activo' : '❌ Inactivo'`
- **Fecha**: Renderizado directo (formato backend: yyyy-MM-dd)
- **Tiempo**: Renderizado directo (formato backend: HH:mm:ss)

## Manejo de Errores

### Errores de API
- Se muestran mediante `alert()` al usuario
- Incluyen el mensaje de error del backend
- No detienen el flujo de la aplicación

### Confirmaciones
- Eliminación requiere confirmación con `confirm()`
- Previene eliminaciones accidentales

## Mejoras Futuras

### Entidades Pendientes
- Empleados (requiere selects para FK)
- Horas Extra
- Permisos
- Liquidaciones
- Planillas
- Evaluaciones de Desempeño

### Funcionalidades Adicionales
- [ ] Búsqueda/filtrado en tablas
- [ ] Ordenamiento por columnas
- [ ] Exportación a CSV/Excel
- [ ] Validación de formularios más robusta
- [ ] Mensajes de éxito (toast notifications)
- [ ] Manejo de relaciones FK con selects dinámicos
- [ ] Vista previa antes de eliminar
- [ ] Historial de cambios (audit log)
- [ ] Filtros avanzados por fecha/rango

## Dependencias

```json
{
  "react": "^18.x",
  "tailwindcss": "^3.x",
  "@radix-ui/react-*": "^1.x" // Componentes UI de shadcn
}
```

## Notas Técnicas

### TypeScript Genéricos
El uso de genéricos (`<T>`) permite reutilizar componentes:
```typescript
ApiService<Departamento>
DataTable<Puesto>
Column<ConfiguracionRenta>
```

### Estado de Refresco
`refreshTrigger` es un contador que incrementa después de operaciones CRUD para forzar recarga de datos.

### Validación de Tipos
Las interfaces en `apiService.ts` garantizan type-safety en toda la aplicación.

## Testing

### Pruebas Manuales Recomendadas
1. Login como ADMIN
2. Verificar visibilidad del menú
3. Para cada entidad:
   - Crear registro nuevo
   - Editar registro existente
   - Eliminar registro
   - Navegar paginación
   - Verificar formato de datos

### Casos de Prueba
- ✅ Crear con campos vacíos (debe fallar en backend)
- ✅ Editar y cancelar (no debe guardar)
- ✅ Eliminar y confirmar/cancelar
- ✅ Paginación con más de 10 registros
- ✅ Tabla vacía (mensaje "No hay registros")
- ✅ Error de red (mensaje de error)

## Soporte

Para problemas o mejoras, contactar al equipo de desarrollo.
