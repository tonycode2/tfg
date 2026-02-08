package com.anthony.tfg.tfg.Modulos.Reportes.Servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Respuesta.ReporteVacacionesDTO;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.LiquidacionesRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaEncabezadoRepositorio;

class ServicioReportesTest {

    @Test
    void generarReporteVacaciones_sinEmpleados_totalEnCero() {
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        PlanillaEncabezadoRepositorio planillaEncabezadoRepositorio = mock(PlanillaEncabezadoRepositorio.class);
        PlanillaDetalleRepositorio planillaDetalleRepositorio = mock(PlanillaDetalleRepositorio.class);
        IncapacidadesRepositorio incapacidadesRepositorio = mock(IncapacidadesRepositorio.class);
        LiquidacionesRepositorio liquidacionesRepositorio = mock(LiquidacionesRepositorio.class);

        when(empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of());

        ServicioReportes servicio = new ServicioReportes(
                empleadosRepositorio,
                planillaEncabezadoRepositorio,
                planillaDetalleRepositorio,
                incapacidadesRepositorio,
                liquidacionesRepositorio);

        ReporteVacacionesDTO reporte = servicio.generarReporteVacaciones();

        assertNotNull(reporte);
        assertEquals(0, reporte.getTotalDiasPendientes());
        assertNotNull(reporte.getEmpleados());
        assertEquals(0, reporte.getEmpleados().size());
    }
}
