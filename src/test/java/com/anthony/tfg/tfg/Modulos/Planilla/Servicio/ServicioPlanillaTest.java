package com.anthony.tfg.tfg.Modulos.Planilla.Servicio;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarPlanillaDTO;
import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasConfiguracionRentas;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPlanillaEncabezado;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPlanillaEncabezados;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;
import com.anthony.tfg.tfg.Util.PlanillaPdfStorageService;

class ServicioPlanillaTest {

    private static final double SALARIO_MENSUAL_PRUEBA = 300000.0;
    private static final double SALARIO_MENSUAL_RENTA = 1_000_000.0;
    private static final double SALARIO_MENSUAL_RENTA_ALTA = 2_500_000.0;

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

    @Test
    void generarPlanilla_conIncapacidadCcss_rebajaDiasYAplicaPagoSoloPrimerosTresDias() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(SALARIO_MENSUAL_PRUEBA);

        LocalDate inicioPeriodo = LocalDate.of(2026, 1, 1);
        LocalDate finPeriodo = LocalDate.of(2026, 1, 14);
        List<LocalDate> diasLaborablesIncapacidad = primerosDiasLaborables(inicioPeriodo, finPeriodo, 10);
        List<JornadaDiaria> jornadas = crearJornadasIncapacidad(diasLaborablesIncapacidad, TipoEntidadEmisora.CCSS);

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(empleado.getId(), inicioPeriodo, finPeriodo))
                .thenReturn(jornadas);

        SolicitudGenerarPlanillaDTO solicitud = new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA);
        fixture.servicio.generarPlanilla(solicitud);

        List<PlanillaDetalle> detalles = fixture.detallesGuardados.get();
        assertNotNull(detalles);
        assertEquals(1, detalles.size());

        PlanillaDetalle detalle = detalles.getFirst();
        assertEquals(150000.0, detalle.getSalarioBasePeriodo(), 0.0001);
        assertEquals(0.0, detalle.getMontoIncapacidad(), 0.0001);
        assertEquals(11, detalle.getCantidadDiasNoTrabajadosEnQuincena());
    }

    @Test
    void generarPlanilla_conPermisoSinGoce_rebajaSalarioDiarioSinMontoIncapacidad() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(SALARIO_MENSUAL_PRUEBA);

        LocalDate inicioPeriodo = LocalDate.of(2026, 1, 1);
        LocalDate finPeriodo = LocalDate.of(2026, 1, 14);
        List<LocalDate> diasLaborablesSinGoce = primerosDiasLaborables(inicioPeriodo, finPeriodo, 4);
        List<JornadaDiaria> jornadas = crearJornadasPermisoSinGoce(diasLaborablesSinGoce);

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(empleado.getId(), inicioPeriodo, finPeriodo))
                .thenReturn(jornadas);

        SolicitudGenerarPlanillaDTO solicitud = new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA);
        fixture.servicio.generarPlanilla(solicitud);

        List<PlanillaDetalle> detalles = fixture.detallesGuardados.get();
        assertNotNull(detalles);
        assertEquals(1, detalles.size());

        PlanillaDetalle detalle = detalles.getFirst();
        assertEquals(150000.0, detalle.getSalarioBasePeriodo(), 0.0001);
        assertEquals(0.0, detalle.getMontoIncapacidad(), 0.0001);
        assertEquals(11, detalle.getCantidadDiasNoTrabajadosEnQuincena());
    }

        @Test
        void generarPlanilla_salarioUnMillon_sumaDeduccionesObrerasMensualesEs108300() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(SALARIO_MENSUAL_RENTA);

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());
        when(fixture.consultasConfiguracionRentas.obtenerTodos()).thenReturn(crearTramosParaRenta8200());

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA));
        PlanillaDetalle detallePrimera = fixture.detallesGuardados.get().getFirst();

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.SEGUNDA));
        PlanillaDetalle detalleSegunda = fixture.detallesGuardados.get().getFirst();

        double deduccionesObrerasPrimera = detallePrimera.getDeduccionCcssSem()
            + detallePrimera.getDeduccionCcssIvm()
            + detallePrimera.getOtrasDeducciones();
        double deduccionesObrerasSegunda = detalleSegunda.getDeduccionCcssSem()
            + detalleSegunda.getDeduccionCcssIvm()
            + detalleSegunda.getOtrasDeducciones();
        double deduccionObreraMensual = deduccionesObrerasPrimera + deduccionesObrerasSegunda;

        assertEquals(108300.0, deduccionObreraMensual, 0.0001);
        }

        @Test
        void generarPlanilla_salarioUnMillon_rentaSeCobraSoloAlFinalDelMesPor8200() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(SALARIO_MENSUAL_RENTA);

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());
        when(fixture.consultasConfiguracionRentas.obtenerTodos()).thenReturn(crearTramosParaRenta8200());

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA));
        PlanillaDetalle detallePrimera = fixture.detallesGuardados.get().getFirst();

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.SEGUNDA));
        PlanillaDetalle detalleSegunda = fixture.detallesGuardados.get().getFirst();

        assertEquals(0.0, detallePrimera.getImpuestoDeRenta(), 0.0001);
        assertEquals(8200.0, detalleSegunda.getImpuestoDeRenta(), 0.0001);
        }

        @Test
        void generarPlanilla_salarioDosMillonesQuinientos_ccssEs270750YRenta222650EnSegundaQuincena() {
        var fixture = crearFixtureBase();
        Empleados empleado = crearEmpleadoConSalario(SALARIO_MENSUAL_RENTA_ALTA);

        when(fixture.empleadosRepositorio.findByEstaActivoTrue()).thenReturn(List.of(empleado));
        when(fixture.jornadaDiariaRepositorio.findByEmpleadoIdAndFechaBetween(anyLong(), any(LocalDate.class), any(LocalDate.class)))
            .thenReturn(List.of());
        when(fixture.consultasConfiguracionRentas.obtenerTodos()).thenReturn(crearTramosParaRenta222650());

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.PRIMERA));
        PlanillaDetalle detallePrimera = fixture.detallesGuardados.get().getFirst();

        fixture.servicio.generarPlanilla(new SolicitudGenerarPlanillaDTO(1, 2026, TipoQuincena.SEGUNDA));
        PlanillaDetalle detalleSegunda = fixture.detallesGuardados.get().getFirst();

        double deduccionesObrerasPrimera = detallePrimera.getDeduccionCcssSem()
            + detallePrimera.getDeduccionCcssIvm()
            + detallePrimera.getOtrasDeducciones();
        double deduccionesObrerasSegunda = detalleSegunda.getDeduccionCcssSem()
            + detalleSegunda.getDeduccionCcssIvm()
            + detalleSegunda.getOtrasDeducciones();

        assertEquals(270750.0, deduccionesObrerasPrimera + deduccionesObrerasSegunda, 0.0001);
        assertEquals(0.0, detallePrimera.getImpuestoDeRenta(), 0.0001);
        assertEquals(222650.0, detalleSegunda.getImpuestoDeRenta(), 0.0001);
        }

    private Fixture crearFixtureBase() {
        ConsultasPlanillaEncabezado consulta = mock(ConsultasPlanillaEncabezado.class);
        MantenimientosPlanillaEncabezados mantenimiento = mock(MantenimientosPlanillaEncabezados.class);
        PlanillaDetalleRepositorio planillaDetalleRepo = mock(PlanillaDetalleRepositorio.class);
        EmpleadosRepositorio empleadosRepositorio = mock(EmpleadosRepositorio.class);
        JornadaDiariaRepositorio jornadaDiariaRepositorio = mock(JornadaDiariaRepositorio.class);
        DiasFeriadosRepositorio diasFeriadosRepositorio = mock(DiasFeriadosRepositorio.class);
        ConsultasConfiguracionRentas consultasConfiguracionRentas = mock(ConsultasConfiguracionRentas.class);
        PlanillaPdfStorageService planillaPdfStorageService = mock(PlanillaPdfStorageService.class);

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
                planillaPdfStorageService);

        return new Fixture(servicio,
            empleadosRepositorio,
            jornadaDiariaRepositorio,
            consultasConfiguracionRentas,
            detallesGuardados);
    }

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

                private List<ConfiguracionRenta> crearTramosParaRenta222650() {
                return List.of(
                    ConfiguracionRenta.builder()
                        .id(1L)
                        .montoMinimo(0.0)
                        .montoMaximo(1141000.0)
                        .porcentaje(0.0)
                        .build(),
                    ConfiguracionRenta.builder()
                        .id(2L)
                        .montoMinimo(1141000.0)
                        .montoMaximo(Double.MAX_VALUE)
                        .porcentaje(20.0)
                        .build());
                }

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

    private List<JornadaDiaria> crearJornadasIncapacidad(List<LocalDate> fechas, TipoEntidadEmisora entidadEmisora) {
        Incapacidades incapacidad = Incapacidades.builder()
                .id(100L)
                .entidadEmisora(entidadEmisora)
                .build();

        List<JornadaDiaria> jornadas = new ArrayList<>();
        for (int i = 0; i < fechas.size(); i++) {
            jornadas.add(JornadaDiaria.builder()
                    .fecha(fechas.get(i))
                    .horasRegulares(0.0)
                    .horasExtra(0.0)
                    .diaPermiso(i + 1)
                    .incapacidad(incapacidad)
                    .build());
        }
        return jornadas;
    }

    private List<JornadaDiaria> crearJornadasPermisoSinGoce(List<LocalDate> fechas) {
        Permisos permisoSinGoce = Permisos.builder()
                .id(200L)
                .tipoPermiso(TipoPermiso.SIN_GOCE_SALARIO)
                .build();

        List<JornadaDiaria> jornadas = new ArrayList<>();
        for (int i = 0; i < fechas.size(); i++) {
            jornadas.add(JornadaDiaria.builder()
                    .fecha(fechas.get(i))
                    .horasRegulares(0.0)
                    .horasExtra(0.0)
                    .diaPermiso(i + 1)
                    .permiso(permisoSinGoce)
                    .build());
        }
        return jornadas;
    }

    private List<LocalDate> primerosDiasLaborables(LocalDate inicio, LocalDate fin, int cantidadDias) {
        List<LocalDate> resultado = new ArrayList<>();
        LocalDate fecha = inicio;
        while (!fecha.isAfter(fin) && resultado.size() < cantidadDias) {
            if (!esFinDeSemana(fecha)) {
                resultado.add(fecha);
            }
            fecha = fecha.plusDays(1);
        }
        return resultado;
    }

    private boolean esFinDeSemana(LocalDate fecha) {
        DayOfWeek dayOfWeek = fecha.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    private record Fixture(
            ServicioPlanilla servicio,
            EmpleadosRepositorio empleadosRepositorio,
            JornadaDiariaRepositorio jornadaDiariaRepositorio,
            ConsultasConfiguracionRentas consultasConfiguracionRentas,
            AtomicReference<List<PlanillaDetalle>> detallesGuardados) {
    }
}
