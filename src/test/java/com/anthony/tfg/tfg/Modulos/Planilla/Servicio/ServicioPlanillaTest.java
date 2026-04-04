package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarPlanillaDTO;
import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.Reportes.Util.ReportePdfGenerator;
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
        ServicioEmail servicioEmail = mock(ServicioEmail.class);
        ReportePdfGenerator reportePdfGenerator = mock(ReportePdfGenerator.class);

        when(planillaDetalleRepo.findByEmpleadoId(1L)).thenReturn(List.of());

        ServicioPlanilla servicio = new ServicioPlanilla(
                consulta,
                mantenimiento,
                planillaDetalleRepo,
                empleadosRepositorio,
                jornadaDiariaRepositorio,
                diasFeriadosRepositorio,
                consultasConfiguracionRentas,
                planillaPdfStorageService,
                servicioEmail,
                reportePdfGenerator);

        var resultado = servicio.obtenerPlanillasPorEmpleado(1L);

        assertNotNull(resultado);
        assertTrue(resultado.isEmpty());
    }

    @Test
    void generarPlanilla_tresHorasExtraConSalarioHoraDiezMil_retornaMontoHorasExtraCuarentaYCincoMil() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(2_400_000.0);

        List<JornadaDiaria> jornadas = crearJornadasConHorasExtra(
                List.of(LocalDate.of(2026, 1, 2)),
                3.0);
        
        // Configurar las jornadas con el empleado correcto
        jornadas.forEach(j -> j.setEmpleado(empleado));

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(jornadas);
        when(fixture.consultasConfiguracionRentas.obtenerTodos()).thenReturn(crearTramosParaRenta8200());

        SolicitudGenerarPlanillaDTO solicitud = new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA);
        fixture.servicio.generarPlanilla(solicitud);

        List<PlanillaDetalle> detalles = fixture.detallesGuardados.get();
        assertNotNull(detalles);
        assertEquals(1, detalles.size());

        PlanillaDetalle detalle = detalles.getFirst();
        assertEquals(45_000.0, detalle.getMontoHorasExtra(), 0.0001);
    }
    /** 
     * @return Fixture
     */
    private Fixture crearFixtureBase() {
        ConsultasPlanillaEncabezado consulta = mock(ConsultasPlanillaEncabezado.class);
        MantenimientosPlanillaEncabezados mantenimiento = mock(MantenimientosPlanillaEncabezados.class);
        PlanillaDetalleRepositorio planillaDetalleRepo = mock(PlanillaDetalleRepositorio.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        JornadaDiariaRepositorio jornadaDiariaRepositorio = mock(JornadaDiariaRepositorio.class);
        DiasFeriadosRepositorio diasFeriadosRepositorio = mock(DiasFeriadosRepositorio.class);
        ConsultasConfiguracionRentas consultasConfiguracionRentas = mock(ConsultasConfiguracionRentas.class);
        PlanillaPdfStorageService planillaPdfStorageService = mock(PlanillaPdfStorageService.class);
        ServicioEmail servicioEmail = mock(ServicioEmail.class);
        ReportePdfGenerator reportePdfGenerator = mock(ReportePdfGenerator.class);

        when(consulta.existePlanillaParaPeriodo(any(LocalDate.class), any(LocalDate.class), any(TipoQuincena.class)))
                .thenReturn(false);
        when(diasFeriadosRepositorio.findByFechaBetween(any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(List.of());
        when(consultasConfiguracionRentas.obtenerTodos()).thenReturn(List.of());

        when(mantenimiento.crear(any(PlanillaEncabezado.class))).thenAnswer(invocation -> {
            PlanillaEncabezado encabezado = invocation.getArgument(0);
            if (encabezado.getId() == null) {
                encabezado.setId(1L);
            }
            if (encabezado.getEstadoPlanilla() == null) {
                encabezado.setEstadoPlanilla(EstadoPlanilla.BORRADOR);
            }
            return encabezado;
        });
        when(mantenimiento.actualizar(any(PlanillaEncabezado.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AtomicReference<List<PlanillaDetalle>> detallesGuardados = new AtomicReference<>();
        when(planillaDetalleRepo.saveAll(anyList())).thenAnswer(invocation -> {
            List<PlanillaDetalle> detalles = invocation.getArgument(0);
            detallesGuardados.set(detalles);
            return detalles;
        });

        ServicioPlanilla servicio = new ServicioPlanilla(
                consulta,
                mantenimiento,
                planillaDetalleRepo,
                empleadosRepositorio,
                jornadaDiariaRepositorio,
                diasFeriadosRepositorio,
                consultasConfiguracionRentas,
                planillaPdfStorageService,
                servicioEmail,
                reportePdfGenerator);

        return new Fixture(servicio,
            empleadosRepositorio,
            jornadaDiariaRepositorio,
            consultasConfiguracionRentas,
            detallesGuardados);
    }

    /** 
     * @return List<ConfiguracionRenta>
     */
    private List<ConfiguracionRenta> crearTramosParaRenta8200() {
        return List.of(
                ConfiguracionRenta.builder()
                        .id(1L)
                        .montoMinimo(0.0)
                    .montoMaximo(860700.0)
                        .porcentaje(0.0)
                        .build(),
                ConfiguracionRenta.builder()
                        .id(2L)
                    .montoMinimo(860700.0)
                        .montoMaximo(Double.MAX_VALUE)
                        .porcentaje(20.0)
                        .build());
    }

    /** 
     * @param salarioMensual
     * @return Empleados
     */
    private Empleados crearEmpleadoConSalario(double salarioMensual) {
        Puestos puesto = Puestos.builder()
                .id(10L)
                .nombre("Operario")
                .salarioMinimo(salarioMensual)
                .build();

        return Empleados.builder()
                .id(1L)
                .nombre("Carlos")
                .primerApellido("Ramírez")
                .estaActivo(true)
                .puesto(puesto)
                .build();
    }


    private List<JornadaDiaria> crearJornadasConHorasExtra(List<LocalDate> fechas, double horasExtra) {
        List<JornadaDiaria> jornadas = new ArrayList<>();
        for (LocalDate fecha : fechas) {
            jornadas.add(JornadaDiaria.builder()
                    .fecha(fecha)
                    .horasRegulares(8.0)
                    .horasExtra(horasExtra)
                    .build());
        }
        return jornadas;
    }

    private record Fixture(
            ServicioPlanilla servicio,
            EmpleadosRepositorio empleadosRepositorio,
            JornadaDiariaRepositorio jornadaDiariaRepositorio,
            ConsultasConfiguracionRentas consultasConfiguracionRentas,
            AtomicReference<List<PlanillaDetalle>> detallesGuardados) {
    }
}
