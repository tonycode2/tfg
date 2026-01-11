const API_URL = 'http://localhost:8080/api';

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export interface ErrorResponse {
  timestamp: string;
  status: number;
  error: string;
  message: string;
  path: string;
  errors?: ValidationError[];
}

export interface ValidationError {
  field: string;
  message: string;
  rejectedValue: unknown;
}

/**
 * Servicio genérico para operaciones CRUD sobre recursos de la API.
 * Implementa manejo centralizado de autenticación, errores y logging.
 * Incluye soporte para cancelación de peticiones mediante AbortController.
 */
export class ApiService<T> {
  private readonly endpoint: string;

  constructor(endpoint: string) {
    this.endpoint = endpoint;
  }

  private getAuthHeaders(): HeadersInit {
    const token = localStorage.getItem('token');
    return {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  }

  private async handleResponse<R>(response: Response): Promise<R> {
    if (!response.ok) {
      // Si el token expiró (401 Unauthorized), cerrar sesión automáticamente
      if (response.status === 401) {
        localStorage.removeItem('token');
        window.location.href = '/login';
        throw new Error('Sesión expirada. Por favor, inicia sesión nuevamente.');
      }
      
      try {
        const errorData: ErrorResponse = await response.json();
        throw new Error(errorData.message || 'Error en la solicitud');
      } catch (error) {
        if (error instanceof Error && error.message !== 'Error en la solicitud') {
          throw error;
        }
        throw new Error(`Error HTTP ${response.status}: ${response.statusText}`);
      }
    }
    return response.json();
  }

  async getAll(page: number = 0, size: number = 10, signal?: AbortSignal): Promise<PaginatedResponse<T>> {
    const response = await fetch(
      `${API_URL}/${this.endpoint}?page=${page}&size=${size}`,
      {
        method: 'GET',
        headers: this.getAuthHeaders(),
        signal,
      }
    );
    return this.handleResponse<PaginatedResponse<T>>(response);
  }

  async getAllUnpaginated(signal?: AbortSignal): Promise<T[]> {
    const response = await fetch(
      `${API_URL}/${this.endpoint}`,
      {
        method: 'GET',
        headers: this.getAuthHeaders(),
        signal,
      }
    );
    return this.handleResponse<T[]>(response);
  }

  async getById(id: number | string, signal?: AbortSignal): Promise<T> {
    const response = await fetch(`${API_URL}/${this.endpoint}/${id}`, {
      method: 'GET',
      headers: this.getAuthHeaders(),
      signal,
    });
    return this.handleResponse<T>(response);
  }

  async create(data: Partial<T>, signal?: AbortSignal): Promise<T> {
    const response = await fetch(`${API_URL}/${this.endpoint}`, {
      method: 'POST',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(data),
      signal,
    });
    return this.handleResponse<T>(response);
  }

  async update(id: number | string, data: Partial<T>, signal?: AbortSignal): Promise<T> {
    const response = await fetch(`${API_URL}/${this.endpoint}/${id}`, {
      method: 'PUT',
      headers: this.getAuthHeaders(),
      body: JSON.stringify(data),
      signal,
    });
    return this.handleResponse<T>(response);
  }

  async delete(id: number | string, signal?: AbortSignal): Promise<void> {
    const response = await fetch(`${API_URL}/${this.endpoint}/${id}`, {
      method: 'DELETE',
      headers: this.getAuthHeaders(),
      signal,
    });
    
    if (!response.ok) {
      const errorData: ErrorResponse = await response.json();
      throw new Error(errorData.message || 'Error al eliminar');
    }
  }
}

// Interfaces para las entidades principales
export interface Empleado {
  id: number;
  cedula: string;
  nombre: string;
  primerApellido: string;
  segundoApellido: string;
  correoPersonal: string;
  fechaNacimiento: string;
  fechaIngreso: string;
  salarioBase: number;
  cantidadDeHijos: number;
  saldoVacaciones: number;
  cuentaIban?: string;
  estaActivo: boolean;
  estaCasado: boolean;
  tipoDeJornada: string;
  puesto: {
    id: number;
    nombre: string;
    salarioMinimo: number;
    departamento?: {
      id: number;
      nombre: string;
    };
  };
  direccion?: {
    id: number;
    provincia: string;
    canton: string;
    distrito: string;
    direccionExacta: string;
  };
  nombreUsuario?: string;
}

export interface Departamento {
  id: number;
  nombre: string;
}

export interface Direccion {
  id?: number;
  provincia: string;
  canton: string;
  distrito: string;
  direccionExacta: string;
}

export interface Puesto {
  id: number;
  nombre: string;
  salarioMinimo: number;
  horaEntrada: string;
  horaSalida: string;
  idDepartamento: number;
  departamento?: {
    id: number;
    nombre: string;
  };
}

export interface ConfiguracionRenta {
  id?: number;
  montoMinimo: number;
  montoMaximo: number;
  porcentaje: number;
}

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

export interface Aguinaldo {
  id?: number;
  anio: number;
  fechaInicioPeriodo: string;
  fechaFinPeriodo: string;
  totalSalariosDevengados: number;
  montoAguinaldo: number;
  fechaCalculo: string;
  fechaPago: string;
  idEmpleado: number;
}

export interface HorasExtra {
  id?: number;
  fechaSolicitud: string;
  cantidadDeHoras: number;
  motivo: string;
  aprobado: boolean;
  procesado: boolean;
  estadoSolicitud: string;
  tipoTarifa: string;
  idEmpleado: number;
}

export interface Permiso {
  id?: number;
  fechaInicio: string;
  fechaFin: string;
  diasTotales: number;
  motivo: string;
  urlDocumentoAdjunto?: string;
  estadoSolicitud: string;
  tipoPermiso: string;
  idEmpleado: number;
}

export interface Incapacidad {
  id?: number;
  fechaInicio: string;
  fechaFin: string;
  diasTotales: number;
  tipoIncapacidad: string;
  estadoSolicitud: string;
  porcentajePago: number;
  entidadEmisora: string;
  numeroDocumento?: string;
  observaciones?: string;
  urlDocumentoAdjunto?: string;
  idEmpleado: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
  segundoApellidoEmpleado?: string;
}

export interface Liquidacion {
  id?: number;
  fechaSalida: string;
  montoPreaviso: number;
  montoCesantia: number;
  montoVacacionesPendientes: number;
  montoAguinaldoPendiente: number;
  totalLiquidacion: number;
  motivoSalida: string;
  idEmpleado: number;
}

export interface PlanillaEncabezado {
  id?: number;
  fechaInicioPeriodo: string;
  fechaFinPeriodo: string;
  fechaPago: string;
  totalPlanillaBruto: number;
  totalPlanillaNeto: number;
  estadoPlanilla: string;
}

export interface EvaluacionDesempeno {
  id?: number;
  fechaEvaluacion: string;
  periodoEvaluado: string;
  puntuacionFinal: number;
  observaciones: string;
  planDeMejora: string;
  idEmpleado: number;
}

export interface JefeDepartamento {
  id?: number;
  idDepartamento: number;
  nombreDepartamento?: string;
  idEmpleado: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
  segundoApellidoEmpleado?: string;
  fechaInicio: string;
  fechaFin?: string;
  estaActivo: boolean;
}

export type Role = 'ADMIN' | 'HR' | 'JEFE' | 'EMPLEADO';

export interface GenerarUsuarioRequest {
  role: Role;
}

export interface CredencialesResponse {
  username: string;
  password: string;
  correoEmpleado: string;
  nombreCompleto: string;
}

// Extender ApiService para incluir generación de usuario
export class EmpleadosService extends ApiService<Empleado> {
  async generarUsuario(idEmpleado: number, role: Role): Promise<CredencialesResponse> {
    const token = localStorage.getItem('token');
    const response = await fetch(
      `${API_URL}/empleados/${idEmpleado}/generar-usuario`,
      {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...(token && { Authorization: `Bearer ${token}` }),
        },
        body: JSON.stringify({ role }),
      }
    );
    
    if (!response.ok) {
      const errorData: ErrorResponse = await response.json();
      throw new Error(errorData.message || 'Error en la solicitud');
    }
    
    return response.json();
  }
}

export interface DiaFeriado {
  id: number;
  nombre: string;
  fecha: string;
  descripcion?: string;
}

// Servicios específicos para cada entidad
export const empleadosService = new EmpleadosService('empleados');
export const departamentosService = new ApiService<Departamento>('departamentos');
export const direccionesService = new ApiService<Direccion>('direcciones');
export const puestosService = new ApiService<Puesto>('puestos');
export const configuracionRentaService = new ApiService<ConfiguracionRenta>('configuracion-renta');
export const asistenciasService = new ApiService<Asistencia>('asistencias');
export const aguinaldosService = new ApiService<Aguinaldo>('aguinaldos');
export const horasExtraService = new ApiService<HorasExtra>('horas-extra');
export const permisosService = new ApiService<Permiso>('permisos');
export const liquidacionesService = new ApiService<Liquidacion>('liquidaciones');
export const planillasService = new ApiService<PlanillaEncabezado>('planillas');
export const evaluacionesService = new ApiService<EvaluacionDesempeno>('evaluaciones');
export const jefesDepartamentoService = new ApiService<JefeDepartamento>('jefes-departamento');
export const incapacidadesService = new ApiService<Incapacidad>('incapacidades');
export const diasFeriadosService = new ApiService<DiaFeriado>('dias-feriados');
