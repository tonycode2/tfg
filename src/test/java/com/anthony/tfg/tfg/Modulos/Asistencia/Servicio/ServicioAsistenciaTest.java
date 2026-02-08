package com.anthony.tfg.tfg.Modulos.Asistencia.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasAsistencias;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosAsistencia;

class ServicioAsistenciaTest {

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        ConsultasAsistencias consulta = mock(ConsultasAsistencias.class);
        MantenimientosAsistencia mantenimiento = mock(MantenimientosAsistencia.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);

        when(consulta.obtenerPorId(1L)).thenReturn(null);

        ServicioAsistencia servicio = new ServicioAsistencia(consulta, mantenimiento, consultasEmpleados);

        assertThrows(ResourceNotFoundException.class, () -> servicio.obtenerPorId(1L));
    }
}
