-- Fix enum check constraints for horas_extra
ALTER TABLE horas_extra DROP CONSTRAINT IF EXISTS horas_extra_estado_solicitud_check;
ALTER TABLE horas_extra
  ADD CONSTRAINT horas_extra_estado_solicitud_check
  CHECK (estado_solicitud IN ('PENDIENTE','PENDIENTE_RH','APROBADA_POR_JEFE','RECHAZADA_POR_JEFE','RECHAZADA_POR_RH','APROBADA','CANCELADA'));

ALTER TABLE horas_extra DROP CONSTRAINT IF EXISTS horas_extra_tipo_tarifa_check;
ALTER TABLE horas_extra
  ADD CONSTRAINT horas_extra_tipo_tarifa_check
  CHECK (tipo_tarifa IN ('SIMPLE','DOBLE','TRIPLE'));

-- Optional: other tables with similar empty-check issues
ALTER TABLE permisos DROP CONSTRAINT IF EXISTS permisos_estado_solicitud_check;
ALTER TABLE permisos
  ADD CONSTRAINT permisos_estado_solicitud_check
  CHECK (estado_solicitud IN ('PENDIENTE','PENDIENTE_RH','APROBADA_POR_JEFE','RECHAZADA_POR_JEFE','RECHAZADA_POR_RH','APROBADA','CANCELADA'));

ALTER TABLE permisos DROP CONSTRAINT IF EXISTS permisos_tipo_permiso_check;
ALTER TABLE permisos
  ADD CONSTRAINT permisos_tipo_permiso_check
  CHECK (tipo_permiso IN ('PERSONAL','MEDICO','LUTO','MATERNIDAD','PATERNIDAD','ESTUDIO','SIN_GOCE_SALARIO','VACACIONES'));
