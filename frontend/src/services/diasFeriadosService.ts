const API_URL = 'http://localhost:8080/api/dias-feriados';

// ==================== INTERFACES ====================

export interface DiaFeriado {
  id?: number;
  nombre: string;
  fecha: string;
  descripcion?: string;
}

export interface ValidarFechaResponse {
  fecha: string;
  esFeriado: boolean;
}

// ==================== HELPER FUNCTIONS ====================

/**
 * Get authorization headers with JWT token
 */
const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('token');
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
};

/**
 * Handle API errors and redirect to login on 401
 */
const handleApiError = async (response: Response) => {
  if (response.status === 401) {
    console.log('Token expired or invalid, redirecting to login...');
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    window.location.href = '/';
    throw new Error('Sesión expirada');
  }
  
  const errorData = await response.json().catch(() => ({ message: response.statusText }));
  throw new Error(errorData.message || 'Error en la solicitud');
};

/**
 * Generic fetch helper
 */
const fetchWithAuth = async <T>(url: string, options: RequestInit = {}): Promise<T> => {
  const response = await fetch(url, {
    ...options,
    headers: {
      ...getAuthHeaders(),
      ...options.headers,
    },
  });

  if (!response.ok) {
    await handleApiError(response);
  }

  return response.json();
};

// ==================== API FUNCTIONS ====================

/**
 * Obtiene todos los días feriados
 */
export const obtenerTodosFeriados = async (): Promise<DiaFeriado[]> => {
  return fetchWithAuth<DiaFeriado[]>(API_URL);
};

/**
 * Obtiene un día feriado por su ID
 */
export const obtenerFeriadoPorId = async (id: number): Promise<DiaFeriado> => {
  return fetchWithAuth<DiaFeriado>(`${API_URL}/${id}`);
};

/**
 * Obtiene los feriados en un rango de fechas
 * @param fechaInicio Fecha de inicio en formato YYYY-MM-DD
 * @param fechaFin Fecha de fin en formato YYYY-MM-DD
 */
export const obtenerFeriadosEnRango = async (
  fechaInicio: string,
  fechaFin: string
): Promise<DiaFeriado[]> => {
  const params = new URLSearchParams({
    fechaInicio,
    fechaFin,
  });
  return fetchWithAuth<DiaFeriado[]>(`${API_URL}/rango?${params.toString()}`);
};

/**
 * Obtiene los feriados de un año específico
 */
export const obtenerFeriadosPorAnio = async (anio: number): Promise<DiaFeriado[]> => {
  return fetchWithAuth<DiaFeriado[]>(`${API_URL}/anio/${anio}`);
};

/**
 * Valida si una fecha específica es un día feriado
 * @param fecha Fecha en formato YYYY-MM-DD
 */
export const validarFecha = async (fecha: string): Promise<ValidarFechaResponse> => {
  const params = new URLSearchParams({ fecha });
  return fetchWithAuth<ValidarFechaResponse>(`${API_URL}/validar-fecha?${params.toString()}`);
};

/**
 * Crea un nuevo día feriado (solo HR/ADMIN)
 */
export const crearFeriado = async (feriado: Omit<DiaFeriado, 'id'>): Promise<DiaFeriado> => {
  return fetchWithAuth<DiaFeriado>(API_URL, {
    method: 'POST',
    body: JSON.stringify(feriado),
  });
};

/**
 * Actualiza un día feriado existente (solo HR/ADMIN)
 */
export const actualizarFeriado = async (id: number, feriado: DiaFeriado): Promise<DiaFeriado> => {
  return fetchWithAuth<DiaFeriado>(`${API_URL}/${id}`, {
    method: 'PUT',
    body: JSON.stringify(feriado),
  });
};

/**
 * Elimina un día feriado (solo HR/ADMIN)
 */
export const eliminarFeriado = async (id: number): Promise<void> => {
  const response = await fetch(`${API_URL}/${id}`, {
    method: 'DELETE',
    headers: getAuthHeaders(),
  });

  if (!response.ok) {
    await handleApiError(response);
  }
};

// ==================== UTILITY FUNCTIONS ====================

/**
 * Valida que no haya feriados en un rango de fechas.
 * Retorna los feriados encontrados (si hay alguno).
 * @param fechaInicio Fecha de inicio en formato YYYY-MM-DD
 * @param fechaFin Fecha de fin en formato YYYY-MM-DD
 * @returns Lista de feriados en el rango, vacía si no hay ninguno
 */
export const validarRangoSinFeriados = async (
  fechaInicio: string,
  fechaFin: string
): Promise<DiaFeriado[]> => {
  const feriados = await obtenerFeriadosEnRango(fechaInicio, fechaFin);
  return feriados;
};

/**
 * Formatea la fecha de un feriado para mostrar (DD/MM/YYYY)
 */
export const formatearFechaFeriado = (fecha: string): string => {
  const [year, month, day] = fecha.split('-');
  return `${day}/${month}/${year}`;
};
