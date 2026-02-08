package com.anthony.tfg.tfg.Modulos.JefesDepartamento.Servicio;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudJefesDepartamentoDTO;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDepartamentos;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasJefesDepartamento;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosJefesDepartamento;

class ServicioJefesDepartamentoTest {

    @Test
    void deSolicitudDtoAEntidad_departamentoNoExiste_retornaNull() {
        ConsultasJefesDepartamento consulta = mock(ConsultasJefesDepartamento.class);
        MantenimientosJefesDepartamento mantenimiento = mock(MantenimientosJefesDepartamento.class);
        ConsultasDepartamentos consultasDepartamentos = mock(ConsultasDepartamentos.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);

        when(consultasDepartamentos.obtenerPorId(10L)).thenReturn(null);

        ServicioJefesDepartamento servicio = new ServicioJefesDepartamento(
                consulta,
                mantenimiento,
                consultasDepartamentos,
                consultasEmpleados);

        SolicitudJefesDepartamentoDTO dto = new SolicitudJefesDepartamentoDTO();
        dto.idDepartamento = 10L;
        dto.idEmpleado = 5L;
        dto.fechaInicio = LocalDate.now();
        dto.estaActivo = true;

        var resultado = servicio.deSolicitudDtoAEntidad(dto);

        assertNull(resultado);
    }
}
