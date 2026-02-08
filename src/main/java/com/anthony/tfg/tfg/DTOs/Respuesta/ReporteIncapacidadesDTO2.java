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
 * DTO para el Reporte de Incapacidades.
 * Nombre con sufijo "2" para evitar conflicto con RespuestaIncapacidadesDTO existente.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteIncapacidadesDTO2 {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;
    private String usuarioGenerador;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFinPeriodo;

    private List<DetalleIncapacidadReporteDTO> incapacidades;

    private Integer totalDiasCCSS;
    private Integer totalDiasINS;
    private Integer totalDiasOtros;
    private Integer totalDiasGeneral;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleIncapacidadReporteDTO {
        private String cedula;
        private String nombreCompleto;
        private String departamento;
        private String tipoIncapacidad;
        private String entidadEmisora;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaInicio;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaFin;

        private Integer diasTotales;
        private String estado;
    }
}
