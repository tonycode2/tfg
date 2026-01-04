package com.anthony.tfg.tfg.DTOs.Solicitud;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SolicitudDireccionDTO {
    @NotNull
    public Long id;
    @NotNull
    @Size(min = 5, max = 100)
    public String provincia;
    @NotNull
    @Size(min = 5, max = 100)
    public String canton;
    @NotNull
    @Size(min = 5, max = 100)
    public String distrito;
    @NotNull
    @Size(min = 5, max = 100)
    public String indicaciones;
}
