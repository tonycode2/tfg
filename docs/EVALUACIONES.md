# Evaluación de Desempeño

Resumen de la implementación y endpoints añadidos para el módulo de Evaluación de Desempeño.

Principales decisiones:
- El correo que recibe el empleado **solo** contiene el resumen de la evaluación (puntuación, periodo, observaciones y plan de mejora).
- Los empleados **no** pueden ver sus evaluaciones en la UI; solo reciben notificación por correo.
- Solo `JEFE`, `HR` y `ADMIN` pueden crear evaluaciones. `JEFE` solo puede evaluar empleados de los departamentos que administra.

Endpoints:
- `GET /api/evaluaciones/resumen-departamento/{idDepartamento}`
  - Devuelve un resumen por empleado (promedio, cantidad) del departamento.
  - Roles permitidos: `HR`, `ADMIN`, `JEFE` (si gestiona el departamento).

- `GET /api/evaluaciones/empleados-mis-departamentos`
  - Devuelve lista de empleados (resumen) que pertenecen a los departamentos accesibles para el usuario.

- `POST /api/evaluaciones` (existente)
  - Crea una evaluación y envía notificación por correo al empleado.
  - Request body: `SolicitudEvaluacionDeDesempenoDTO` (fechaEvaluacion, periodoEvaluado, puntuacionFinal, observaciones, planDeMejora, idEmpleado).

Correo:
- Implementado en `ServicioEmail.enviarNotificacionEvaluacion(...)`.
- Contenido: resumen con `nombre completo`, `periodo`, `puntuacion final`, `observaciones` y `plan de mejora`.

Pruebas:
- Se añadieron pruebas unitarias (Mockito) para `ServicioEvaluacion` en `src/test/.../ServicioEvaluacionTest.java`:
  - Caso exitoso: usuario con rol `HR` guarda evaluación y se verifica envío de correo.
  - Caso prohibido: `JEFE` que no administra el departamento recibe `ForbiddenException`.

Notas sobre ejecución de tests:
- El proyecto de pruebas de integración requiere PostgreSQL en `localhost:5432` para algunas pruebas de contexto; si no está disponible, ejecutar `mvnw -DskipTests=true package` o configurar un contenedor de tests.
