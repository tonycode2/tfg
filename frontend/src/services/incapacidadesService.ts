const API_URL = 'http://localhost:8080/api/incapacidades';

// ==================== INTERFACES ====================

export interface SolicitudIncapacidad {
  fechaInicio: string; // yyyy-MM-dd
  fechaFin: string; // yyyy-MM-dd
  diasTotales: number;
  tipoIncapacidad: string; // ENFERMEDAD_COMUN, ACCIDENTE_LABORAL, etc.
  porcentajePago: number; // 0-100
  entidadEmisora: string; // CCSS, INS, CLINICA_PRIVADA, OTRO
  numeroDocumento?: string;
  observaciones?: string;
  urlDocumentoAdjunto?: string;
  idEmpleado: number;
}

export interface RespuestaIncapacidad {
  id: number;
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
  
  // Fechas de auditoría
  fechaSolicitud: string;
  fechaAprobacionJefe?: string;
  fechaAprobacionRH?: string;
  
  // Comentarios
  comentariosJefe?: string;
  comentariosRH?: string;
  
  // Campos de extensión
  esExtension?: boolean;
  idIncapacidadOriginal?: number;
  fechaFinOriginal?: string;
  comentariosExtension?: string;
  
  // Empleado solicitante
  idEmpleado: number;
  nombreEmpleado: string;
  primerApellidoEmpleado: string;
  segundoApellidoEmpleado: string;
  departamentoEmpleado?: string;
  
  // Aprobador jefe
  nombreAprobadorJefe?: string;
  primerApellidoAprobadorJefe?: string;
  segundoApellidoAprobadorJefe?: string;
  
  // Aprobador RH
  nombreAprobadorRH?: string;
  primerApellidoAprobadorRH?: string;
  segundoApellidoAprobadorRH?: string;
}

export interface AccionIncapacidad {
  comentarios?: string;
}

export interface SolicitudExtensionIncapacidad {
  nuevaFechaFin: string; // yyyy-MM-dd
  diasAdicionales: number;
  numeroDocumento?: string;
  observaciones?: string;
  urlDocumentoAdjunto?: string;
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

  // Handle 204 No Content
  if (response.status === 204) {
    return {} as T;
  }

  return await response.json();
};

// ==================== API FUNCTIONS - EMPLEADOS ====================

/**
 * EMPLEADOS: Obtiene las solicitudes de incapacidad del empleado autenticado
 */
export const obtenerMisSolicitudes = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/mis-solicitudes`);
};

/**
 * EMPLEADOS: Crea una nueva solicitud de incapacidad
 */
export const crearSolicitud = async (solicitud: SolicitudIncapacidad | FormData): Promise<RespuestaIncapacidad> => {
  // Si se pasa FormData (subida de archivo), no establecer Content-Type (browser lo hace automáticamente)
  if (solicitud instanceof FormData) {
    const token = localStorage.getItem('token');
    const response = await fetch(API_URL, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`
      },
      body: solicitud,
    });

    if (!response.ok) {
      const error = { status: response.status, message: await response.text() };
      handleApiError(error);
    }

    return await response.json();
  }

  return fetchWithAuth<RespuestaIncapacidad>(API_URL, {
    method: 'POST',
    body: JSON.stringify(solicitud),
  });
};

// ==================== API FUNCTIONS - JEFES ====================

/**
 * JEFES: Obtiene las solicitudes de incapacidad pendientes del departamento del jefe autenticado
 */
export const obtenerSolicitudesPendientesDepartamento = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/pendientes-departamento`);
};

/**
 * JEFES: Obtiene los empleados actualmente incapacitados del departamento del jefe autenticado
 */
export const obtenerEmpleadosIncapacitadosDepartamento = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/empleados-incapacitados-departamento`);
};

/**
 * JEFES: Solicita una extensión de incapacidad
 */
export const solicitarExtension = async (id: number, solicitud: SolicitudExtensionIncapacidad): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/solicitar-extension`, {
    method: 'POST',
    body: JSON.stringify(solicitud),
  });
};

/**
 * JEFES: Aprueba una solicitud de incapacidad como jefe
 */
export const aprobarPorJefe = async (id: number, accion: AccionIncapacidad): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/aprobar-jefe`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * JEFES: Rechaza una solicitud de incapacidad como jefe
 */
export const rechazarPorJefe = async (id: number, accion: AccionIncapacidad): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/rechazar-jefe`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

// ==================== API FUNCTIONS - RH ====================

/**
 * RH: Obtiene las solicitudes de incapacidad que necesitan aprobación de RH
 */
export const obtenerSolicitudesParaRH = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/pendientes-rh`);
};

/**
 * RH: Obtiene todas las solicitudes de incapacidad (para auditoría)
 */
export const obtenerTodasLasSolicitudes = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/todas`);
};

/**
 * RH: Obtiene las incapacidades activas (aprobadas y en curso)
 */
export const obtenerIncapacidadesActivas = async (): Promise<RespuestaIncapacidad[]> => {
  return fetchWithAuth<RespuestaIncapacidad[]>(`${API_URL}/activas`);
};

/**
 * RH: Aprueba una solicitud de incapacidad como RH (aprobación final)
 */
export const aprobarPorRH = async (id: number, accion: AccionIncapacidad): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/aprobar-rh`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * RH: Rechaza una solicitud de incapacidad como RH
 */
export const rechazarPorRH = async (id: number, accion: AccionIncapacidad): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/rechazar-rh`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * RH: Cancela una solicitud de incapacidad aprobada
 */
export const cancelarSolicitud = async (id: number): Promise<RespuestaIncapacidad> => {
  return fetchWithAuth<RespuestaIncapacidad>(`${API_URL}/${id}/cancelar`, {
    method: 'POST',
  });
};
