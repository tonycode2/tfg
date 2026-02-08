package com.anthony.tfg.tfg.Modulos.DiasFeriados.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudDiasFeriadosDTO;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDiasFeriados;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosDiasFeriados;

class ServicioDiasFeriadosTest {

    @Test
    void guardar_fechaNoFutura_lanzaExcepcion() {
        ConsultasDiasFeriados consulta = mock(ConsultasDiasFeriados.class);
        MantenimientosDiasFeriados mantenimiento = mock(MantenimientosDiasFeriados.class);

        when(consulta.esFeriado(LocalDate.now())).thenReturn(false);

        ServicioDiasFeriados servicio = new ServicioDiasFeriados(consulta, mantenimiento);

        SolicitudDiasFeriadosDTO dto = new SolicitudDiasFeriadosDTO();
        dto.nombre = "Feriado";
        dto.fecha = LocalDate.now();
        dto.descripcion = "Prueba";

        assertThrows(BadRequestException.class, () -> servicio.guardar(dto));
    }
}
