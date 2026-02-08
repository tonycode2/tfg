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
 * DTO para el Reporte de Vacaciones.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVacacionesDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;
    private String usuarioGenerador;

    private List<DetalleVacacionesDTO> empleados;
    private Integer totalDiasPendientes;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleVacacionesDTO {
        private String cedula;
        private String nombreCompleto;
        private String departamento;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaIngreso;

        private Integer diasAcumulados;
        private Integer diasDisfrutados;
        private Integer diasPendientes;
        private Boolean tieneVencidos;
    }
}
