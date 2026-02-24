package com.anthony.tfg.tfg.Modulos.Liquidacion.Servicios;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

class LiquidacionesCalculoServicioTest {

        @Test
        void calcularCesantia_unAnioExacto_aplica19Punto5Dias() {
                PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
                IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

                LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                                planillaDetalleRepositorio,
                                incapacidadesRepositorio);

                double monto = servicio.calcularCesantia(365, 10_000.0, MotivoSalida.DESPIDO_CON_RESPONSABILIDAD);

                assertEquals(195_000.0, monto, 0.01);
        }

        @Test
        void calcularCesantia_unAnioMasFraccionMayorASeisMeses_sumaSegundoAnio() {
                PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
                IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

                LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                                planillaDetalleRepositorio,
                                incapacidadesRepositorio);

                double monto = servicio.calcularCesantia(365 + 184, 10_000.0, MotivoSalida.DESPIDO_CON_RESPONSABILIDAD);

                assertEquals(400_000.0, monto, 0.01);
        }

    @Test
    void calcularLiquidacionCompleta_despidoConResponsabilidad_eneroANoviembre_salarioConstante() {
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

        LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                planillaDetalleRepositorio,
                incapacidadesRepositorio);

        Long empleadoId = 1L;
        LocalDate fechaSalida = LocalDate.of(2026, 11, 30);

        Empleados empleado = Empleados.builder()
                .id(empleadoId)
                .fechaIngreso(LocalDate.of(2026, 1, 1))
                .saldoVacaciones(8)
                .build();

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                fechaSalida.minusMonths(6),
                fechaSalida)).thenReturn(4_200_000.0);

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                LocalDate.of(2025, 12, 1),
                fechaSalida)).thenReturn(8_400_000.0);

        LiquidacionesCalculoServicio.ResultadoCalculo resultado = servicio.calcularLiquidacionCompleta(
                empleado,
                fechaSalida,
                MotivoSalida.DESPIDO_CON_RESPONSABILIDAD,
                true);

        assertEquals(326666.67, resultado.montoCesantia(), 0.01);
        assertEquals(350000.00, resultado.montoPreaviso(), 0.01);
        assertEquals(700000.00, resultado.montoAguinaldoProporcional(), 0.01);
        assertEquals(186666.67, resultado.montoVacacionesPendientes(), 0.01);
    }

    @Test
    void calcularLiquidacionCompleta_enero2023ANoviembre2026_salarioConstante() {
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

        LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                planillaDetalleRepositorio,
                incapacidadesRepositorio);

        Long empleadoId = 2L;
        LocalDate fechaSalida = LocalDate.of(2026, 11, 30);

        Empleados empleado = Empleados.builder()
                .id(empleadoId)
                .fechaIngreso(LocalDate.of(2023, 1, 1))
                .saldoVacaciones(20)
                .build();

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                fechaSalida.minusMonths(6),
                fechaSalida)).thenReturn(7_200_000.0);

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                LocalDate.of(2025, 12, 1),
                fechaSalida)).thenReturn(14_400_000.0);

        LiquidacionesCalculoServicio.ResultadoCalculo resultado = servicio.calcularLiquidacionCompleta(
                empleado,
                fechaSalida,
                MotivoSalida.DESPIDO_CON_RESPONSABILIDAD,
                true);

        assertEquals(3360000.00, resultado.montoCesantia(), 0.01);
        assertEquals(1200000.00, resultado.montoPreaviso(), 0.01);
        assertEquals(1200000.00, resultado.montoAguinaldoProporcional(), 0.01);
        assertEquals(800000.00, resultado.montoVacacionesPendientes(), 0.01);
    }

    @Test
    void calcularLiquidacionCompleta_enero2020ANoviembre2025_salarioConstante() {
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

        LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                planillaDetalleRepositorio,
                incapacidadesRepositorio);

        Long empleadoId = 3L;
        LocalDate fechaSalida = LocalDate.of(2025, 11, 30);

        Empleados empleado = Empleados.builder()
                .id(empleadoId)
                .fechaIngreso(LocalDate.of(2020, 1, 1))
                .saldoVacaciones(26)
                .build();

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                fechaSalida.minusMonths(6),
                fechaSalida)).thenReturn(5_202_000.0);

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                LocalDate.of(2024, 12, 1),
                fechaSalida)).thenReturn(10_404_000.0);

        LiquidacionesCalculoServicio.ResultadoCalculo resultado = servicio.calcularLiquidacionCompleta(
                empleado,
                fechaSalida,
                MotivoSalida.DESPIDO_CON_RESPONSABILIDAD,
                true);

        assertEquals(3728100.00, resultado.montoCesantia(), 0.01);
        assertEquals(867000.00, resultado.montoPreaviso(), 0.01);
        assertEquals(867000.00, resultado.montoAguinaldoProporcional(), 0.01);
        assertEquals(751400.00, resultado.montoVacacionesPendientes(), 0.01);
    }

    @Test
        void calcularLiquidacionCompleta_julio2022ANoviembre2025_salarioConstante() {
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);

        LiquidacionesCalculoServicio servicio = new LiquidacionesCalculoServicio(
                planillaDetalleRepositorio,
                incapacidadesRepositorio);

        Long empleadoId = 4L;
        LocalDate fechaSalida = LocalDate.of(2025, 11, 30);

        Empleados empleado = Empleados.builder()
                .id(empleadoId)
                .fechaIngreso(LocalDate.of(2022, 7, 1))
                .saldoVacaciones(15)
                .build();

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                fechaSalida.minusMonths(6),
                fechaSalida)).thenReturn(5_400_000.0);

        when(planillaDetalleRepositorio.sumDevengadoByEmpleadoAndFechaPagoBetween(
                empleadoId,
                LocalDate.of(2024, 12, 1),
                fechaSalida)).thenReturn(10_800_000.0);

        LiquidacionesCalculoServicio.ResultadoCalculo resultado = servicio.calcularLiquidacionCompleta(
                empleado,
                fechaSalida,
                MotivoSalida.DESPIDO_CON_RESPONSABILIDAD,
                true);

        assertEquals(1845000.00, resultado.montoCesantia(), 0.01);
        assertEquals(900000.00, resultado.montoPreaviso(), 0.01);
        assertEquals(900000.00, resultado.montoAguinaldoProporcional(), 0.01);
        assertEquals(450000.00, resultado.montoVacacionesPendientes(), 0.01);
    }
}
