const API_URL = 'http://localhost:8080/api';

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
      throw new Error('Sesión expirada. Por favor inicia sesión nuevamente.');
    }
    try {
      const err = await response.json();
      throw new Error(err.message || 'Error en la solicitud');
    } catch (e) {
      throw new Error(`Error HTTP ${response.status}`);
    }
  }
  return response.json();
}

export interface EmpleadoEvaluacionResumen {
  empleadoId: number;
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  puestoNombre: string;
  promedioPuntuacion: number | null;
  cantidadEvaluaciones: number;
}

export interface ResumenEvaluacionesDepartamento {
  departamentoId: number;
  departamentoNombre: string;
  empleados: EmpleadoEvaluacionResumen[];
}

export async function obtenerResumenDepartamento(idDepartamento: number): Promise<ResumenEvaluacionesDepartamento> {
  const response = await fetch(`${API_URL}/evaluaciones/resumen-departamento/${idDepartamento}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  return handleResponse<ResumenEvaluacionesDepartamento>(response);
}

export async function obtenerEmpleadosMisDepartamentos(): Promise<EmpleadoEvaluacionResumen[]> {
  const response = await fetch(`${API_URL}/evaluaciones/empleados-mis-departamentos`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  return handleResponse<EmpleadoEvaluacionResumen[]>(response);
}

export async function obtenerEvaluacionesPorEmpleado(idEmpleado: number): Promise<any[]> {
  const response = await fetch(`${API_URL}/evaluaciones/por-empleado/${idEmpleado}`, {
    method: 'GET',
    headers: getAuthHeaders(),
  });
  return handleResponse<any[]>(response);
}

export default {
  obtenerResumenDepartamento,
  obtenerEmpleadosMisDepartamentos,
};
