# Extensión de Incapacidades - Documentación

## Descripción General

Se ha implementado la funcionalidad para que los jefes de departamento puedan extender incapacidades de sus empleados cuando sea necesario. Las extensiones requieren aprobación de Recursos Humanos.

## Componentes Implementados

### Backend

#### 1. Entidad `Incapacidades` (Actualizada)
**Archivo**: `Entidades/Incapacidades.java`

Nuevos campos agregados:
- `esExtension` (Boolean): Indica si la incapacidad es una extensión
- `incapacidadOriginal` (ManyToOne): Referencia a la incapacidad original
- `fechaFinOriginal` (LocalDate): Fecha fin original antes de la extensión
- `comentariosExtension` (String): Comentarios sobre la extensión

#### 2. DTO `SolicitudExtensionIncapacidadDTO` (Nuevo)
**Archivo**: `DTOs/Solicitud/SolicitudExtensionIncapacidadDTO.java`

Campos:
- `nuevaFechaFin` (LocalDate): Nueva fecha de finalización
- `diasAdicionales` (Integer): Días adicionales de la extensión
- `numeroDocumento` (String, opcional): Número de documento médico
- `observaciones` (String, opcional): Observaciones adicionales
- `urlDocumentoAdjunto` (String, opcional): URL del documento adjunto

#### 3. DTO `RespuestaIncapacidadesDTO` (Actualizado)
**Archivo**: `DTOs/Respuesta/RespuestaIncapacidadesDTO.java`

Campos agregados para mostrar información de extensión en el frontend.

#### 4. Controlador `ControladorIncapacidad` (Actualizado)
**Archivo**: `Modulos/Incapacidad/Controlador/ControladorIncapacidad.java`

Nuevos endpoints:

##### `GET /api/incapacidades/empleados-incapacitados-departamento`
- **Descripción**: Obtiene empleados actualmente incapacitados del departamento del jefe
- **Autorización**: JEFE, HR, ADMIN
- **Respuesta**: Lista de `RespuestaIncapacidadesDTO`

##### `POST /api/incapacidades/{id}/solicitar-extension`
- **Descripción**: Crea una solicitud de extensión para una incapacidad existente
- **Autorización**: JEFE, HR, ADMIN
- **Body**: `SolicitudExtensionIncapacidadDTO`
- **Respuesta**: `RespuestaIncapacidadesDTO` de la nueva extensión creada

#### 5. Servicio `ServicioIncapacidad` (Actualizado)
**Archivo**: `Modulos/Incapacidad/Servicio/ServicioIncapacidad.java`

Nuevos métodos:

##### `obtenerEmpleadosIncapacitadosDepartamento(Authentication auth)`
- Obtiene incapacidades activas (aprobadas y en curso) del departamento del jefe
- Valida que el usuario sea jefe del departamento
- Retorna lista de empleados actualmente incapacitados

##### `solicitarExtension(Long idIncapacidad, SolicitudExtensionIncapacidadDTO solicitud, Authentication auth)`
- Crea una nueva solicitud de incapacidad como extensión de una existente
- Validaciones:
  - La incapacidad original debe estar aprobada
  - El jefe debe tener permiso sobre el departamento
  - La nueva fecha fin debe ser posterior a la actual
- La extensión se crea con estado `PENDIENTE_RH` (va directo a RH)
- Mantiene referencia a la incapacidad original

#### 6. Repositorio `IncapacidadesRepositorio` (Actualizado)
**Archivo**: `Repositorios/IncapacidadesRepositorio.java`

Nueva consulta:
```java
@Query("SELECT i FROM Incapacidades i WHERE i.estadoSolicitud = 'APROBADA' " +
       "AND i.empleado.puesto.departamento.id = :idDepartamento " +
       "AND i.fechaInicio <= :fecha AND i.fechaFin >= :fecha " +
       "ORDER BY i.empleado.primerApellido")
List<Incapacidades> findIncapacidadesActivasByDepartamento(@Param("idDepartamento") Long idDepartamento, 
                                                            @Param("fecha") LocalDate fecha);
```

### Frontend

#### 1. Servicio `incapacidadesService.ts` (Actualizado)
**Archivo**: `frontend/src/services/incapacidadesService.ts`

Nueva interfaz:
```typescript
export interface SolicitudExtensionIncapacidad {
  nuevaFechaFin: string; // yyyy-MM-dd
  diasAdicionales: number;
  numeroDocumento?: string;
  observaciones?: string;
  urlDocumentoAdjunto?: string;
}
```

Nuevas funciones:
- `obtenerEmpleadosIncapacitadosDepartamento()`: Obtiene empleados incapacitados
- `solicitarExtension(id, solicitud)`: Solicita extensión de incapacidad

#### 2. Vista `IncapacidadesPendientesView.tsx` (Actualizado)
**Archivo**: `frontend/src/components/dashboard/IncapacidadesPendientesView.tsx`

Cambios implementados:

##### Nueva Sección: Empleados Actualmente Incapacitados
- Tabla mostrando empleados con incapacidades activas
- Columnas: Empleado, Tipo, Fecha Inicio, Fecha Fin, Días, Entidad
- Botón "Extender" para cada empleado incapacitado

##### Nuevo Modal: Extender Incapacidad
- Muestra información de la incapacidad actual
- Formulario para solicitar extensión:
  - Nueva fecha de fin (campo de fecha)
  - Días adicionales (número)
  - Número de documento (opcional)
  - Observaciones (opcional)
- Alerta indicando que requiere aprobación de RH
- Validaciones del formulario antes de enviar

##### Estados Agregados
```typescript
const [empleadosIncapacitados, setEmpleadosIncapacitados] = useState<RespuestaIncapacidad[]>([]);
const [showExtenderModal, setShowExtenderModal] = useState(false);
const [incapacidadAExtender, setIncapacidadAExtender] = useState<RespuestaIncapacidad | null>(null);
const [nuevaFechaFin, setNuevaFechaFin] = useState('');
const [diasAdicionales, setDiasAdicionales] = useState('');
const [numeroDocumento, setNumeroDocumento] = useState('');
const [observacionesExtension, setObservacionesExtension] = useState('');
```

### Base de Datos

#### Script SQL de Migración
**Archivo**: `add_extension_fields.sql`

Columnas agregadas a tabla `incapacidades`:
- `es_extension` (BOOLEAN): Flag de extensión
- `id_incapacidad_original` (BIGINT): FK a incapacidad original
- `fecha_fin_original` (DATE): Fecha fin antes de extensión
- `comentarios_extension` (TEXT): Comentarios de extensión

Índices creados:
- `idx_incapacidades_extension`: Para búsquedas de extensiones
- `idx_incapacidades_original`: Para relación con incapacidad original

## Flujo de Trabajo

### 1. Visualización de Empleados Incapacitados
1. Jefe ingresa a "Incapacidades Pendientes"
2. Sistema carga dos conjuntos de datos en paralelo:
   - Solicitudes pendientes de aprobación
   - Empleados actualmente incapacitados
3. Ambas listas se muestran en tarjetas separadas

### 2. Solicitud de Extensión
1. Jefe hace clic en "Extender" junto a un empleado incapacitado
2. Se abre modal mostrando:
   - Datos de la incapacidad actual
   - Formulario para extensión
3. Jefe completa:
   - Nueva fecha de fin (debe ser posterior a fecha fin actual)
   - Días adicionales
   - Opcionalmente: número de documento y observaciones
4. Al enviar:
   - Sistema valida datos
   - Crea nueva incapacidad marcada como extensión
   - Establece estado como `PENDIENTE_RH`
   - Mantiene referencia a incapacidad original
5. Jefe recibe confirmación
6. Lista se actualiza automáticamente

### 3. Aprobación por RH
1. Extensión aparece en vista de RH como solicitud pendiente
2. RH puede:
   - Aprobar: Extensión se activa
   - Rechazar: Extensión se rechaza, incapacidad original permanece sin cambios
3. Sistema registra aprobador RH y fecha de aprobación

## Validaciones

### Backend
- ✅ Solo incapacidades aprobadas pueden ser extendidas
- ✅ Nueva fecha fin debe ser posterior a fecha fin actual
- ✅ Jefe debe tener permiso sobre el departamento del empleado
- ✅ Días adicionales deben ser positivos
- ✅ Extensiones siempre van a estado `PENDIENTE_RH`

### Frontend
- ✅ Campos requeridos validados antes de envío
- ✅ Nueva fecha fin debe ser posterior a fecha actual de la incapacidad
- ✅ Días adicionales deben ser número positivo
- ✅ Alertas informativas sobre proceso de aprobación

## Consideraciones de Seguridad

1. **Autorización**: Solo jefes, HR y administradores pueden solicitar extensiones
2. **Validación de Permisos**: Se verifica que el jefe tenga permiso sobre el departamento
3. **Estado de Extensión**: Las extensiones van directo a RH (no requieren aprobación de jefe nuevamente)
4. **Trazabilidad**: Se mantiene referencia a incapacidad original y se registran comentarios

## Próximos Pasos Sugeridos

1. **Notificaciones por Email**: Enviar notificación a RH cuando se solicita una extensión
2. **Dashboard para RH**: Distinguir visualmente extensiones de solicitudes normales
3. **Historial**: Mostrar todas las extensiones de una incapacidad en la vista de detalles
4. **Reportes**: Incluir estadísticas de extensiones en reportes mensuales
5. **Límites**: Considerar agregar límites al número de extensiones permitidas

## Testing

### Casos de Prueba Backend
- [ ] Obtener empleados incapacitados del departamento
- [ ] Solicitar extensión de incapacidad aprobada
- [ ] Rechazar extensión de incapacidad no aprobada
- [ ] Validar que nueva fecha fin sea posterior
- [ ] Validar permisos de jefe sobre departamento
- [ ] Aprobar/rechazar extensión por RH

### Casos de Prueba Frontend
- [ ] Cargar empleados incapacitados correctamente
- [ ] Abrir modal de extensión con datos correctos
- [ ] Validar formulario de extensión
- [ ] Enviar solicitud de extensión exitosamente
- [ ] Manejar errores de API adecuadamente
- [ ] Actualizar listas después de crear extensión

## Notas Técnicas

- Las extensiones se crean como nuevas incapacidades en lugar de modificar la original
- Esto permite mejor auditoría y trazabilidad
- La fecha de inicio de la extensión es automáticamente el día siguiente a la fecha fin de la original
- Spring Boot crea automáticamente las columnas nuevas con `ddl-auto=update`
- Sin embargo, se recomienda ejecutar el script SQL en producción para tener control explícito
