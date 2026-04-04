package com.anthony.tfg.tfg.Modulos.Incapacidad.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudIncapacidadesDTO;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasIncapacidades;
import com.anthony.tfg.tfg.Modulos.Empleados.Servicio.ServicioEmail;
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosIncapacidades;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Util.FileStorageService;

class ServicioIncapacidadTest {

    @Test
    void guardar_fechaFinAntesDeInicio_lanzaExcepcion() {
        ConsultasIncapacidades consulta = mock(ConsultasIncapacidades.class);
        MantenimientosIncapacidades mantenimiento = mock(MantenimientosIncapacidades.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        JefesDepartamentoRepositorio jefesDepartamentoRepo = mock(JefesDepartamentoRepositorio.class);
        FileStorageService fileStorageService = mock(FileStorageService.class);
        ServicioJornadaDiaria servicioJornadaDiaria = mock(ServicioJornadaDiaria.class);
        ServicioEmail servicioEmail = mock(ServicioEmail.class);

        ServicioIncapacidad servicio = new ServicioIncapacidad(
                consulta,
                mantenimiento,
                consultasEmpleados,
                jefesDepartamentoRepo,
                fileStorageService,
                servicioJornadaDiaria,
                servicioEmail);

        SolicitudIncapacidadesDTO dto = new SolicitudIncapacidadesDTO();
        dto.setFechaInicio(LocalDate.now());
        dto.setFechaFin(LocalDate.now().minusDays(1));
        dto.setDiasTotales(1);
        dto.setTipoIncapacidad("ENFERMEDAD_COMUN");
        dto.setPorcentajePago(50.0);
        dto.setEntidadEmisora("CCSS");
        dto.setIdEmpleado(1L);

        assertThrows(BadRequestException.class, () -> servicio.guardar(dto));
    }
}
