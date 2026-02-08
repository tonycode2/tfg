package com.anthony.tfg.tfg.Modulos.Aguinaldo.Servicio;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasAguinaldos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosAguinaldo;
import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

class ServicioAguinaldoTest {

    @Test
    void calcularAguinaldos_sinEmpleados_retornaListaVacia() {
        ConsultasAguinaldos consulta = mock(ConsultasAguinaldos.class);
        MantenimientosAguinaldo mantenimiento = mock(MantenimientosAguinaldo.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        AguinaldosRepositorio aguinaldosRepositorio = mock(AguinaldosRepositorio.class);

        when(empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of());

        ServicioAguinaldo servicio = new ServicioAguinaldo(
                consulta,
                mantenimiento,
                consultasEmpleados,
                empleadosRepositorio,
                planillaDetalleRepositorio,
                aguinaldosRepositorio);

        var resultado = servicio.calcularAguinaldos();

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(planillaDetalleRepositorio, aguinaldosRepositorio, mantenimiento);
    }
}
