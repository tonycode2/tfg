# Solución al Error 409: Violación de Constraint en Vacaciones

## Problema Identificado

El error ocurre porque la tabla `permisos` en PostgreSQL tiene un CHECK CONSTRAINT llamado `permisos_tipo_permiso_check` que **no incluye el valor 'VACACIONES'**.

### Error del Log:
```
ERROR: new row for relation "permisos" violates check constraint "permisos_tipo_permiso_check"
Detail: Failing row contains (..., VACACIONES, ...)
```

## Causa Raíz

La tabla `permisos` fue creada por Hibernate cuando el enum `TipoPermiso` no incluía `VACACIONES`. Después se agregó `VACACIONES` al código Java, pero el constraint de la base de datos nunca se actualizó.

## Solución

Ejecutar estos comandos SQL en tu base de datos PostgreSQL:

### Opción 1: Usando Docker (Recomendado)

Abre PowerShell y ejecuta:

```powershell
# 1. Ver los contenedores activos
docker ps

# 2. Ejecutar el SQL para eliminar el constraint viejo
docker exec -it <CONTAINER_ID> psql -U tony -d tfg -c "ALTER TABLE permisos DROP CONSTRAINT IF EXISTS permisos_tipo_permiso_check;"

# 3. Agregar el nuevo constraint con VACACIONES incluido
docker exec -it <CONTAINER_ID> psql -U tony -d tfg -c "ALTER TABLE permisos ADD CONSTRAINT permisos_tipo_permiso_check CHECK (tipo_permiso IN ('PERSONAL','MEDICO','LUTO','MATERNIDAD','PATERNIDAD','ESTUDIO','SIN_GOCE_SALARIO','VACACIONES'));"

# 4. Verificar que se aplicó correctamente
docker exec -it <CONTAINER_ID> psql -U tony -d tfg -c "SELECT conname, pg_get_constraintdef(oid) FROM pg_constraint WHERE conname = 'permisos_tipo_permiso_check';"
```

Reemplaza `<CONTAINER_ID>` con el ID del contenedor de PostgreSQL (puedes obtenerlo con `docker ps`).

### Opción 2: Usando el archivo SQL

Si prefieres usar el archivo SQL que generé:

```powershell
# 1. Copiar el archivo al contenedor
docker cp "c:\Users\aalva\OneDrive\Documentos\Codigo\tfg\fix_vacaciones_constraint.sql" <CONTAINER_ID>:/tmp/fix.sql

# 2. Ejecutar el script
docker exec -it <CONTAINER_ID> psql -U tony -d tfg -f /tmp/fix.sql
```

### Opción 3: Usando DBeaver o pgAdmin

1. Conecta a la base de datos `tfg` (localhost:5432, usuario: tony, password: gersonandre)
2. Abre una nueva ventana SQL
3. Ejecuta estos comandos:

```sql
-- Eliminar el constraint viejo
ALTER TABLE permisos 
DROP CONSTRAINT IF EXISTS permisos_tipo_permiso_check;

-- Agregar el constraint actualizado
ALTER TABLE permisos 
ADD CONSTRAINT permisos_tipo_permiso_check 
CHECK (tipo_permiso IN (
    'PERSONAL',
    'MEDICO',
    'LUTO',
    'MATERNIDAD',
    'PATERNIDAD',
    'ESTUDIO',
    'SIN_GOCE_SALARIO',
    'VACACIONES'
));
```

## Verificación

Después de aplicar la solución, intenta crear una nueva solicitud de vacaciones. El error 409 debe desaparecer.

## Prevención Futura

Para evitar este problema en el futuro, actualiza el archivo `init_clean.sql` para incluir el constraint correcto al crear la tabla `permisos`. Busca la línea:

```sql
CREATE TABLE permisos (
    ...
    tipo_permiso varchar(255),
    ...
);
```

Y agregar después de crear la tabla:

```sql
ALTER TABLE permisos 
ADD CONSTRAINT permisos_tipo_permiso_check 
CHECK (tipo_permiso IN (
    'PERSONAL','MEDICO','LUTO','MATERNIDAD','PATERNIDAD','ESTUDIO','SIN_GOCE_SALARIO','VACACIONES'
));
```

## Alternativa: Recrear la Base de Datos

Si prefieres empezar desde cero (esto **borrará todos los datos**):

```powershell
# 1. Detener el backend
# 2. Detener Docker Compose
docker compose down -v

# 3. Iniciar de nuevo
docker compose up -d

# 4. Iniciar el backend
```

Hibernate recreará las tablas con los constraints correctos basándose en la versión actual del código.
