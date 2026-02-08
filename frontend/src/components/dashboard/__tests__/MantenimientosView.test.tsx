import { render, screen } from '@testing-library/react';
import { MantenimientosView } from '../MantenimientosView';
import { describe, it, expect } from 'vitest';

describe('MantenimientosView', () => {
  it('renders the main heading', () => {
    render(<MantenimientosView />);

    expect(screen.getByText('Mantenimientos y Consultas')).toBeInTheDocument();
  });
});
