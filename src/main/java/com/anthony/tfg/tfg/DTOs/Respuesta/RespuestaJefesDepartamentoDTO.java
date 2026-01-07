package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

public class RespuestaJefesDepartamentoDTO {
    public Long id;
    public Long idDepartamento;
    public String nombreDepartamento;
    public Long idEmpleado;
    public String nombreEmpleado;
    public String primerApellidoEmpleado;
    public String segundoApellidoEmpleado;
    public LocalDate fechaInicio;
    public LocalDate fechaFin;
    public Boolean estaActivo;
}
