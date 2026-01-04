package com.anthony.tfg.tfg.DTOs.Respuesta;

public class RespuestaCredencialesDTO {
    public String username;
    public String password;
    public String correoEmpleado;
    public String nombreCompleto;
    
    public RespuestaCredencialesDTO(String username, String password, String correoEmpleado, String nombreCompleto) {
        this.username = username;
        this.password = password;
        this.correoEmpleado = correoEmpleado;
        this.nombreCompleto = nombreCompleto;
    }
}
