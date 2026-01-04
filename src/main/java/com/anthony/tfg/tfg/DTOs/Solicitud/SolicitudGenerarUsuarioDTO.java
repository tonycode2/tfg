package com.anthony.tfg.tfg.DTOs.Solicitud;

import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import jakarta.validation.constraints.NotNull;

public class SolicitudGenerarUsuarioDTO {
    // Se asigna desde el path parameter en el controlador
    public Long idEmpleado;
    
    @NotNull
    public Role role;
}
