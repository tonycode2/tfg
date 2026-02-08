package com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosJornadaDiaria;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.HorasExtraRepositorio;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;

class ServicioJornadaDiariaTest {

    @Test
    void obtenerPorId_noExiste_lanzaExcepcion() {
        ConsultasJornadaDiaria consulta = mock(ConsultasJornadaDiaria.class);
        MantenimientosJornadaDiaria mantenimiento = mock(MantenimientosJornadaDiaria.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        AsistenciaRepositorio asistenciaRepositorio = mock(AsistenciaRepositorio.class);
        HorasExtraRepositorio horasExtraRepositorio = mock(HorasExtraRepositorio.class);
        JornadaDiariaRepositorio jornadaDiariaRepositorio = mock(JornadaDiariaRepositorio.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        PermisosRepositorio permisosRepositorio = mock(PermisosRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

        when(consulta.obtenerPorId(1L)).thenReturn(null);

        ServicioJornadaDiaria servicio = new ServicioJornadaDiaria(
                consulta,
                mantenimiento,
                consultasEmpleados,
                asistenciaRepositorio,
                horasExtraRepositorio,
                jornadaDiariaRepositorio,
                empleadosRepositorio,
                permisosRepositorio,
                incapacidadesRepositorio);

        assertThrows(ResourceNotFoundException.class, () -> servicio.obtenerPorId(1L));
    }
}
