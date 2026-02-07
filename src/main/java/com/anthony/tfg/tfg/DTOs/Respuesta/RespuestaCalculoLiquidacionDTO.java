package com.anthony.tfg.tfg.DTOs.Respuesta;

import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDate;
import java.util.List;

public record RespuestaCalculoLiquidacionDTO(
        Long id,
        Long idEmpleado,
        String nombreEmpleado,
        String primerApellidoEmpleado,
        String segundoApellidoEmpleado,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        LocalDate fechaSalida,
        String motivoSalida,
        Double salarioPromedioDiario,
        Long diasTrabajadosTotal,
        Boolean preaviso_pagado,
        Double montoPreaviso,
        Double montoCesantia,
        Double montoVacacionesPendientes,
        Double montoAguinaldoProporcional,
        Double montoSalarioProporcional,
        Double totalLiquidacion,
        Integer saldoVacaciones,
        String descripcion,
        List<DetalleCalculoLiquidacionDTO> detalles) {
}
