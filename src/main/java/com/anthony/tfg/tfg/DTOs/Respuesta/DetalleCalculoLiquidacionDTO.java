package com.anthony.tfg.tfg.DTOs.Respuesta;

public record DetalleCalculoLiquidacionDTO(
        String concepto,
        String formula,
        Double monto) {
}
