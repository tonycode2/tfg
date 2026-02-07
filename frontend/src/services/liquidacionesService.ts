import { API_URL } from './apiService';

const API_ENDPOINT = `${API_URL}/liquidaciones`;

export interface SolicitudCalculoLiquidacion {
  idEmpleado: number;
  fechaSalida: string; // YYYY-MM-DD
  motivoSalida: string;
  preaviso_pagado: boolean;
  descripcion?: string | null;
}

export interface DetalleCalculo {
  concepto: string;
  formula: string;
  monto: number;
}

export interface LiquidacionCalculada {
  id: number;
  idEmpleado: number;
  nombreEmpleado?: string;
  primerApellidoEmpleado?: string;
  segundoApellidoEmpleado?: string;
  fechaSalida: string;
  motivoSalida: string;
  salarioPromedioDiario: number;
  diasTrabajadosTotal: number;
  preaviso_pagado: boolean;
  montoPreaviso: number;
  montoCesantia: number;
  montoVacacionesPendientes: number;
  montoAguinaldoProporcional: number;
  montoSalarioProporcional: number;
  totalLiquidacion: number;
  saldoVacaciones: number;
  descripcion?: string | null;
  detalles?: DetalleCalculo[];
}

export async function calcularLiquidacion(
  payload: SolicitudCalculoLiquidacion
): Promise<LiquidacionCalculada> {
  const token = localStorage.getItem('token');
  const response = await fetch(`${API_ENDPOINT}/calcular`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      ...(token && { Authorization: `Bearer ${token}` }),
    },
    body: JSON.stringify(payload),
  });

  if (!response.ok) {
    try {
      const err = await response.json();
      throw new Error(err.message || 'Error al calcular la liquidación');
    } catch (e) {
      throw new Error(`HTTP ${response.status} - ${response.statusText}`);
    }
  }

  return response.json();
}

export default {
  calcularLiquidacion,
} as const;
