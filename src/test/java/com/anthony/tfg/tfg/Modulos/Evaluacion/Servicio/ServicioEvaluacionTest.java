package com.anthony.tfg.tfg.Modulos.Evaluacion.Servicio;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.sql.Date;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEvaluacionDeDesempenoDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Exceptions.ForbiddenException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Modulos.Asistencia.Servicio.ServicioRegistroAsistencia;

public class ServicioEvaluacionTest {

    private ConsultasEvaluacionDeDesempeno consultasEvaluacion;
    private MantenimientosEvaluacionDeDesempeno mantenimientos;
    private ConsultasEmpleados consultasEmpleados;
    private JefesDepartamentoRepositorio jefesDepartamentoRepositorio;
    private EmpleadosRepositorio empleadosRepositorio;
    private DepartamentoRepositorio departamentoRepositorio;
    private ServicioEmail servicioEmail;
    private ServicioRegistroAsistencia servicioRegistroAsistencia;
    private ServicioEvaluacion servicio;

    @BeforeEach
    public void setup() {
        consultasEvaluacion = mock(ConsultasEvaluacionDeDesempeno.class);
        mantenimientos = mock(MantenimientosEvaluacionDeDesempeno.class);
        consultasEmpleados = mock(ConsultasEmpleados.class);
        jefesDepartamentoRepositorio = mock(JefesDepartamentoRepositorio.class);
        empleadosRepositorio = mock(EmpleadosRepositorio.class);
        departamentoRepositorio = mock(DepartamentoRepositorio.class);
        servicioEmail = mock(ServicioEmail.class);
        servicioRegistroAsistencia = mock(ServicioRegistroAsistencia.class);

        servicio = new ServicioEvaluacion(consultasEvaluacion, mantenimientos, consultasEmpleados,
                jefesDepartamentoRepositorio, empleadosRepositorio, departamentoRepositorio, servicioEmail,
                servicioRegistroAsistencia);
    }

    @AfterEach
    public void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    public void guardar_asHr_sendsEmailAndReturnsResponse() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        User user = new User();
        user.setId(100L);
        user.setRole(Role.HR);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(user);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        Empleados empleado = new Empleados();
        empleado.setId(10L);
        empleado.setNombre("Juan");
        empleado.setPrimerApellido("Perez");
        empleado.setSegundoApellido("Lopez");
        empleado.setCorreoPersonal("juan.perez@example.com");

        when(consultasEmpleados.obtenerPorId(10L)).thenReturn(empleado);

        EvaluacionDeDesempeno guardada = EvaluacionDeDesempeno.builder()
                .id(1L)
                .puntuacionFinal(4.5)
                .periodoEvaluado("2025 Q4")
                .observaciones("Buen desempeño")
                .planDeMejora("Mantener calidad")
                .empleado(empleado)
                .build();

        when(mantenimientos.crear(any(EvaluacionDeDesempeno.class))).thenReturn(guardada);

        SolicitudEvaluacionDeDesempenoDTO dto = new SolicitudEvaluacionDeDesempenoDTO();
        dto.setIdEmpleado(10L);
        dto.setFechaEvaluacion(new Date(System.currentTimeMillis()));
        dto.setPeriodoEvaluado("2025 Q4");
        dto.setPuntuacionFinal(4.5);
        dto.setObservaciones("Buen desempeño");
        dto.setPlanDeMejora("Mantener calidad");

        // Act
        var respuesta = servicio.guardar(dto);

        // Assert
        assertNotNull(respuesta);
        verify(servicioEmail).enviarNotificacionEvaluacion("juan.perez@example.com", "Juan Perez Lopez", 4.5,
                "2025 Q4", "Buen desempeño", "Mantener calidad");
    }

    @Test
    public void guardar_asJefeWithoutAccess_throwsForbidden() {
        // Arrange
        Authentication auth = mock(Authentication.class);
        SecurityContext context = mock(SecurityContext.class);
        User user = new User();
        user.setId(200L);
        user.setRole(Role.JEFE);
        Empleados empleadoJefe = new Empleados();
        empleadoJefe.setId(2000L);
        user.setEmpleado(empleadoJefe);

        when(auth.isAuthenticated()).thenReturn(true);
        when(auth.getPrincipal()).thenReturn(user);
        when(context.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(context);

        Empleados empleadoEvaluado = new Empleados();
        empleadoEvaluado.setId(20L);
        when(consultasEmpleados.obtenerPorId(20L)).thenReturn(empleadoEvaluado);

        // jefe does not manage department -> repository returns empty
        when(jefesDepartamentoRepositorio.findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(any(Long.class), any(Long.class)))
                .thenReturn(Optional.empty());

        SolicitudEvaluacionDeDesempenoDTO dto = new SolicitudEvaluacionDeDesempenoDTO();
        dto.setIdEmpleado(20L);
        dto.setFechaEvaluacion(new Date(System.currentTimeMillis()));
        dto.setPeriodoEvaluado("2025 Q4");
        dto.setPuntuacionFinal(3.0);
        dto.setObservaciones("Observaciones largas...");
        dto.setPlanDeMejora("Plan de mejora largo...");

        // Act & Assert
        assertThrows(ForbiddenException.class, () -> servicio.guardar(dto));
    }

}
