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
 * DTO para el Reporte de Aguinaldo.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAguinaldoDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;
    private String usuarioGenerador;

    // Datos del empleado
    private String cedula;
    private String nombreCompleto;
    private String puesto;
    private String departamento;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFinPeriodo;

    private Integer anio;

    // Desglose de salarios mensuales
    private List<DetalleMensualAguinaldoDTO> mesesDetalle;

    // Totales
    private Double totalSalariosDevengados;
    private Double montoAguinaldo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleMensualAguinaldoDTO {
        private String mes; // "Enero", "Febrero", etc.
        private Integer anioMes;
        private Integer mesNumero; // 1-12
        private Double salarioBruto;
    }
}
