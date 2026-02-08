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
 * DTO para el Reporte de Antigüedad de empleados.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteAntiguedadDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;
    private String usuarioGenerador;

    private List<DetalleAntiguedadDTO> empleados;

    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DetalleAntiguedadDTO {
        private String cedula;
        private String nombreCompleto;
        private String departamento;
        private String puesto;

        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
        private LocalDate fechaIngreso;

        private Long anios;
        private Long meses;
        private Long dias;
        private String clasificacion; // "0-1 año", "1-5 años", "5-10 años", ">10 años"
    }
}
