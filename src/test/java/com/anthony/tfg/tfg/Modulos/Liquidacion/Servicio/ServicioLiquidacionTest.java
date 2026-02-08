package com.anthony.tfg.tfg.Modulos.Liquidacion.Servicio;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasLiquidaciones;
import com.anthony.tfg.tfg.Modulos.Liquidacion.Servicios.LiquidacionesCalculoServicio;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosLiquidaciones;

class ServicioLiquidacionTest {

    @Test
    void calcularYGuardar_empleadoNoExiste_lanzaExcepcion() {
        ConsultasLiquidaciones consulta = mock(ConsultasLiquidaciones.class);
        MantenimientosLiquidaciones mantenimiento = mock(MantenimientosLiquidaciones.class);
        ConsultasEmpleados consultasEmpleados = mock(ConsultasEmpleados.class);
        LiquidacionesCalculoServicio calculoServicio = mock(LiquidacionesCalculoServicio.class);

        when(consultasEmpleados.obtenerPorId(99L)).thenReturn(null);

        ServicioLiquidacion servicio = new ServicioLiquidacion(
                consulta,
                mantenimiento,
                consultasEmpleados,
                calculoServicio);

        SolicitudCalculoLiquidacionDTO dto = new SolicitudCalculoLiquidacionDTO();
        dto.setIdEmpleado(99L);
        dto.setFechaSalida(LocalDate.now());
        dto.setMotivoSalida("RENUNCIA_VOLUNTARIA");
        dto.setPreaviso_pagado(false);

        assertThrows(ResourceNotFoundException.class, () -> servicio.calcularYGuardar(dto));
    }
}
