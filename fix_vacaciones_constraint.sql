-- Fix para el CHECK CONSTRAINT de tipo_permiso en la tabla permisos
-- El constraint actual no incluye 'VACACIONES', lo que causa error 409

-- 1. Eliminar el constraint existente
ALTER TABLE permisos 
DROP CONSTRAINT IF EXISTS permisos_tipo_permiso_check;

-- 2. Agregar el constraint actualizado con VACACIONES incluido
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

-- Verificar que el constraint se aplicó correctamente
SELECT conname, pg_get_constraintdef(oid) 
FROM pg_constraint 
WHERE conname = 'permisos_tipo_permiso_check';
