package com.anthony.tfg.tfg.Modulos.Extras.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudHorasExtraDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasHorasExtras;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosHorasExtras;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

class ServicioExtrasTest {

    @Test
    void guardar_cantidadHorasMayorA3_lanzaExcepcion() {
        ConsultasHorasExtras consulta = mock(ConsultasHorasExtras.class);
        MantenimientosHorasExtras mantenimiento = mock(MantenimientosHorasExtras.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        JefesDepartamentoRepositorio jefesDepartamentoRepo = mock(JefesDepartamentoRepositorio.class);
        ServicioEmail servicioEmail = mock(ServicioEmail.class);
        Authentication auth = mock(Authentication.class);

        ServicioExtras servicio = new ServicioExtras(consulta, mantenimiento, consultasEmpleados, jefesDepartamentoRepo, servicioEmail);

        Empleados empleado = Empleados.builder().id(1L).build();
        User usuario = User.builder()
                .id(10L)
                .username("empleado.test")
                .password("secret")
                .role(Role.EMPLEADO)
                .empleado(empleado)
                .build();
        when(auth.getPrincipal()).thenReturn(usuario);

        SolicitudHorasExtraDTO dto = new SolicitudHorasExtraDTO();
        dto.cantidadDeHoras = 4;
        dto.fechaSolicitud = LocalDate.now();
        dto.motivo = "Horas extra prueba";
        dto.aprobado = false;
        dto.procesado = false;
        dto.estadoSolicitud = "PENDIENTE";
        dto.tipoTarifa = "SIMPLE";

        assertThrows(BadRequestException.class, () -> servicio.guardar(dto, auth));
    }
}
