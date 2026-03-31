const API_URL = 'http://localhost:8080/api/aguinaldos';

export interface AguinaldoCalculado {
  id: number;
  idEmpleado: number;
  nombreEmpleado: string | null;
  primerApellidoEmpleado: string | null;
  segundoApellidoEmpleado: string | null;
  anio: number;
  fechaInicioPeriodo: string;
  fechaFinPeriodo: string;
  totalSalariosDevengados: number;
  montoAguinaldo: number;
  fechaCalculo: string;
  fechaPago: string | null;
}

const getAuthHeaders = (): HeadersInit => {
  const token = localStorage.getItem('token');
  return {
    'Authorization': `Bearer ${token}`,
    'Content-Type': 'application/json',
  };
};

const handleApiError = (error: { status: number; message: string }) => {
  if (error.status === 401) {
    localStorage.removeItem('token');
    localStorage.removeItem('userRole');
    window.location.href = '/';
  }
  throw new Error(error.message || 'Error en la solicitud');
};

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

  return response.json();
};

export const calcularAguinaldos = async (anio: number): Promise<AguinaldoCalculado[]> => {
  return fetchWithAuth<AguinaldoCalculado[]>(`${API_URL}/calcular`, {
    method: 'POST',
    body: JSON.stringify({ anio }),
  });
};

export const obtenerAguinaldosPorEmpleado = async (idEmpleado: number): Promise<AguinaldoCalculado[]> => {
  return fetchWithAuth<AguinaldoCalculado[]>(`${API_URL}/empleado/${idEmpleado}`);
};
