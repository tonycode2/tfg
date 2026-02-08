package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para el Reporte de Liquidación de un empleado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteLiquidacionDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;

    // Datos del empleado
    private String cedula;
    private String nombreCompleto;
    private String puesto;
    private String departamento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaIngreso;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaSalida;

    private String motivoSalida;
    private String antiguedadTexto;

    // Cálculos
    private Double salarioPromedioDiario;
    private List<RubroLiquidacionDTO> rubros;
    private Double totalLiquidacion;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RubroLiquidacionDTO {
        private String concepto;
        private String detalle;
        private Double monto;
    }
}
