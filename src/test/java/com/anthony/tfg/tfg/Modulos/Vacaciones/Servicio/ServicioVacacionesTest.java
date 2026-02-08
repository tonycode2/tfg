package com.anthony.tfg.tfg.Modulos.Vacaciones.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

class ServicioVacacionesTest {

    @Test
    void validarSaldoDisponible_excedeSaldo_lanzaExcepcion() {
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        JefesDepartamentoRepositorio jefesDepartamentoRepo = mock(JefesDepartamentoRepositorio.class);

        Empleados empleado = new Empleados();
        empleado.setSaldoVacaciones(3);

        when(consultasEmpleados.obtenerPorId(1L)).thenReturn(empleado);

        ServicioVacaciones servicio = new ServicioVacaciones(empleadosRepositorio, consultasEmpleados, jefesDepartamentoRepo);

        assertThrows(BadRequestException.class, () -> servicio.validarSaldoDisponible(1L, 5));
    }
}
