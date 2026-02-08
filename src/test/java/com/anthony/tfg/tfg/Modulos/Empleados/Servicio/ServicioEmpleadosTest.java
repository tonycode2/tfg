package com.anthony.tfg.tfg.Modulos.Empleados.Servicio;

import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEmpleadosDTO;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDirecciones;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPuestos;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEmpleados;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;

class ServicioEmpleadosTest {

    @Test
    void deSolicitudDtoAEntidad_tipoJornadaInvalido_retornaNull() {
        ConsultasEmpleados consulta = mock(ConsultasEmpleados.class);
        MantenimientosEmpleados mantenimiento = mock(MantenimientosEmpleados.class);
        ConsultasPuestos consultasPuestos = mock(ConsultasPuestos.class);
        ConsultasDirecciones consultasDirecciones = mock(ConsultasDirecciones.class);
        UserRepository userRepository = mock(UserRepository.class);

        ServicioEmpleados servicio = new ServicioEmpleados(
                consulta,
                mantenimiento,
                consultasPuestos,
                consultasDirecciones,
                userRepository);

        SolicitudEmpleadosDTO dto = new SolicitudEmpleadosDTO();
        dto.tipoDeJornada = "INVALIDO";

        var resultado = servicio.deSolicitudDtoAEntidad(dto);

        assertNull(resultado);
    }
}
