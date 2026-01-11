-- Seed script para días feriados de Costa Rica
-- Este script inserta los feriados comunes para el año 2026
-- Los administradores deben agregar manualmente los feriados cada año
-- ya que algunos feriados pueden moverse si caen en fin de semana

-- Limpiar feriados existentes (opcional, comentar si solo desea agregar)
-- TRUNCATE TABLE dias_feriados RESTART IDENTITY;

-- Feriados de Costa Rica 2026
INSERT INTO dias_feriados (nombre, fecha, descripcion) VALUES
('Año Nuevo', '2026-01-01', 'Celebración del inicio del año nuevo'),
('Día de Juan Santamaría', '2026-04-11', 'Conmemoración del héroe nacional Juan Santamaría'),
('Jueves Santo', '2026-04-02', 'Semana Santa - día de reflexión religiosa'),
('Viernes Santo', '2026-04-03', 'Semana Santa - conmemoración de la crucifixión'),
('Día del Trabajador', '2026-05-01', 'Día Internacional del Trabajo'),
('Anexión del Partido de Nicoya', '2026-07-25', 'Anexión de Guanacaste a Costa Rica'),
('Día de la Virgen de los Ángeles', '2026-08-02', 'Patrona de Costa Rica'),
('Día de la Madre', '2026-08-15', 'Celebración del Día de la Madre y Asunción de la Virgen María'),
('Día de la Independencia', '2026-09-15', 'Independencia de Costa Rica de España'),
('Día de las Culturas', '2026-10-12', 'Anteriormente Día de la Raza, celebra la diversidad cultural'),
('Navidad', '2026-12-25', 'Celebración de la Navidad');

-- Nota: Los feriados móviles como Semana Santa deben ser actualizados cada año
-- ya que las fechas cambian según el calendario litúrgico.
-- 
-- Para 2026, Semana Santa es:
-- - Jueves Santo: 2 de abril
-- - Viernes Santo: 3 de abril
--
-- Recuerde que si un feriado cae en fin de semana, 
-- la empresa puede decidir moverlo al viernes anterior o lunes siguiente.
-- En ese caso, elimine el feriado original y cree uno nuevo con la fecha movida.
