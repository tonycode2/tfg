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
 * DTO para el Reporte de Planilla Mensual.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportePlanillaDTO {

    private String nombreEmpresa;
    private String tituloReporte;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFinPeriodo;

    private String fechaGeneracion;
    private String usuarioGenerador;
    private String tipoQuincena;

    private List<DetallePlanillaReporteDTO> empleados;

    private Double totalBruto;
    private Double totalDeducciones;
    private Double totalNeto;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetallePlanillaReporteDTO {
        private String cedula;
        private String nombreCompleto;
        private String puesto;
        private String departamento;
        private Double salarioBase;
        private Double montoHorasExtra;
        private Double montoFeriadosTrabajados;
        private Double montoIncapacidad;
        private Double totalDevengado;
        private Double deduccionCcssIvm;
        private Double deduccionCcssSem;
        private Double impuestoDeRenta;
        private Double otrasDeducciones;
        private Double totalDeducciones;
        private Double salarioNeto;
    }
}
