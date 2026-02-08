import { render, screen } from '@testing-library/react';
import { describe, expect, it, vi } from 'vitest';
import { MiPlanillaView } from '../MiPlanillaView';

vi.mock('jspdf', () => ({
  jsPDF: function MockJsPdf() {},
}));

vi.mock('@/services/authService', () => ({
  authService: {
    getUserInfo: () => ({ idEmpleado: 1, role: 'EMPLEADO' }),
  },
}));

vi.mock('@/services/apiService', () => ({
  planillasService: {
    getPlanillasPorEmpleado: vi.fn().mockResolvedValue([]),
    downloadPlanillaPdf: vi.fn().mockResolvedValue(new Blob()),
  },
}));

describe('MiPlanillaView', () => {
  it('renders the payroll header', async () => {
    render(<MiPlanillaView />);

    expect(await screen.findByText('Mi Planilla')).toBeInTheDocument();
  });
});
