import { API_URL } from './apiService';

function getAuthHeaders(contentType: string | null = 'application/json') {
  const token = localStorage.getItem('token');
  const base: Record<string, string> = {};
  if (contentType) base['Content-Type'] = contentType;
  if (token) base['Authorization'] = `Bearer ${token}`;
  return base;
}

async function fetchJson<T>(url: string): Promise<T> {
  const res = await fetch(url, {
    method: 'GET',
    headers: getAuthHeaders(null),
  });
  if (!res.ok) {
    const text = await res.text();
    throw new Error(text || `HTTP ${res.status}`);
  }
  return res.json() as Promise<T>;
}

export interface PlanillaEncabezado {
  id: number;
  fechaInicioPeriodo: string;
  fechaFinPeriodo: string;
  fechaPago: string;
  tipoQuincena: string;
  totalPlanillaBruto: number;
  totalPlanillaNeto: number;
  estadoPlanilla: string;
}

export interface PlanillaDetalle {
  id: number;
  nombreEmpleado: string;
  primerApellidoEmpleado: string;
  segundoApellidoEmpleado: string;
}

export interface LiquidacionResumen {
  id: number;
  idEmpleado: number;
  fechaSalida: string;
  nombreEmpleado: string;
  primerApellidoEmpleado: string;
  segundoApellidoEmpleado: string;
}

async function downloadBlob(response: Response, filename: string) {
  if (!response.ok) {
    const text = await response.text();
    throw new Error(text || `HTTP ${response.status}`);
  }
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const a = document.createElement('a');
  a.href = url;
  a.download = filename;
  document.body.appendChild(a);
  a.click();
  a.remove();
  window.URL.revokeObjectURL(url);
}

export const reportesService = {
  async obtenerPlanillas(): Promise<PlanillaEncabezado[]> {
    return fetchJson<PlanillaEncabezado[]>(`${API_URL}/planillas`);
  },

  async obtenerPlanillaDetalles(planillaId: number): Promise<PlanillaDetalle[]> {
    return fetchJson<PlanillaDetalle[]>(`${API_URL}/planillas/${planillaId}/detalles`);
  },

  async obtenerLiquidaciones(): Promise<LiquidacionResumen[]> {
    return fetchJson<LiquidacionResumen[]>(`${API_URL}/liquidaciones`);
  },

  async planilla(planillaId: number) {
    const res = await fetch(`${API_URL}/reportes/planilla/${planillaId}`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-planilla-${planillaId}.pdf`);
  },

  async colilla(detalleId: number) {
    const res = await fetch(`${API_URL}/reportes/colilla/${detalleId}`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `colilla-pago-${detalleId}.pdf`);
  },

  async vacaciones() {
    const res = await fetch(`${API_URL}/reportes/vacaciones`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-vacaciones.pdf`);
  },

  async antiguedad() {
    const res = await fetch(`${API_URL}/reportes/antiguedad`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-antiguedad.pdf`);
  },

  async deducciones(planillaId: number) {
    const res = await fetch(`${API_URL}/reportes/deducciones/${planillaId}`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-deducciones-${planillaId}.pdf`);
  },

  async liquidacion(liquidacionId: number) {
    const res = await fetch(`${API_URL}/reportes/liquidacion/${liquidacionId}`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-liquidacion-${liquidacionId}.pdf`);
  },

  async incapacidades(fechaInicio: string, fechaFin: string) {
    const params = new URLSearchParams({ fechaInicio, fechaFin });
    const res = await fetch(`${API_URL}/reportes/incapacidades?${params.toString()}`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `reporte-incapacidades-${fechaInicio}_to_${fechaFin}.pdf`);
  },

  async proyeccionCesantia() {
    const res = await fetch(`${API_URL}/reportes/proyeccion-cesantia`, {
      method: 'GET',
      headers: getAuthHeaders(null),
    });
    await downloadBlob(res, `proyeccion-cesantia.pdf`);
  },
};

export default reportesService;
