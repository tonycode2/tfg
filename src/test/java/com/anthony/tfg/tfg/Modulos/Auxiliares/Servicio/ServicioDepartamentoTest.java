package com.anthony.tfg.tfg.Modulos.Auxiliares.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDepartamentos;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosDepartamentos;

class ServicioDepartamentoTest {

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        ConsultasDepartamentos consulta = mock(ConsultasDepartamentos.class);
        MantenimientosDepartamentos mantenimiento = mock(MantenimientosDepartamentos.class);

        when(consulta.obtenerPorId(1L)).thenReturn(null);

        ServicioDepartamento servicio = new ServicioDepartamento(consulta, mantenimiento);

        assertThrows(ResourceNotFoundException.class, () -> servicio.obtenerPorId(1L));
    }
}
