package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudEmpleadosDTO {
    // @NotBlank //Revisar si esto debe seguir o no comentado
    // Long id;

    @NotBlank
    @Size(min = 9, max = 12)
    String cedula;

    @NotBlank
    @Size(min = 2, max = 100)
    String nombre;
    
    @NotBlank
    @Size(min = 2, max = 100)
    String primerApellido;

    @NotBlank
    @Size(min = 2, max = 100)
    String segundoApellido;

    @NotBlank
    @Size(min = 2, max = 100)
    @Email
    String correoPersonal;

    @NotBlank
    @Past
    Date fechaNacimiento;
    
    @NotBlank
    @Future //Esto se puede cambiar dependiendo de como se maneje. Puede que no sea futuro. 
    Date fechaIngreso;

    @NotBlank
    Double salarioBase;

    @NotBlank
    @Positive
    Integer cantidadDeHijos;

    @NotBlank
    Integer saldoVacaciones;

    @NotBlank
    @Size(min = 22, max = 22)
    String cuentaIban;

    @NotBlank
    Boolean estaActivo;

    @NotBlank
    Boolean estaCasado;

    @NotBlank
    @Positive
    Long idTipoDeJornada;

    @NotBlank
    @Positive
    Long idPuesto;

    @NotBlank
    @Positive
    Long idDireccion;

    @NotBlank
    @Positive
    Long idUsuario;
}
