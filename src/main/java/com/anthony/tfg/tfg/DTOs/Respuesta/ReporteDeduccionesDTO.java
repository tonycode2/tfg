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
 * DTO para el Reporte de Deducciones Legales.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteDeduccionesDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;
    private String usuarioGenerador;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFinPeriodo;

    private List<DetalleDeduccionesDTO> empleados;

    private Double totalCcssIvm;
    private Double totalCcssSem;
    private Double totalCcssPatrono;
    private Double totalImpuestoRenta;
    private Double totalOtrasDeducciones;
    private Double granTotal;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleDeduccionesDTO {
        private String cedula;
        private String nombreCompleto;
        private String departamento;
        private Double salarioBruto;
        private Double deduccionCcssIvm;
        private Double deduccionCcssSem;
        private Double ccssPatrono;
        private Double impuestoDeRenta;
        private Double otrasDeducciones;
        private Double totalDeducciones;
    }
}
