package com.anthony.tfg.tfg.Modulos.Aguinaldo.Servicio;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasAguinaldos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosAguinaldo;
import com.anthony.tfg.tfg.Entidades.Aguinaldos;
import com.anthony.tfg.tfg.Entidades.Empleados;
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

        var resultado = servicio.calcularAguinaldos(2026);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
        verifyNoInteractions(planillaDetalleRepositorio, aguinaldosRepositorio, mantenimiento);
    }

        @Test
        void calcularAguinaldos_salarioMensualConstante_retornaAguinaldoIgualAlSalarioMensual() {
        ConsultasAguinaldos consulta = mock(ConsultasAguinaldos.class);
        MantenimientosAguinaldo mantenimiento = mock(MantenimientosAguinaldo.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        AguinaldosRepositorio aguinaldosRepositorio = mock(AguinaldosRepositorio.class);

        Empleados empleado = Empleados.builder()
            .id(1L)
            .nombre("María")
            .primerApellido("Pérez")
            .segundoApellido("Rojas")
            .build();

        when(empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(any(Long.class), any(), any()))
            .thenReturn(14_400_000.0);
        when(aguinaldosRepositorio.findByEmpleadoIdAndAnio(any(Long.class), any(Integer.class)))
            .thenReturn(java.util.Optional.empty());
        when(mantenimiento.actualizar(any(Aguinaldos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicioAguinaldo servicio = new ServicioAguinaldo(
            consulta,
            mantenimiento,
            consultasEmpleados,
            empleadosRepositorio,
            planillaDetalleRepositorio,
            aguinaldosRepositorio);

        var resultado = servicio.calcularAguinaldos(2026);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(14_400_000.0, resultado.get(0).totalSalariosDevengados());
        assertEquals(1_200_000.0, resultado.get(0).montoAguinaldo());
        verify(mantenimiento).actualizar(any(Aguinaldos.class));
        }

        @Test
        void calcularAguinaldos_seisMesesDevengados_retornaMedioAguinaldo() {
        ConsultasAguinaldos consulta = mock(ConsultasAguinaldos.class);
        MantenimientosAguinaldo mantenimiento = mock(MantenimientosAguinaldo.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        AguinaldosRepositorio aguinaldosRepositorio = mock(AguinaldosRepositorio.class);

        Empleados empleado = Empleados.builder()
            .id(2L)
            .nombre("Carlos")
            .primerApellido("Ramírez")
            .segundoApellido("Vega")
            .build();

        when(empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(any(Long.class), any(), any()))
            .thenReturn(7_200_000.0);
        when(aguinaldosRepositorio.findByEmpleadoIdAndAnio(any(Long.class), any(Integer.class)))
            .thenReturn(java.util.Optional.empty());
        when(mantenimiento.actualizar(any(Aguinaldos.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ServicioAguinaldo servicio = new ServicioAguinaldo(
            consulta,
            mantenimiento,
            consultasEmpleados,
            empleadosRepositorio,
            planillaDetalleRepositorio,
            aguinaldosRepositorio);

        var resultado = servicio.calcularAguinaldos(2026);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        assertEquals(7_200_000.0, resultado.get(0).totalSalariosDevengados());
        assertEquals(600_000.0, resultado.get(0).montoAguinaldo());
        verify(mantenimiento).actualizar(any(Aguinaldos.class));
        }
}
