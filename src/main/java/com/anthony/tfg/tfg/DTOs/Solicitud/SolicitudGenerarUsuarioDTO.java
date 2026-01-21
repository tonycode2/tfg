package com.anthony.tfg.tfg.DTOs.Solicitud;

import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SolicitudGenerarUsuarioDTO {
    // Se asigna desde el path parameter en el controlador
    public Long idEmpleado;
    
    @NotNull
    public Role role;
}
