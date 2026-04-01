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

export const descargarPdfAguinaldo = async (empleadoId: number, anio: number): Promise<void> => {
  const token = localStorage.getItem('token');
  const url = `http://localhost:8080/api/reportes/aguinaldo/${empleadoId}/${anio}`;
  
  try {
    const response = await fetch(url, {
      method: 'GET',
      headers: {
        'Authorization': `Bearer ${token}`,
      },
    });

    if (!response.ok) {
      throw new Error('Error al descargar el PDF');
    }

    const blob = await response.blob();
    const urlBlob = window.URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = urlBlob;
    link.download = `aguinaldo-${empleadoId}-${anio}.pdf`;
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    window.URL.revokeObjectURL(urlBlob);
  } catch (error) {
    console.error('Error descargando PDF:', error);
    throw error;
  }
};
