/**
 * Specialized service for attendance (asistencia) operations.
 * Provides clock-in/clock-out, status checking, and department summary methods.
 */

const API_URL = 'http://localhost:8080/api';

// ==================== INTERFACES ====================

/**
 * Attendance record returned from backend
 */
export interface Asistencia {
  id?: number;
  tipoEvento: 'ENTRADA' | 'SALIDA';
  fechaHora: string;
  observaciones?: string;
  idEmpleado: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
  segundoApellidoEmpleado?: string;
}

/**
 * Current attendance status for an employee
 */
export interface EstadoAsistencia {
  empleadoId: number;
  nombreCompleto: string;
  departamentoNombre: string;
  puestoNombre: string;
  ultimoEvento: 'ENTRADA' | 'SALIDA' | null;
  fechaHoraUltimoEvento: string | null;
  estadoActual: 'LABORANDO' | 'FUERA';
  observaciones: string | null;
  horaEntradaHoy: string | null;
  horaSalidaHoy: string | null;
}

/**
 * Department attendance summary
 */
export interface ResumenDepartamento {
  departamentoId: number;
  departamentoNombre: string;
  totalEmpleados: number;
  empleadosLaborando: number;
  empleadosFuera: number;
  empleados: EstadoAsistencia[];
}

/**
 * Request body for clock-in/clock-out (optional custom time for testing)
 */
export interface SolicitudRegistroAsistencia {
  fechaHora?: string; // Format: "yyyy-MM-dd HH:mm:ss"
}

// ==================== HELPER FUNCTIONS ====================

function getAuthHeaders(): HeadersInit {
  const token = localStorage.getItem('token');
  return {
    'Content-Type': 'application/json',
    ...(token && { Authorization: `Bearer ${token}` }),
  };
}

async function handleResponse<T>(response: Response): Promise<T> {
  if (!response.ok) {
    if (response.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login';
      throw new Error('Sesión expirada. Por favor, inicia sesión nuevamente.');
    }
    
    try {
      const errorData = await response.json();
      throw new Error(errorData.message || 'Error en la solicitud');
    } catch (error) {
      if (error instanceof Error && error.message !== 'Error en la solicitud') {
        throw error;
      }
      throw new Error(`Error HTTP ${response.status}: ${response.statusText}`);
    }
  }
  
  // Handle 204 No Content
  if (response.status === 204) {
    return {} as T;
  }
  
  return response.json();
}

// ==================== SERVICE METHODS ====================

/**
 * Register clock-in (ENTRADA) for the authenticated user
 * @param fechaHora Optional custom date/time for testing (format: "yyyy-MM-dd HH:mm:ss")
 */
export async function marcarEntrada(fechaHora?: string): Promise<Asistencia> {
  const body: SolicitudRegistroAsistencia | undefined = fechaHora ? { fechaHora } : undefined;
  
  const response = await fetch(`${API_URL}/asistencias/entrada`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  });
  
  return handleResponse<Asistencia>(response);
}

/**
 * Register clock-out (SALIDA) for the authenticated user
 * @param fechaHora Optional custom date/time for testing (format: "yyyy-MM-dd HH:mm:ss")
 */
export async function marcarSalida(fechaHora?: string): Promise<Asistencia> {
  const body: SolicitudRegistroAsistencia | undefined = fechaHora ? { fechaHora } : undefined;
  
  const response = await fetch(`${API_URL}/asistencias/salida`, {
    method: 'POST',
    headers: getAuthHeaders(),
    body: body ? JSON.stringify(body) : undefined,
  });
  
  return handleResponse<Asistencia>(response);
}

/**
 * Get current attendance status for the authenticated user
 */
export async function obtenerMiEstado(): Promise<EstadoAsistencia> {
  const response = await fetch(`${API_URL}/asistencias/mi-estado`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  
  return handleResponse<EstadoAsistencia>(response);
}

/**
 * Get list of department IDs that the current user can access
 */
export async function obtenerDepartamentosAccesibles(): Promise<number[]> {
  const response = await fetch(`${API_URL}/asistencias/departamentos-accesibles`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  
  return handleResponse<number[]>(response);
}

/**
 * Get attendance summary for a department
 * @param idDepartamento Department ID
 * @param fecha Optional date to get summary for (format: "yyyy-MM-dd"), defaults to today
 */
export async function obtenerResumenDepartamento(idDepartamento: number, fecha?: string): Promise<ResumenDepartamento> {
  const params = new URLSearchParams();
  
  if (fecha) {
    params.append('fecha', fecha);
  }
  
  const queryString = params.toString();
  const url = `${API_URL}/asistencias/departamento/${idDepartamento}${queryString ? `?${queryString}` : ''}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  
  return handleResponse<ResumenDepartamento>(response);
}

/**
 * Get attendance history with optional filters
 * @param idEmpleado Optional employee ID (null for own records)
 * @param fechaInicio Optional start date (format: "yyyy-MM-dd HH:mm:ss")
 * @param fechaFin Optional end date (format: "yyyy-MM-dd HH:mm:ss")
 */
export async function obtenerHistorial(
  idEmpleado?: number,
  fechaInicio?: string,
  fechaFin?: string
): Promise<Asistencia[]> {
  const params = new URLSearchParams();
  
  if (idEmpleado) {
    params.append('idEmpleado', idEmpleado.toString());
  }
  if (fechaInicio) {
    params.append('fechaInicio', fechaInicio);
  }
  if (fechaFin) {
    params.append('fechaFin', fechaFin);
  }
  
  const queryString = params.toString();
  const url = `${API_URL}/asistencias/historial${queryString ? `?${queryString}` : ''}`;
  
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  
  return handleResponse<Asistencia[]>(response);
}

/**
 * Format a Date to backend datetime format
 * @param date Date object
 * @returns String in "yyyy-MM-dd HH:mm:ss" format
 */
export function formatDateTimeForBackend(date: Date): string {
  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');
  
  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
}

/**
 * Combine date string and time string into backend datetime format
 * @param dateStr Date string in "yyyy-MM-dd" format
 * @param timeStr Time string in "HH:mm" format
 * @returns String in "yyyy-MM-dd HH:mm:ss" format
 */
export function combineDateAndTime(dateStr: string, timeStr: string): string {
  return `${dateStr} ${timeStr}:00`;
}

/**
 * Get current date in "yyyy-MM-dd" format
 */
export function getCurrentDateString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  const day = String(now.getDate()).padStart(2, '0');
  return `${year}-${month}-${day}`;
}

/**
 * Get current time in "HH:mm" format
 */
export function getCurrentTimeString(): string {
  const now = new Date();
  const hours = String(now.getHours()).padStart(2, '0');
  const minutes = String(now.getMinutes()).padStart(2, '0');
  return `${hours}:${minutes}`;
}

/**
 * Get start of current month in backend datetime format
 */
export function getStartOfMonthString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = String(now.getMonth() + 1).padStart(2, '0');
  return `${year}-${month}-01 00:00:00`;
}

/**
 * Get end of current month in backend datetime format
 */
export function getEndOfMonthString(): string {
  const now = new Date();
  const year = now.getFullYear();
  const month = now.getMonth();
  const lastDay = new Date(year, month + 1, 0).getDate();
  const monthStr = String(month + 1).padStart(2, '0');
  return `${year}-${monthStr}-${lastDay} 23:59:59`;
}

/**
 * Get preview of daily work record (jornada diaria) before clock-out
 * @param fechaHoraSalida - Optional ISO timestamp for clock-out time (defaults to current time on backend)
 */
export async function obtenerPreviewJornadaDiaria(fechaHoraSalida?: string): Promise<{
  fecha: string;
  horaEntrada: string;
  horaSalida: string;
  horasRegulares: number;
  horasExtra: number;
  observaciones: string;
  idEmpleado: number;
  nombreCompleto: string;
}> {
  const url = fechaHoraSalida 
    ? `${API_URL}/jornada-diaria/preview?fechaHoraSalida=${encodeURIComponent(fechaHoraSalida)}`
    : `${API_URL}/jornada-diaria/preview`;
    
  const response = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  
  return handleResponse(response);
}
