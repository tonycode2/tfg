import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { AsistenciaView } from '../AsistenciaView';

vi.mock('@/services/authService', () => ({
  authService: {
    getUserInfo: () => ({ idEmpleado: 1, role: 'EMPLEADO' }),
  },
}));

vi.mock('@/services/apiService', () => ({
  departamentosService: {
    getAllUnpaginated: vi.fn().mockResolvedValue([]),
  },
}));

vi.mock('@/services/asistenciaService', () => ({
  marcarEntrada: vi.fn(),
  marcarSalida: vi.fn(),
  obtenerMiEstado: vi.fn().mockResolvedValue(null),
  obtenerDepartamentosAccesibles: vi.fn().mockResolvedValue([]),
  obtenerResumenDepartamento: vi.fn().mockResolvedValue(null),
  obtenerHistorial: vi.fn().mockResolvedValue([]),
  obtenerPreviewJornadaDiaria: vi.fn().mockResolvedValue({}),
  combineDateAndTime: (fecha: string, hora: string) => `${fecha} ${hora}`,
  getCurrentDateString: () => '2026-02-07',
  getCurrentTimeString: () => '08:00',
  getStartOfMonthString: () => '2026-02-01 00:00:00',
  getEndOfMonthString: () => '2026-02-28 23:59:59',
}));

describe('AsistenciaView', () => {
  it('renders the attendance header', async () => {
    render(<AsistenciaView />);

    expect(await screen.findByText('Registro de Asistencia')).toBeInTheDocument();
  });
});
