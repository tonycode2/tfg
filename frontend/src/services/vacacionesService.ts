const API_URL = 'http://localhost:8080/api/vacaciones';

// ==================== INTERFACES ====================

export interface SaldoVacaciones {
  diasDisponibles: number;
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
const handleApiError = (error: { status: number; message: string }) => {
  if (error.status === 401) {
    console.log('Token expired or invalid, redirecting to login...');
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    window.location.href = '/';
  }
  throw error;
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
    const error = { status: response.status, message: await response.text() };
    handleApiError(error);
  }

  return await response.json();
};

// ==================== API FUNCTIONS ====================

/**
 * Obtiene el saldo de vacaciones del empleado autenticado
 */
export const obtenerMiSaldo = async (): Promise<SaldoVacaciones> => {
  return fetchWithAuth<SaldoVacaciones>(`${API_URL}/mi-saldo`);
};

/**
 * Obtiene el saldo de vacaciones de un empleado específico
 * Solo HR, ADMIN o jefes del departamento del empleado pueden consultar
 */
export const obtenerSaldoEmpleado = async (idEmpleado: number): Promise<SaldoVacaciones> => {
  return fetchWithAuth<SaldoVacaciones>(`${API_URL}/saldo/${idEmpleado}`);
};

/**
 * Ejecuta manualmente la acumulación de 1 día de vacaciones para todos los empleados activos
 * Solo HR y ADMIN pueden ejecutar esta acción
 */
export const ejecutarAcumulacionManual = async (): Promise<{ message: string; status: string }> => {
  return fetchWithAuth<{ message: string; status: string }>(`${API_URL}/acumular-manual`, {
    method: 'POST',
  });
};
