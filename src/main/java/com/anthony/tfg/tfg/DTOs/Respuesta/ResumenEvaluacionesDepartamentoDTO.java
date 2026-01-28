package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.util.List;

/**
 * DTO representing evaluation summaries for a single department
 */
public class ResumenEvaluacionesDepartamentoDTO {
    public Long departamentoId;
    public String departamentoNombre;
    public List<EmpleadoEvaluacionResumenDTO> empleados;

    public ResumenEvaluacionesDepartamentoDTO() {
    }

    public ResumenEvaluacionesDepartamentoDTO(Long departamentoId, String departamentoNombre,
            List<EmpleadoEvaluacionResumenDTO> empleados) {
        this.departamentoId = departamentoId;
        this.departamentoNombre = departamentoNombre;
        this.empleados = empleados;
    }
}
