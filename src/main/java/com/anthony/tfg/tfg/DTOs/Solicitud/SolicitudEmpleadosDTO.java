package com.anthony.tfg.tfg.DTOs.Solicitud;

import java.sql.Date;

import com.anthony.tfg.tfg.Utils.EdadMinima;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class SolicitudEmpleadosDTO {
    Long id;

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

    @NotNull
    @Past
    @EdadMinima(18)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaNacimiento;
    
    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public Date fechaIngreso;

    @NotNull
    @Positive
    public Double salarioBase;

    @NotNull
    @Positive
    public Integer cantidadDeHijos;

    @NotNull
    public Integer saldoVacaciones;

    @Size(min = 22, max = 22)
    public String cuentaIban;

    @NotNull
    public Boolean estaActivo;

    @NotNull
    public Boolean estaCasado;

    @NotBlank
    public String tipoDeJornada;

    @NotNull
    @Positive
    public Long idPuesto;

    @NotNull
    @Positive
    public Long idDireccion;

    // Opcional - se asigna después con el botón "Generar Usuario"
    @Positive
    public Long idUsuario;
}
