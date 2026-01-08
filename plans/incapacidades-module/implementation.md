# Módulo de Incapacidades - Implementación

## Goal
Integrar el módulo de Incapacidades en el frontend del sistema de RH. El backend (entidad, enums, DTOs, repositorio, consultas, mantenimientos, servicio y controlador) ya está completamente implementado.

## Prerequisites
Asegúrate de estar en la rama `feature/incapacidades-module` antes de comenzar la implementación.
Si no existe, créala desde main.

---

## Estado Actual del Backend ✅

Los siguientes componentes **ya están implementados**:

| Componente | Archivo | Estado |
|------------|---------|--------|
| Entidad | `Entidades/Incapacidades.java` | ✅ Completo |
| Enum TipoIncapacidad | `Entidades/Enums/TipoIncapacidad.java` | ✅ Completo |
| Enum TipoEntidadEmisora | `Entidades/Enums/TipoEntidadEmisora.java` | ✅ Completo |
| DTO Solicitud | `DTOs/Solicitud/SolicitudIncapacidadesDTO.java` | ✅ Completo |
| DTO Respuesta | `DTOs/Respuesta/RespuestaIncapacidadesDTO.java` | ✅ Completo |
| Repositorio | `Repositorios/IncapacidadesRepositorio.java` | ✅ Completo |
| Consultas | `Modulos/Consultas/ConsultasIncapacidades.java` | ✅ Completo |
| Mantenimientos | `Modulos/Mantenimientos/MantenimientosIncapacidades.java` | ✅ Completo |
| Servicio | `Modulos/Incapacidad/Servicio/ServicioIncapacidad.java` | ✅ Completo |
| Controlador | `Modulos/Incapacidad/Controlador/ControladorIncapacidad.java` | ✅ Completo |

**API Endpoints disponibles:**
- `GET /api/incapacidades` - Listar todas
- `GET /api/incapacidades/{id}` - Obtener por ID
- `POST /api/incapacidades` - Crear nueva
- `PUT /api/incapacidades/{id}` - Actualizar
- `DELETE /api/incapacidades/{id}` - Eliminar

---

## Step-by-Step Instructions

### Step 1: Agregar el servicio de Incapacidades en apiService.ts

- [x] Abrir el archivo `frontend/src/services/apiService.ts`
- [x] Localizar la línea donde se exportan los servicios (aproximadamente línea 360)
- [x] Agregar el export del servicio de incapacidades después de `jefesDepartamentoService`

Buscar este bloque al final del archivo:
```typescript
export const jefesDepartamentoService = new ApiService<JefeDepartamento>('jefes-departamento');
```

Agregar inmediatamente después:
```typescript
export const incapacidadesService = new ApiService<Incapacidad>('incapacidades');
```

##### Step 1 Verification Checklist
- [ ] El archivo compila sin errores TypeScript
- [ ] Ejecutar `npm run build` en la carpeta frontend para verificar

#### Step 1 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

### Step 2: Agregar Incapacidades al tipo EntityType en MantenimientosView

- [ ] Abrir el archivo `frontend/src/components/dashboard/MantenimientosView.tsx`
- [ ] Localizar el import de servicios (línea ~13-27) y agregar `incapacidadesService`
- [ ] Localizar el tipo `EntityType` (línea ~29-42) y agregar `'incapacidades'`
- [ ] Localizar el objeto `entities` (línea ~49-62) y agregar la configuración

**2.1 - Agregar el import:**

Buscar:
```typescript
import {
  empleadosService,
  departamentosService,
  direccionesService,
  puestosService,
  configuracionRentaService,
  asistenciasService,
  aguinaldosService,
  horasExtraService,
  permisosService,
  liquidacionesService,
  planillasService,
  evaluacionesService,
  jefesDepartamentoService,
} from '@/services/apiService';
```

Reemplazar con:
```typescript
import {
  empleadosService,
  departamentosService,
  direccionesService,
  puestosService,
  configuracionRentaService,
  asistenciasService,
  aguinaldosService,
  horasExtraService,
  permisosService,
  incapacidadesService,
  liquidacionesService,
  planillasService,
  evaluacionesService,
  jefesDepartamentoService,
} from '@/services/apiService';
```

**2.2 - Agregar al tipo EntityType:**

Buscar:
```typescript
type EntityType =
  | 'empleados'
  | 'departamentos'
  | 'direcciones'
  | 'puestos'
  | 'configuracion-renta'
  | 'asistencias'
  | 'aguinaldos'
  | 'horas-extra'
  | 'permisos'
  | 'liquidaciones'
  | 'planillas'
  | 'evaluaciones'
  | 'jefes-departamento';
```

Reemplazar con:
```typescript
type EntityType =
  | 'empleados'
  | 'departamentos'
  | 'direcciones'
  | 'puestos'
  | 'configuracion-renta'
  | 'asistencias'
  | 'aguinaldos'
  | 'horas-extra'
  | 'permisos'
  | 'incapacidades'
  | 'liquidaciones'
  | 'planillas'
  | 'evaluaciones'
  | 'jefes-departamento';
```

**2.3 - Agregar al objeto entities:**

Buscar:
```typescript
const entities: Record<EntityType, EntityConfig> = {
  empleados: { name: 'Empleados', icon: '👥' },
  departamentos: { name: 'Departamentos', icon: '🏢' },
  direcciones: { name: 'Direcciones', icon: '📍' },
  puestos: { name: 'Puestos', icon: '💼' },
  'configuracion-renta': { name: 'Configuración de Renta', icon: '💰' },
  asistencias: { name: 'Asistencias', icon: '⏰' },
  aguinaldos: { name: 'Aguinaldos', icon: '🎁' },
  'horas-extra': { name: 'Horas Extra', icon: '⏱️' },
  permisos: { name: 'Permisos', icon: '📋' },
  liquidaciones: { name: 'Liquidaciones', icon: '💵' },
  planillas: { name: 'Planillas', icon: '📊' },
  evaluaciones: { name: 'Evaluaciones', icon: '⭐' },
  'jefes-departamento': { name: 'Jefes de Departamento', icon: '👔' },
};
```

Reemplazar con:
```typescript
const entities: Record<EntityType, EntityConfig> = {
  empleados: { name: 'Empleados', icon: '👥' },
  departamentos: { name: 'Departamentos', icon: '🏢' },
  direcciones: { name: 'Direcciones', icon: '📍' },
  puestos: { name: 'Puestos', icon: '💼' },
  'configuracion-renta': { name: 'Configuración de Renta', icon: '💰' },
  asistencias: { name: 'Asistencias', icon: '⏰' },
  aguinaldos: { name: 'Aguinaldos', icon: '🎁' },
  'horas-extra': { name: 'Horas Extra', icon: '⏱️' },
  permisos: { name: 'Permisos', icon: '📋' },
  incapacidades: { name: 'Incapacidades', icon: '🏥' },
  liquidaciones: { name: 'Liquidaciones', icon: '💵' },
  planillas: { name: 'Planillas', icon: '📊' },
  evaluaciones: { name: 'Evaluaciones', icon: '⭐' },
  'jefes-departamento': { name: 'Jefes de Departamento', icon: '👔' },
};
```

##### Step 2 Verification Checklist
- [ ] No hay errores de TypeScript en el archivo
- [ ] La entidad "Incapacidades" aparece en la lista del menú lateral

#### Step 2 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

### Step 3: Agregar entityRelations para Incapacidades

- [ ] Localizar el objeto `entityRelations` (línea ~70-106)
- [ ] Agregar la relación para incapacidades (necesita idEmpleado)

Buscar (al final del objeto entityRelations, antes del cierre `};`):
```typescript
  'jefes-departamento': [
    { fieldName: 'idDepartamento', label: 'Departamento', entityType: 'departamentos', displayField: 'nombre' },
    { fieldName: 'idEmpleado', label: 'Empleado', entityType: 'empleados', displayField: 'nombre' },
  ],
  departamentos: [],
  direcciones: [],
  'configuracion-renta': [],
  planillas: [],
};
```

Reemplazar con:
```typescript
  'jefes-departamento': [
    { fieldName: 'idDepartamento', label: 'Departamento', entityType: 'departamentos', displayField: 'nombre' },
    { fieldName: 'idEmpleado', label: 'Empleado', entityType: 'empleados', displayField: 'nombre' },
  ],
  incapacidades: [
    { fieldName: 'idEmpleado', label: 'Empleado', entityType: 'empleados', displayField: 'nombre' },
  ],
  departamentos: [],
  direcciones: [],
  'configuracion-renta': [],
  planillas: [],
};
```

##### Step 3 Verification Checklist
- [ ] No hay errores de TypeScript
- [ ] El dropdown de empleados carga correctamente al abrir el modal de incapacidades

#### Step 3 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

### Step 4: Agregar Incapacidades a getServiceForEntity

- [ ] Localizar la función `getServiceForEntity` (línea ~284)
- [ ] Agregar el caso para 'incapacidades'

Buscar:
```typescript
      case 'permisos':
        return permisosService;
      case 'liquidaciones':
        return liquidacionesService;
```

Reemplazar con:
```typescript
      case 'permisos':
        return permisosService;
      case 'incapacidades':
        return incapacidadesService;
      case 'liquidaciones':
        return liquidacionesService;
```

##### Step 4 Verification Checklist
- [ ] No hay errores de TypeScript
- [ ] Al seleccionar "Incapacidades", la tabla intenta cargar datos (puede estar vacía)

#### Step 4 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

### Step 5: Agregar columnas para Incapacidades en getColumnsForEntity

- [ ] Localizar la función `getColumnsForEntity` (línea ~317)
- [ ] Agregar el caso para 'incapacidades' después del caso 'permisos'

Buscar el caso de permisos (aproximadamente línea 429-448):
```typescript
      case 'permisos':
        return [
          { key: 'fechaInicio', label: 'Fecha Inicio' },
          { key: 'fechaFin', label: 'Fecha Fin' },
          { key: 'diasTotales', label: 'Días' },
          { key: 'tipoPermiso', label: 'Tipo' },
          { key: 'motivo', label: 'Motivo' },
          {
            key: 'estadoSolicitud',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                PENDIENTE: '🟡 Pendiente',
                APROBADA: '✅ Aprobada',
                RECHAZADA: '❌ Rechazada',
              };
              return estados[value] || value;
            },
          },
        ];
      case 'liquidaciones':
```

Reemplazar con:
```typescript
      case 'permisos':
        return [
          { key: 'fechaInicio', label: 'Fecha Inicio' },
          { key: 'fechaFin', label: 'Fecha Fin' },
          { key: 'diasTotales', label: 'Días' },
          { key: 'tipoPermiso', label: 'Tipo' },
          { key: 'motivo', label: 'Motivo' },
          {
            key: 'estadoSolicitud',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                PENDIENTE: '🟡 Pendiente',
                APROBADA: '✅ Aprobada',
                RECHAZADA: '❌ Rechazada',
              };
              return estados[value] || value;
            },
          },
        ];
      case 'incapacidades':
        return [
          { key: 'fechaInicio', label: 'Fecha Inicio' },
          { key: 'fechaFin', label: 'Fecha Fin' },
          { key: 'diasTotales', label: 'Días' },
          { 
            key: 'tipoIncapacidad', 
            label: 'Tipo',
            render: (value) => {
              const tipos: Record<string, string> = {
                ENFERMEDAD_COMUN: 'Enfermedad Común',
                ACCIDENTE_LABORAL: 'Accidente Laboral',
                ACCIDENTE_TRANSITO: 'Accidente Tránsito',
                MATERNIDAD: 'Maternidad',
                RIESGO_EMBARAZO: 'Riesgo Embarazo',
                ENFERMEDAD_PROFESIONAL: 'Enfermedad Profesional',
              };
              return tipos[value] || value;
            },
          },
          { 
            key: 'entidadEmisora', 
            label: 'Entidad',
            render: (value) => {
              const entidades: Record<string, string> = {
                CCSS: 'CCSS',
                INS: 'INS',
                CLINICA_PRIVADA: 'Clínica Privada',
                OTRO: 'Otro',
              };
              return entidades[value] || value;
            },
          },
          {
            key: 'porcentajePago',
            label: '% Pago',
            render: (value) => `${value}%`,
          },
          {
            key: 'estadoSolicitud',
            label: 'Estado',
            render: (value) => {
              const estados: Record<string, string> = {
                PENDIENTE: '🟡 Pendiente',
                APROBADA: '✅ Aprobada',
                RECHAZADA: '❌ Rechazada',
                CANCELADA: '⛔ Cancelada',
              };
              return estados[value] || value;
            },
          },
        ];
      case 'liquidaciones':
```

##### Step 5 Verification Checklist
- [ ] No hay errores de TypeScript
- [ ] La tabla de incapacidades muestra las columnas correctamente
- [ ] Los renders de estado, tipo y entidad muestran texto legible

#### Step 5 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

### Step 6: Agregar formulario para Incapacidades en renderForm

- [ ] Localizar la función `renderForm` (línea ~540 aproximadamente)
- [ ] Agregar el caso para 'incapacidades' después del caso 'permisos' (aproximadamente línea 912-997)

Buscar el final del caso 'permisos' y el inicio de 'liquidaciones':
```typescript
            <div>
              <Label htmlFor="idEmpleado">Empleado</Label>
              <SearchableSelect
                options={relationOptions['idEmpleado'] || []}
                value={formData.idEmpleado}
                onChange={(value) => setFormData({ ...formData, idEmpleado: value })}
                placeholder="Seleccionar empleado..."
                searchPlaceholder="Buscar empleado..."
              />
            </div>
          </div>
        );

      case 'liquidaciones':
```

Reemplazar con:
```typescript
            <div>
              <Label htmlFor="idEmpleado">Empleado</Label>
              <SearchableSelect
                options={relationOptions['idEmpleado'] || []}
                value={formData.idEmpleado}
                onChange={(value) => setFormData({ ...formData, idEmpleado: value })}
                placeholder="Seleccionar empleado..."
                searchPlaceholder="Buscar empleado..."
              />
            </div>
          </div>
        );

      case 'incapacidades':
        return (
          <div className="space-y-4">
            <div>
              <Label htmlFor="fechaInicio">Fecha Inicio</Label>
              <DatePicker
                value={formData.fechaInicio || ''}
                onChange={(date) => setFormData({ ...formData, fechaInicio: date })}
                placeholder="Seleccionar fecha de inicio"
              />
            </div>
            <div>
              <Label htmlFor="fechaFin">Fecha Fin</Label>
              <DatePicker
                value={formData.fechaFin || ''}
                onChange={(date) => setFormData({ ...formData, fechaFin: date })}
                placeholder="Seleccionar fecha de fin"
              />
            </div>
            <div>
              <Label htmlFor="diasTotales">Días Totales</Label>
              <Input
                id="diasTotales"
                type="number"
                min="1"
                value={formData.diasTotales || ''}
                onChange={(e) =>
                  setFormData({ ...formData, diasTotales: parseInt(e.target.value) })
                }
              />
            </div>
            <div>
              <Label htmlFor="tipoIncapacidad">Tipo de Incapacidad</Label>
              <select
                id="tipoIncapacidad"
                value={formData.tipoIncapacidad || 'ENFERMEDAD_COMUN'}
                onChange={(e) =>
                  setFormData({ ...formData, tipoIncapacidad: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="ENFERMEDAD_COMUN">Enfermedad Común</option>
                <option value="ACCIDENTE_LABORAL">Accidente Laboral</option>
                <option value="ACCIDENTE_TRANSITO">Accidente de Tránsito</option>
                <option value="MATERNIDAD">Maternidad</option>
                <option value="RIESGO_EMBARAZO">Riesgo de Embarazo</option>
                <option value="ENFERMEDAD_PROFESIONAL">Enfermedad Profesional</option>
              </select>
            </div>
            <div>
              <Label htmlFor="entidadEmisora">Entidad Emisora</Label>
              <select
                id="entidadEmisora"
                value={formData.entidadEmisora || 'CCSS'}
                onChange={(e) =>
                  setFormData({ ...formData, entidadEmisora: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="CCSS">CCSS</option>
                <option value="INS">INS</option>
                <option value="CLINICA_PRIVADA">Clínica Privada</option>
                <option value="OTRO">Otro</option>
              </select>
            </div>
            <div>
              <Label htmlFor="porcentajePago">Porcentaje de Pago (%)</Label>
              <Input
                id="porcentajePago"
                type="number"
                min="0"
                max="100"
                step="0.1"
                value={formData.porcentajePago || ''}
                onChange={(e) =>
                  setFormData({ ...formData, porcentajePago: parseFloat(e.target.value) })
                }
                placeholder="Ej: 60"
              />
            </div>
            <div>
              <Label htmlFor="numeroDocumento">Número de Documento</Label>
              <Input
                id="numeroDocumento"
                value={formData.numeroDocumento || ''}
                onChange={(e) =>
                  setFormData({ ...formData, numeroDocumento: e.target.value })
                }
                placeholder="Número de boleta/certificado"
              />
            </div>
            <div>
              <Label htmlFor="estadoSolicitud">Estado</Label>
              <select
                id="estadoSolicitud"
                value={formData.estadoSolicitud || 'PENDIENTE'}
                onChange={(e) =>
                  setFormData({ ...formData, estadoSolicitud: e.target.value })
                }
                className="flex h-10 w-full rounded-md border border-input bg-background px-3 py-2 text-sm"
              >
                <option value="PENDIENTE">Pendiente</option>
                <option value="APROBADA">Aprobada</option>
                <option value="RECHAZADA">Rechazada</option>
                <option value="CANCELADA">Cancelada</option>
              </select>
            </div>
            <div>
              <Label htmlFor="observaciones">Observaciones</Label>
              <Input
                id="observaciones"
                value={formData.observaciones || ''}
                onChange={(e) =>
                  setFormData({ ...formData, observaciones: e.target.value })
                }
                placeholder="Notas adicionales (opcional)"
              />
            </div>
            <div>
              <Label htmlFor="urlDocumentoAdjunto">URL Documento Adjunto</Label>
              <Input
                id="urlDocumentoAdjunto"
                value={formData.urlDocumentoAdjunto || ''}
                onChange={(e) =>
                  setFormData({ ...formData, urlDocumentoAdjunto: e.target.value })
                }
                placeholder="URL del certificado médico (opcional)"
              />
            </div>
            <div>
              <Label htmlFor="idEmpleado">Empleado</Label>
              <SearchableSelect
                options={relationOptions['idEmpleado'] || []}
                value={formData.idEmpleado}
                onChange={(value) => setFormData({ ...formData, idEmpleado: value })}
                placeholder="Seleccionar empleado..."
                searchPlaceholder="Buscar empleado..."
              />
            </div>
          </div>
        );

      case 'liquidaciones':
```

##### Step 6 Verification Checklist
- [ ] No hay errores de TypeScript
- [ ] El formulario de incapacidades se muestra correctamente
- [ ] Todos los campos del formulario funcionan (fechas, selects, inputs)
- [ ] El SearchableSelect de empleados carga y funciona correctamente

#### Step 6 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

## Testing Final

Una vez completados todos los pasos, realizar las siguientes pruebas:

### Backend
- [ ] Verificar que el backend está corriendo: `GET http://localhost:8080/api/incapacidades` debe retornar `[]` o lista de incapacidades

### Frontend
1. [ ] Navegar a Dashboard → Mantenimientos y Consultas
2. [ ] Seleccionar "Incapacidades" en el menú lateral
3. [ ] **Crear**: Click en "+ Nuevo", llenar formulario con:
   - Fecha Inicio: 2026-01-07
   - Fecha Fin: 2026-01-14
   - Días Totales: 7
   - Tipo: Enfermedad Común
   - Entidad: CCSS
   - Porcentaje: 60
   - Número Documento: INC-2026-001
   - Estado: Pendiente
   - Empleado: (seleccionar uno)
   - Click "Guardar"
4. [ ] **Leer**: Verificar que el registro aparece en la tabla
5. [ ] **Actualizar**: Click en "Editar", cambiar estado a "Aprobada", guardar
6. [ ] **Eliminar**: Click en "Eliminar", confirmar eliminación

---

## Resumen de Archivos Modificados

| Archivo | Cambios |
|---------|---------|
| `frontend/src/services/apiService.ts` | Agregar export de `incapacidadesService` |
| `frontend/src/components/dashboard/MantenimientosView.tsx` | Agregar import, EntityType, entities, entityRelations, getServiceForEntity, getColumnsForEntity, renderForm |
