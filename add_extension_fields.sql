-- Script para agregar campos de extensión a la tabla incapacidades
-- Fecha: 2026-01-20
-- Descripción: Agrega campos para manejar extensiones de incapacidades

-- Agregar columna para marcar si es una extensión
ALTER TABLE incapacidades 
ADD COLUMN IF NOT EXISTS es_extension BOOLEAN DEFAULT FALSE;

-- Agregar referencia a la incapacidad original (en caso de extensión)
ALTER TABLE incapacidades 
ADD COLUMN IF NOT EXISTS id_incapacidad_original BIGINT REFERENCES incapacidades(id);

-- Agregar columna para guardar la fecha fin original antes de la extensión
ALTER TABLE incapacidades 
ADD COLUMN IF NOT EXISTS fecha_fin_original DATE;

-- Agregar comentarios de la extensión
ALTER TABLE incapacidades 
ADD COLUMN IF NOT EXISTS comentarios_extension TEXT;

-- Crear índice para mejorar búsquedas de extensiones
CREATE INDEX IF NOT EXISTS idx_incapacidades_extension 
ON incapacidades(es_extension) 
WHERE es_extension = TRUE;

-- Crear índice para la relación con incapacidad original
CREATE INDEX IF NOT EXISTS idx_incapacidades_original 
ON incapacidades(id_incapacidad_original) 
WHERE id_incapacidad_original IS NOT NULL;

-- Comentarios en las columnas
COMMENT ON COLUMN incapacidades.es_extension IS 'Indica si esta incapacidad es una extensión de otra';
COMMENT ON COLUMN incapacidades.id_incapacidad_original IS 'ID de la incapacidad original si esta es una extensión';
COMMENT ON COLUMN incapacidades.fecha_fin_original IS 'Fecha fin de la incapacidad original antes de la extensión';
COMMENT ON COLUMN incapacidades.comentarios_extension IS 'Comentarios sobre la extensión de la incapacidad';

-- Verificar las nuevas columnas
SELECT column_name, data_type, is_nullable
FROM information_schema.columns
WHERE table_name = 'incapacidades'
  AND column_name IN ('es_extension', 'id_incapacidad_original', 'fecha_fin_original', 'comentarios_extension')
ORDER BY ordinal_position;
