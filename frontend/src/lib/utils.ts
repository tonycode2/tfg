import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
}

// Parse dates preserving yyyy-MM-dd as local to avoid timezone shifts when displaying
function parseDatePreserveLocal(value: string | Date | null | undefined): Date | null {
  if (!value) return null;
  if (value instanceof Date) return value;

  if (/^\d{4}-\d{2}-\d{2}$/.test(value)) {
    const [year, month, day] = value.split('-').map(Number);
    return new Date(year, month - 1, day);
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
}

// ==================== PERMISOS UTILITIES ====================

/**
 * Obtiene el color de fondo y texto para un estado de permiso
 */
export function getEstadoPermisoColor(estado: string): string {
  switch(estado) {
    case 'PENDIENTE':
      return 'bg-yellow-100 text-yellow-800 dark:bg-yellow-900 dark:text-yellow-300';
    case 'PENDIENTE_RH':
      return 'bg-blue-100 text-blue-800 dark:bg-blue-900 dark:text-blue-300';
    case 'APROBADA_POR_JEFE':
      return 'bg-cyan-100 text-cyan-800 dark:bg-cyan-900 dark:text-cyan-300';
    case 'APROBADA':
      return 'bg-green-100 text-green-800 dark:bg-green-900 dark:text-green-300';
    case 'RECHAZADA_POR_JEFE':
    case 'RECHAZADA_POR_RH':
      return 'bg-red-100 text-red-800 dark:bg-red-900 dark:text-red-300';
    case 'CANCELADA':
      return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
    default:
      return 'bg-gray-100 text-gray-800 dark:bg-gray-700 dark:text-gray-300';
  }
}

/**
 * Obtiene la etiqueta amigable para un estado de permiso
 */
export function getEstadoPermisoLabel(estado: string): string {
  const labels: Record<string, string> = {
    'PENDIENTE': 'Pendiente de Aprobación',
    'PENDIENTE_RH': 'Pendiente RH',
    'APROBADA_POR_JEFE': 'Aprobada por Jefe',
    'APROBADA': 'Aprobada',
    'RECHAZADA_POR_JEFE': 'Rechazada por Jefe',
    'RECHAZADA_POR_RH': 'Rechazada por RH',
    'CANCELADA': 'Cancelada',
  };
  return labels[estado] || estado;
}

/**
 * Obtiene la etiqueta amigable para un tipo de permiso
 */
export function getTipoPermisoLabel(tipo: string): string {
  const labels: Record<string, string> = {
    'PERSONAL': 'Personal',
    'MEDICO': 'Médico',
    'LUTO': 'Luto',
    'MATERNIDAD': 'Maternidad',
    'PATERNIDAD': 'Paternidad',
    'ESTUDIO': 'Estudio',
    'SIN_GOCE_SALARIO': 'Sin Goce de Salario',
    'VACACIONES': 'Vacaciones',
  };
  return labels[tipo] || tipo;
}

/**
 * Calcula días hábiles entre dos fechas (excluye sábados y domingos)
 * @param fechaInicio - Fecha en formato yyyy-MM-dd
 * @param fechaFin - Fecha en formato yyyy-MM-dd
 * @returns Número de días hábiles (incluye ambos días)
 */
export function calcularDiasHabiles(fechaInicio: string, fechaFin: string): number {
  const inicio = parseDatePreserveLocal(fechaInicio);
  const fin = parseDatePreserveLocal(fechaFin);
  
  if (!inicio || !fin || fin < inicio) {
    return 0;
  }
  
  let diasHabiles = 0;
  const fecha = new Date(inicio);
  
  while (fecha <= fin) {
    const diaSemana = fecha.getDay();
    // 0 = Domingo, 6 = Sábado
    if (diaSemana !== 0 && diaSemana !== 6) {
      diasHabiles++;
    }
    fecha.setDate(fecha.getDate() + 1);
  }
  
  return diasHabiles;
}

/**
 * Formatea una fecha para mostrarla al usuario
 * @param fecha - Fecha en formato yyyy-MM-dd o Date
 * @returns Fecha en formato dd/MM/yyyy
 */
export function formatearFecha(fecha: string | Date | null | undefined): string {
  if (!fecha) return 'N/A';
  
  const date = parseDatePreserveLocal(fecha);
  if (!date) return 'N/A';

  const dia = String(date.getDate()).padStart(2, '0');
  const mes = String(date.getMonth() + 1).padStart(2, '0');
  const año = date.getFullYear();
  
  return `${dia}/${mes}/${año}`;
}

/**
 * Calcula el total de horas entre dos horas en formato HH:mm
 * @param horaInicio - Hora en formato HH:mm (ejemplo: "08:00")
 * @param horaFin - Hora en formato HH:mm (ejemplo: "17:30")
 * @returns Total de horas (ejemplo: 9.5)
 */
export function calcularHoras(horaInicio: string, horaFin: string): number {
  try {
    const [horaIni, minIni] = horaInicio.split(':').map(Number);
    const [horaFi, minFi] = horaFin.split(':').map(Number);
    
    const minutosInicio = horaIni * 60 + minIni;
    const minutosFin = horaFi * 60 + minFi;
    
    const diferenciaMinutos = minutosFin - minutosInicio;
    return Math.max(0, diferenciaMinutos / 60);
  } catch (e) {
    return 0;
  }
}

/**
 * Formatea horas en formato legible
 * @param horas - Total de horas (ejemplo: 9.5)
 * @returns String formateado (ejemplo: "9h 30min" o "8h")
 */
export function formatearHoras(horas: number): string {
  const horasEnteras = Math.floor(horas);
  const minutos = Math.round((horas - horasEnteras) * 60);
  
  if (minutos === 0) {
    return `${horasEnteras}h`;
  }
  return `${horasEnteras}h ${minutos}min`;
}

/**
 * Extrae el nombre de archivo del header Content-Disposition.
 * Soporta tanto `filename="..."` como `filename*=UTF-8''...` (RFC 5987).
 */
export function parseContentDispositionFilename(disp: string | null | undefined): string | null {
  if (!disp) return null;
  // filename*=UTF-8''encoded
  const star = /filename\*=UTF-8''([^;\n\r]+)/i.exec(disp);
  if (star && star[1]) {
    try {
      return decodeURIComponent(star[1].trim().replace(/^"|"$/g, ''));
    } catch (e) {
      return star[1].trim().replace(/^"|"$/g, '');
    }
  }
  // filename="..." or filename=...
  const match = /filename=\"?([^\";]+)\"?/i.exec(disp);
  if (match && match[1]) return match[1].trim();
  return null;
}

/**
 * Construye un nombre de archivo seguro para una incapacidad.
 * Ejemplo: "Incapacidad id 123 Juan Perez.pdf"
 */
export function buildIncapacidadFilename(id?: number | string | null, nombre?: string | null, primerApellido?: string | null, extension?: string | null): string {
  const idPart = id ? `id ${id}` : '';
  const namePart = [nombre || '', primerApellido || ''].join(' ').trim();
  let base = ['Incapacidad', idPart, namePart].filter(Boolean).join(' ').trim();
  // Mantener letras, números, espacios, guiones, guion bajo, punto
  base = base.replace(/[^\p{L}\p{N} _.-]+/gu, '').replace(/\s+/g, ' ').trim();
  if (!base) base = `Incapacidad_${id ?? ''}`;
  const ext = extension && extension.startsWith('.') ? extension : (extension ? `.${extension}` : '');
  return `${base}${ext}`;
}

/**
 * Formatea un número como moneda local (por defecto Costa Rica - CRC).
 * Devuelve una cadena legible con símbolo de moneda.
 */
export function formatCurrency(value: number | null | undefined, currency = 'CRC', locale = 'es-CR'): string {
  const numero = typeof value === 'number' && !Number.isNaN(value) ? value : 0;
  try {
    return new Intl.NumberFormat(locale, { style: 'currency', currency, maximumFractionDigits: 2 }).format(numero);
  } catch (e) {
    // Fallback simple
    return `${numero.toFixed(2)}`;
  }
}
// ==================== DATE VALIDATION UTILITIES ====================

/**
 * Retorna función para validar fechas de nacimiento (mayores de 18 años)
 * Solo permite fechas de hace 18+ años
 */
export function getDateFilterBirthdate(): (date: Date) => boolean {
  return (date: Date) => {
    const today = new Date();
    const age = today.getFullYear() - date.getFullYear();
    const monthDiff = today.getMonth() - date.getMonth();
    const dayDiff = today.getDate() - date.getDate();
    
    const isAtLeast18 = age > 18 || (age === 18 && monthDiff > 0) || (age === 18 && monthDiff === 0 && dayDiff >= 0);
    return isAtLeast18;
  };
}

/**
 * Retorna función para validar fechas de horas extra (solo hoy)
 */
export function getDateFilterHorasExtra(): (date: Date) => boolean {
  return (date: Date) => {
    const today = new Date();

    // Normalizar fechas a medianoche para comparación
    const dateNormalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const todayNormalized = new Date(today.getFullYear(), today.getMonth(), today.getDate());

    return dateNormalized.getTime() === todayNormalized.getTime();
  };
}

/**
 * Retorna función para validar fechas de inicio de incapacidades (hoy, ayer y anteayer)
 */
export function getDateFilterIncapacidadInicio(): (date: Date) => boolean {
  return (date: Date) => {
    const today = new Date();
    const yesterday = new Date(today);
    yesterday.setDate(yesterday.getDate() - 1);
    const twoDaysAgo = new Date(today);
    twoDaysAgo.setDate(twoDaysAgo.getDate() - 2);
    
    const dateNormalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const todayNormalized = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const yesterdayNormalized = new Date(yesterday.getFullYear(), yesterday.getMonth(), yesterday.getDate());
    const twoDaysAgoNormalized = new Date(twoDaysAgo.getFullYear(), twoDaysAgo.getMonth(), twoDaysAgo.getDate());
    
    return dateNormalized.getTime() === todayNormalized.getTime() || 
          dateNormalized.getTime() === yesterdayNormalized.getTime() ||
          dateNormalized.getTime() === twoDaysAgoNormalized.getTime();
  };
}

/**
 * Retorna función para validar fechas de fin de incapacidades (desde fecha inicio a futuro)
 * @param fechaInicio - Fecha de inicio en formato yyyy-MM-dd
 */
export function getDateFilterIncapacidadFin(fechaInicio: string): (date: Date) => boolean {
  return (date: Date) => {
    if (!fechaInicio) return true; // Si no hay fecha inicio, permitir cualquier fecha
    
    const inicio = parseDatePreserveLocal(fechaInicio);
    if (!inicio) return true;
    
    const dateNormalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const inicioNormalized = new Date(inicio.getFullYear(), inicio.getMonth(), inicio.getDate());
    
    return dateNormalized.getTime() >= inicioNormalized.getTime();
  };
}

/**
 * Retorna función para validar fechas sin fechas pasadas (permisos, vacaciones, etc)
 */
export function getDateFilterNoPassedDates(): (date: Date) => boolean {
  return (date: Date) => {
    const today = new Date();
    const dateNormalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const todayNormalized = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    
    return dateNormalized.getTime() >= todayNormalized.getTime();
  };
}

/**
 * Retorna función para validar fechas desde mañana (sin permitir hoy ni pasadas)
 */
export function getDateFilterFromTomorrow(): (date: Date) => boolean {
  return (date: Date) => {
    const tomorrow = new Date();
    tomorrow.setDate(tomorrow.getDate() + 1);

    const dateNormalized = new Date(date.getFullYear(), date.getMonth(), date.getDate());
    const tomorrowNormalized = new Date(tomorrow.getFullYear(), tomorrow.getMonth(), tomorrow.getDate());

    return dateNormalized.getTime() >= tomorrowNormalized.getTime();
  };
}