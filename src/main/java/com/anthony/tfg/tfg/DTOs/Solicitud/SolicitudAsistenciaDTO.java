package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.time.LocalDateTime;

import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class SolicitudAsistenciaDTO {
    public Long id;

    @NotNull
    public TipoEvento tipoEvento;

    @NotNull
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime fechaHora;

    public String observaciones;
    
    @NotNull
    @Positive
    public Long idEmpleado;
}
