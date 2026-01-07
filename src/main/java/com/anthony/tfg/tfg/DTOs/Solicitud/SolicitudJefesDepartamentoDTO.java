package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDate;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SolicitudJefesDepartamentoDTO {
    public Long id;
    
    @NotNull
    @Positive
    public Long idDepartamento;
    
    @NotNull
    @Positive
    public Long idEmpleado;
    
    @NotNull
    public LocalDate fechaInicio;
    
    public LocalDate fechaFin;
    
    @NotNull
    public Boolean estaActivo;
}
