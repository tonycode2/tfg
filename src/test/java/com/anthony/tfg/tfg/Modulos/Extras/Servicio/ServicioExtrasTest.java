package com.anthony.tfg.tfg.Modulos.Extras.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudHorasExtraDTO;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasHorasExtras;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosHorasExtras;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

class ServicioExtrasTest {

    @Test
    void guardar_cantidadHorasMayorA3_lanzaExcepcion() {
        ConsultasHorasExtras consulta = mock(ConsultasHorasExtras.class);
        MantenimientosHorasExtras mantenimiento = mock(MantenimientosHorasExtras.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        JefesDepartamentoRepositorio jefesDepartamentoRepo = mock(JefesDepartamentoRepositorio.class);

        ServicioExtras servicio = new ServicioExtras(consulta, mantenimiento, consultasEmpleados, jefesDepartamentoRepo);

        SolicitudHorasExtraDTO dto = new SolicitudHorasExtraDTO();
        dto.cantidadDeHoras = 4;
        dto.fechaSolicitud = LocalDate.now();
        dto.idEmpleado = 1L;
        dto.motivo = "Horas extra prueba";
        dto.aprobado = false;
        dto.procesado = false;
        dto.estadoSolicitud = "PENDIENTE";
        dto.tipoTarifa = "SIMPLE";

        assertThrows(BadRequestException.class, () -> servicio.guardar(dto));
    }
}
