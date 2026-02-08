import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import IncapacidadesView from '../IncapacidadesView';

vi.mock('../../services/authService', () => ({
  authService: {
    getUserInfo: () => ({ idEmpleado: 1, role: 'EMPLEADO' }),
  },
}));

vi.mock('../../services/incapacidadesService', () => ({
  crearSolicitud: vi.fn(),
  obtenerMisSolicitudes: vi.fn().mockResolvedValue([]),
  solicitarExtension: vi.fn(),
}));

describe('IncapacidadesView', () => {
  it('renders the incapacity header', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue({
      ok: true,
      json: async () => [],
    }));

    render(<IncapacidadesView />);

    expect(await screen.findByText('Mis Incapacidades')).toBeInTheDocument();
  });
});
