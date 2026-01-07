package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDateTime;

import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaAsistenciaDTO {
    public Long id;
    public TipoEvento tipoEvento;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    public LocalDateTime fechaHora;
    public String observaciones;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
}
