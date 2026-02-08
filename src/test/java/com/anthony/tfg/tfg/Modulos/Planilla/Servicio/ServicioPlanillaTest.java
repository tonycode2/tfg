package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;
import com.anthony.tfg.tfg.Util.PlanillaPdfStorageService;

class ServicioPlanillaTest {

    @Test
    void obtenerPlanillasPorEmpleado_sinDetalles_retornaListaVacia() {
        ConsultasPlanillaEncabezado consulta = mock(ConsultasPlanillaEncabezado.class);
        MantenimientosPlanillaEncabezados mantenimiento = mock(MantenimientosPlanillaEncabezados.class);
        PlanillaDetalleRepositorio planillaDetalleRepo = mock(PlanillaDetalleRepositorio.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        JornadaDiariaRepositorio jornadaDiariaRepositorio = mock(JornadaDiariaRepositorio.class);
        DiasFeriadosRepositorio diasFeriadosRepositorio = mock(DiasFeriadosRepositorio.class);
        ConsultasConfiguracionRentas consultasConfiguracionRentas = mock(ConsultasConfiguracionRentas.class);
        PlanillaPdfStorageService planillaPdfStorageService = mock(PlanillaPdfStorageService.class);

        when(planillaDetalleRepo.findByEmpleadoId(1L)).thenReturn(List.of());

        ServicioPlanilla servicio = new ServicioPlanilla(
                consulta,
                mantenimiento,
                planillaDetalleRepo,
                empleadosRepositorio,
                jornadaDiariaRepositorio,
                diasFeriadosRepositorio,
                consultasConfiguracionRentas,
                planillaPdfStorageService);

        var resultado = servicio.obtenerPlanillasPorEmpleado(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }
}
