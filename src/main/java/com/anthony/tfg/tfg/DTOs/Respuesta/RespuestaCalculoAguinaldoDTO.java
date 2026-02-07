package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.sql.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

public record RespuestaCalculoAguinaldoDTO(
        Long id,
        Long idEmpleado,
        String nombreEmpleado,
        String primerApellidoEmpleado,
        String segundoApellidoEmpleado,
        Integer anio,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date fechaInicioPeriodo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date fechaFinPeriodo,
        Double totalSalariosDevengados,
        Double montoAguinaldo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date fechaCalculo,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        Date fechaPago
) {
}
