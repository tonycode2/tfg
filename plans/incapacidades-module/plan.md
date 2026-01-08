# Módulo de Incapacidades

**Branch:** `feature/incapacidades-module`
**Description:** Implementar el módulo completo de gestión de incapacidades para el sistema de RH

## Goal
Crear el módulo de Incapacidades que permite registrar, consultar y gestionar las incapacidades médicas de los empleados, incluyendo información de fechas, tipo de incapacidad (enfermedad común, accidente laboral, maternidad, etc.), porcentaje de pago, entidad emisora y estado de aprobación. Este módulo es esencial para el cálculo correcto de planilla y el seguimiento de ausencias laborales justificadas.

## Implementation Steps

### Step 1: Crear Entidad y Enums (TipoIncapacidad y TipoEntidadEmisora)
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Entidades/Incapacidades.java`
- `src/main/java/com/anthony/tfg/tfg/Entidades/Enums/TipoIncapacidad.java`
- `src/main/java/com/anthony/tfg/tfg/Entidades/Enums/TipoEntidadEmisora.java`

**What:** Crear la entidad JPA `Incapacidades` con todos los campos necesarios (fechaInicio, fechaFin, diasTotales, tipoIncapacidad, estadoSolicitud, porcentajePago, entidadEmisora, numeroDocumento, observaciones, urlDocumentoAdjunto) y relación ManyToOne con Empleados. Crear el enum `TipoIncapacidad` con valores: ENFERMEDAD_COMUN, ACCIDENTE_LABORAL, ACCIDENTE_TRANSITO, MATERNIDAD, RIESGO_EMBARAZO, ENFERMEDAD_PROFESIONAL. Crear el enum `TipoEntidadEmisora` con valores: CCSS, INS, CLINICA_PRIVADA, OTRO.

**Testing:** Verificar que la aplicación inicie sin errores y que la tabla `incapacidades` se cree automáticamente en PostgreSQL con todas las columnas correctas.

---

### Step 2: Crear DTOs de Request y Response
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/DTOs/Solicitud/SolicitudIncapacidadesDTO.java`
- `src/main/java/com/anthony/tfg/tfg/DTOs/Respuesta/RespuestaIncapacidadesDTO.java`

**What:** Crear el DTO de solicitud con validaciones (@NotNull, @NotBlank, @Positive) para fechaInicio, fechaFin, tipoIncapacidad, idEmpleado y demás campos. Crear el DTO de respuesta con información denormalizada del empleado (nombreEmpleado, primerApellidoEmpleado, segundoApellidoEmpleado).

**Testing:** Compilar el proyecto con `./mvnw compile` y verificar que no hay errores de sintaxis.

---

### Step 3: Crear Repositorio JPA
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Repositorios/IncapacidadesRepositorio.java`

**What:** Crear la interfaz del repositorio extendiendo `JpaRepository<Incapacidades, Long>` con queries personalizadas si son necesarias (por ejemplo, buscar por empleado o por rango de fechas).

**Testing:** Verificar que la aplicación inicie correctamente y el repositorio sea reconocido por Spring.

---

### Step 4: Implementar Consultas e Interface
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Modulos/Consultas/ConsultasIncapacidades.java`

**What:** Crear la clase de servicio `ConsultasIncapacidades` implementando `ConsultaInterface<Incapacidades>` con los métodos `obtenerPorId()` y `obtenerTodos()`.

**Testing:** Escribir test unitario o verificar mediante logs que los métodos de consulta funcionan.

---

### Step 5: Implementar Mantenimientos e Interface
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Modulos/Mantenimientos/MantenimientosIncapacidades.java`

**What:** Crear la clase de servicio `MantenimientosIncapacidades` implementando `MantenimientoInterface<Incapacidades>` con los métodos `crear()`, `actualizar()` y `eliminar()`.

**Testing:** Verificar mediante pruebas manuales que las operaciones CRUD funcionan correctamente.

---

### Step 6: Implementar Servicio Principal
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Modulos/Incapacidades/Servicio/ServicioIncapacidades.java`

**What:** Implementar `ServicioInterface<RespuestaIncapacidadesDTO, SolicitudIncapacidadesDTO, Incapacidades>` con toda la lógica de negocio: validaciones, conversión DTO↔Entity, manejo de errores con excepciones personalizadas (ResourceNotFoundException, BadRequestException), logging con @Slf4j.

**Testing:** Ejecutar tests unitarios del servicio verificando conversiones y validaciones.

---

### Step 7: Implementar Controlador REST
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Modulos/Incapacidades/Controlador/ControladorIncapacidades.java`

**What:** Crear el controlador REST con endpoints: GET `/api/incapacidades`, GET `/api/incapacidades/{id}`, POST `/api/incapacidades`, PUT `/api/incapacidades/{id}`, DELETE `/api/incapacidades/{id}`. Usar @Validated para validación de DTOs.

**Testing:** Probar los endpoints usando Postman o curl:
- `GET http://localhost:8080/api/incapacidades` (lista vacía [])
- `POST http://localhost:8080/api/incapacidades` (crear registro)
- Verificar CRUD completo

---

### Step 8: Integrar en Frontend - ApiService
**Files:** 
- `frontend/src/services/apiService.ts`

**What:** Agregar la interfaz `Incapacidad` con todos los campos y crear la instancia `incapacidadesService = new ApiService<Incapacidad>('incapacidades')`.

**Testing:** Verificar que TypeScript compila sin errores (`npm run build`).

---

### Step 9: Integrar en Frontend - MantenimientosView
**Files:** 
- `frontend/src/components/dashboard/MantenimientosView.tsx`

**What:** 
1. Agregar `'incapacidades'` al tipo `EntityType`
2. Agregar configuración en `entities`: `incapacidades: { name: 'Incapacidades', icon: '🏥' }`
3. Agregar relación en `entityRelations` para el campo idEmpleado
4. Agregar caso en `getServiceForEntity()`
5. Agregar columnas en `getColumnsForEntity()` (fechaInicio, fechaFin, diasTotales, tipoIncapacidad, porcentajePago, estadoSolicitud)
6. Agregar formulario en `renderForm()` con DatePicker, Input, Select y SearchableSelect

**Testing:** 
1. Iniciar frontend (`npm run dev`)
2. Navegar a Dashboard → Mantenimientos
3. Seleccionar entidad "Incapacidades"
4. Probar crear, editar, visualizar y eliminar registros
5. Verificar que la relación con Empleado funciona correctamente

---

## Campos de la Entidad Incapacidades

| Campo | Tipo | Descripción |
|-------|------|-------------|
| id | Long | ID autogenerado |
| fechaInicio | Date | Fecha de inicio de la incapacidad |
| fechaFin | Date | Fecha de fin de la incapacidad |
| diasTotales | Integer | Número total de días |
| tipoIncapacidad | TipoIncapacidad | Enum: ENFERMEDAD_COMUN, ACCIDENTE_LABORAL, ACCIDENTE_TRANSITO, MATERNIDAD, RIESGO_EMBARAZO, ENFERMEDAD_PROFESIONAL |
| estadoSolicitud | EstadoSolicitud | Reutiliza enum existente: PENDIENTE, APROBADA, RECHAZADA, CANCELADA |
| porcentajePago | Double | Porcentaje de salario (60%, 100%, etc.) |
| entidadEmisora | TipoEntidadEmisora | Enum: CCSS, INS, CLINICA_PRIVADA, OTRO |
| numeroDocumento | String | Número de boleta/documento |
| observaciones | String | Notas adicionales (nullable) |
| urlDocumentoAdjunto | String | URL del certificado médico (nullable) |
| empleado | Empleados | Relación ManyToOne con Empleados |

## Notas Técnicas

- Reutilizar el enum `EstadoSolicitud` existente en lugar de crear uno nuevo
- El campo `porcentajePago` usa tipo Double para permitir valores como 60.0, 100.0
- El enum `TipoEntidadEmisora` estandariza los valores: CCSS, INS, CLINICA_PRIVADA, OTRO
- El `PlanillaDetalle` ya tiene campo `monto_incapacidad` para integración futura con cálculo de planilla
- Seguir el patrón exacto del módulo `Permisos` que tiene estructura muy similar
