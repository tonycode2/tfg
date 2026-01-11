import { clsx, type ClassValue } from "clsx"
import { twMerge } from "tailwind-merge"

export function cn(...inputs: ClassValue[]) {
  return twMerge(clsx(inputs))
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
  const inicio = new Date(fechaInicio);
  const fin = new Date(fechaFin);
  
  if (fin < inicio) {
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
  
  const date = typeof fecha === 'string' ? new Date(fecha) : fecha;
  const dia = String(date.getDate()).padStart(2, '0');
  const mes = String(date.getMonth() + 1).padStart(2, '0');
  const año = date.getFullYear();
  
  return `${dia}/${mes}/${año}`;
}

