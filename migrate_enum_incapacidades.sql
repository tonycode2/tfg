-- Migration to update TipoIncapacidad values after enum change
-- Run this on the database before starting the backend to avoid enum mapping errors.

BEGIN;

-- Rename existing MATERNIDAD values to LICENCIA_DE_MATERNIDAD
UPDATE incapacidades
SET tipo_incapacidad = 'LICENCIA_DE_MATERNIDAD'
WHERE tipo_incapacidad = 'MATERNIDAD';

-- Map removed types to a fallback value (ENFERMEDAD_COMUN) to preserve historical records
UPDATE incapacidades
SET tipo_incapacidad = 'ENFERMEDAD_COMUN'
WHERE tipo_incapacidad IN ('RIESGO_EMBARAZO', 'ENFERMEDAD_PROFESIONAL');

COMMIT;

-- NOTE: Adjust the mapping above if you prefer a different fallback or to preserve these
-- historical values elsewhere. Backup your DB before running this script.
