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
    public String cedula;

    @NotBlank
    @Size(min = 2, max = 100)
    public String nombre;
    
    @NotBlank
    @Size(min = 2, max = 100)
    public String primerApellido;

    @NotBlank
    @Size(min = 2, max = 100)
    public String segundoApellido;

    @NotBlank
    @Size(min = 2, max = 100)
    @Email
    public String correoPersonal;

    @NotBlank
    @Past
    public Date fechaNacimiento;
    
    @NotBlank
    @Future //Esto se puede cambiar dependiendo de como se maneje. Puede que no sea futuro. 
    public Date fechaIngreso;

    @NotBlank
    public Double salarioBase;

    @NotBlank
    @Positive
    public Integer cantidadDeHijos;

    @NotBlank
    public Integer saldoVacaciones;

    @NotBlank
    @Size(min = 22, max = 22)
    public String cuentaIban;

    @NotBlank
    public Boolean estaActivo;

    @NotBlank
    public Boolean estaCasado;

    @NotBlank
    public String tipoDeJornada;

    @NotBlank
    @Positive
    public Long idPuesto;

    @NotBlank
    @Positive
    public Long idDireccion;

    @NotBlank
    @Positive
    public Long idUsuario;
}
