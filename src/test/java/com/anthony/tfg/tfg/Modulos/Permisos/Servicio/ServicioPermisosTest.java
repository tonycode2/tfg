package com.anthony.tfg.tfg.Modulos.Permisos.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudPermisosDTO;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPermisos;
import com.anthony.tfg.tfg.Modulos.DiasFeriados.Servicio.ServicioDiasFeriados;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosPermisos;
import com.anthony.tfg.tfg.Modulos.Vacaciones.Servicio.ServicioVacaciones;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

class ServicioPermisosTest {

    @Test
    void guardar_fechaInicioPasada_lanzaExcepcion() {
        ConsultasPermisos consulta = mock(ConsultasPermisos.class);
        MantenimientosPermisos mantenimiento = mock(MantenimientosPermisos.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        JefesDepartamentoRepositorio jefesDepartamentoRepo = mock(JefesDepartamentoRepositorio.class);
        ServicioEmail servicioEmail = mock(ServicioEmail.class);
        ServicioVacaciones servicioVacaciones = mock(ServicioVacaciones.class);
        ServicioDiasFeriados servicioDiasFeriados = mock(ServicioDiasFeriados.class);
        ServicioJornadaDiaria servicioJornadaDiaria = mock(ServicioJornadaDiaria.class);

        ServicioPermisos servicio = new ServicioPermisos(
                consulta,
                mantenimiento,
                consultasEmpleados,
                jefesDepartamentoRepo,
                servicioEmail,
                servicioVacaciones,
                servicioDiasFeriados,
                servicioJornadaDiaria);

        SolicitudPermisosDTO dto = new SolicitudPermisosDTO();
        dto.fechaInicio = LocalDate.now().minusDays(1);
        dto.fechaFin = LocalDate.now();
        dto.motivo = "Motivo de prueba";
        dto.tipoPermiso = "VACACIONES";
        dto.idEmpleado = 1L;

        assertThrows(BadRequestException.class, () -> servicio.guardar(dto));
    }
}
