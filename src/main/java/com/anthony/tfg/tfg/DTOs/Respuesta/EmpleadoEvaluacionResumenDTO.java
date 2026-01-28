package com.anthony.tfg.tfg.DTOs.Respuesta;

/**
 * Projection DTO for employee evaluation summary (average and count)
 */
public class EmpleadoEvaluacionResumenDTO {
    public Long empleadoId;
    public String nombre;
    public String primerApellido;
    public String segundoApellido;
    public String puestoNombre;
    public Double promedioPuntuacion;
    public Long cantidadEvaluaciones;

    public EmpleadoEvaluacionResumenDTO(Long empleadoId, String nombre, String primerApellido, String segundoApellido,
            String puestoNombre, Double promedioPuntuacion, Long cantidadEvaluaciones) {
        this.empleadoId = empleadoId;
        this.nombre = nombre;
        this.primerApellido = primerApellido;
        this.segundoApellido = segundoApellido;
        this.puestoNombre = puestoNombre;
        this.promedioPuntuacion = promedioPuntuacion;
        this.cantidadEvaluaciones = cantidadEvaluaciones;
    }

    public EmpleadoEvaluacionResumenDTO() {
    }
}
