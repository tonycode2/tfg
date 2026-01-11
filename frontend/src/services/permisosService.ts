const API_URL = 'http://localhost:8080/api/permisos';

// ==================== INTERFACES ====================

export interface SolicitudPermiso {
  fechaInicio: string; // yyyy-MM-dd
  fechaFin: string; // yyyy-MM-dd
  diasTotales: number;
  motivo: string;
  observacionesEmpleado?: string; // Opcional (usado por jefes al denegar)
  urlDocumentoAdjunto?: string; // Opcional
  tipoPermiso: string; // PERSONAL, MEDICO, LUTO, etc.
  idEmpleado: number;
}

export interface RespuestaPermiso {
  id: number;
  fechaInicio: string;
  fechaFin: string;
  diasTotales: number;
  motivo: string;
  observacionesEmpleado: string;
  urlDocumentoAdjunto?: string;
  estadoSolicitud: string;
  tipoPermiso: string;
  
  // Fechas de auditoría
  fechaSolicitud: string;
  fechaAprobacionJefe?: string;
  fechaAprobacionRH?: string;
  
  // Comentarios
  comentariosJefe?: string;
  comentariosRH?: string;
  
  // Empleado solicitante
  nombreEmpleado: string;
  primerApellidoEmpleado: string;
  segundApellidoEmpleado: string;
  
  // Aprobador jefe
  nombreAprobadorJefe?: string;
  primerApellidoAprobadorJefe?: string;
  segundoApellidoAprobadorJefe?: string;
  
  // Aprobador RH
  nombreAprobadorRH?: string;
  primerApellidoAprobadorRH?: string;
  segundoApellidoAprobadorRH?: string;
}

export interface AccionPermiso {
  comentarios?: string;
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
const handleApiError = (error: any) => {
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
  try {
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
  } catch (error) {
    throw error;
  }
};

// ==================== API FUNCTIONS ====================

/**
 * EMPLEADOS: Obtiene las solicitudes del empleado autenticado
 */
export const obtenerMisSolicitudes = async (): Promise<RespuestaPermiso[]> => {
  return fetchWithAuth<RespuestaPermiso[]>(`${API_URL}/mis-solicitudes`);
};

/**
 * EMPLEADOS: Crea una nueva solicitud de permiso
 */
export const crearSolicitud = async (solicitud: SolicitudPermiso): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(API_URL, {
    method: 'POST',
    body: JSON.stringify(solicitud),
  });
};

/**
 * JEFES: Obtiene las solicitudes pendientes del departamento del jefe autenticado
 */
export const obtenerSolicitudesPendientesDepartamento = async (): Promise<RespuestaPermiso[]> => {
  return fetchWithAuth<RespuestaPermiso[]>(`${API_URL}/pendientes-departamento`);
};

/**
 * JEFES: Aprueba una solicitud como jefe
 */
export const aprobarPorJefe = async (id: number, accion: AccionPermiso): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(`${API_URL}/${id}/aprobar-jefe`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * JEFES: Rechaza una solicitud como jefe
 */
export const rechazarPorJefe = async (id: number, accion: AccionPermiso): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(`${API_URL}/${id}/rechazar-jefe`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * RH: Obtiene las solicitudes que necesitan aprobación de RH
 */
export const obtenerSolicitudesParaRH = async (): Promise<RespuestaPermiso[]> => {
  return fetchWithAuth<RespuestaPermiso[]>(`${API_URL}/pendientes-rh`);
};

/**
 * RH: Aprueba una solicitud como RH (aprobación final)
 */
export const aprobarPorRH = async (id: number, accion: AccionPermiso): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(`${API_URL}/${id}/aprobar-rh`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * RH: Rechaza una solicitud como RH
 */
export const rechazarPorRH = async (id: number, accion: AccionPermiso): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(`${API_URL}/${id}/rechazar-rh`, {
    method: 'POST',
    body: JSON.stringify(accion),
  });
};

/**
 * RH: Cancela una solicitud aprobada
 */
export const cancelarSolicitud = async (id: number): Promise<RespuestaPermiso> => {
  return fetchWithAuth<RespuestaPermiso>(`${API_URL}/${id}/cancelar`, {
    method: 'POST',
    body: JSON.stringify({}),
  });
};

/**
 * RH/ADMIN: Obtiene todas las solicitudes (auditoría)
 */
export const obtenerTodasLasSolicitudes = async (): Promise<RespuestaPermiso[]> => {
  return fetchWithAuth<RespuestaPermiso[]>(API_URL);
};

// ==================== EXPORT SERVICE OBJECT ====================

const permisosService = {
  // Empleados
  obtenerMisSolicitudes,
  crearSolicitud,
  
  // Jefes
  obtenerSolicitudesPendientesDepartamento,
  aprobarPorJefe,
  rechazarPorJefe,
  
  // RH
  obtenerSolicitudesParaRH,
  aprobarPorRH,
  rechazarPorRH,
  cancelarSolicitud,
  obtenerTodasLasSolicitudes,
};

export default permisosService;
