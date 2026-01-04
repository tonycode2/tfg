# Sistema de Manejo de Errores

Este paquete contiene el sistema centralizado de manejo de excepciones para la aplicación.

## Estructura

### Excepciones Personalizadas

- **ResourceNotFoundException**: Se lanza cuando un recurso no se encuentra en la base de datos
  - Uso: `throw new ResourceNotFoundException("Empleados", "id", 123)`
  
- **BadRequestException**: Se lanza cuando la solicitud del cliente contiene datos inválidos
  - Uso: `throw new BadRequestException("El formato de fecha es incorrecto")`
  
- **ConflictException**: Se lanza cuando hay conflictos con el estado actual del recurso
  - Uso: `throw new ConflictException("Ya existe un empleado con esa cédula")`
  
- **UnauthorizedException**: Se lanza cuando el usuario no está autenticado
  - Uso: `throw new UnauthorizedException("Token inválido o expirado")`
  
- **ForbiddenException**: Se lanza cuando el usuario no tiene permisos
  - Uso: `throw new ForbiddenException("No tienes permisos para acceder a este recurso")`

### DTOs de Respuesta de Error

- **ErrorResponse**: DTO principal para respuestas de error
  - Campos: timestamp, status, error, message, path, errors
  
- **ValidationError**: DTO para detalles de errores de validación
  - Campos: field, message, rejectedValue

### GlobalExceptionHandler

Manejador global de excepciones que captura todas las excepciones de la aplicación y devuelve respuestas consistentes.

#### Excepciones Manejadas:

1. **ResourceNotFoundException** → HTTP 404 (Not Found)
2. **MethodArgumentNotValidException** → HTTP 400 (Bad Request) con detalles de validación
3. **BadRequestException** → HTTP 400 (Bad Request)
4. **ConflictException** → HTTP 409 (Conflict)
5. **DataIntegrityViolationException** → HTTP 409 (Conflict)
6. **UnauthorizedException** → HTTP 401 (Unauthorized)
7. **ForbiddenException** → HTTP 403 (Forbidden)
8. **MethodArgumentTypeMismatchException** → HTTP 400 (Bad Request)
9. **IllegalArgumentException** → HTTP 400 (Bad Request)
10. **Exception** → HTTP 500 (Internal Server Error) para errores no contemplados

## Ejemplo de Respuesta de Error

### Error de Recurso No Encontrado

```json
{
  "timestamp": "2026-01-04T15:30:45",
  "status": 404,
  "error": "Not Found",
  "message": "Empleados no encontrado con id: '123'",
  "path": "/api/empleados/123"
}
```

### Error de Validación

```json
{
  "timestamp": "2026-01-04T15:30:45",
  "status": 400,
  "error": "Bad Request",
  "message": "Error de validación en los datos proporcionados",
  "path": "/api/empleados",
  "errors": [
    {
      "field": "nombre",
      "message": "El nombre no puede estar vacío",
      "rejectedValue": null
    },
    {
      "field": "salario",
      "message": "El salario debe ser mayor a 0",
      "rejectedValue": -1000
    }
  ]
}
```

## Uso en Servicios

Los servicios ahora lanzan excepciones en lugar de retornar `null`:

```java
public RespuestaEmpleadosDTO obtenerPorId(Long id) {
    Empleados empleado = consulta.obtenerPorId(id);
    if(empleado == null){
        throw new ResourceNotFoundException("Empleados", "id", id);
    }
    return deEntidadDtoARespuesta(empleado);
}
```

## Beneficios

1. **Consistencia**: Todas las respuestas de error siguen el mismo formato
2. **Centralización**: Un solo lugar para manejar excepciones
3. **Logging**: Todas las excepciones se registran automáticamente
4. **Claridad**: Mensajes de error descriptivos y códigos HTTP apropiados
5. **Validación**: Detalles específicos de campos con errores de validación
