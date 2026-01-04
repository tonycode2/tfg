package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SolicitudDepartamentoDTO {
    @NotNull
    public Long id;
    @NotNull
    @Size(min = 3, max = 100)
    public String nombre;
}
