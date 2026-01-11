package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

public class RespuestaEmpleadosDTO {
    public Long id;
    public String cedula;
    public String nombre;
    public String primerApellido;
    public String segundoApellido;
    public String correoPersonal;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaNacimiento;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    public LocalDate fechaIngreso;
    public Double salarioBase;
    public Integer cantidadDeHijos;
    public Integer saldoVacaciones;
    public String cuentaIban;
    public Boolean estaActivo;
    public Boolean estaCasado;
    public String tipoDeJornada;
    public PuestoInfo puesto;
    public DireccionInfo direccion;
    public String nombreUsuario;
    
    public static class PuestoInfo {
        public Long id;
        public String nombre;
        public Double salarioMinimo;
        public DepartamentoInfo departamento;
    }
    
    public static class DepartamentoInfo {
        public Long id;
        public String nombre;
    }
    
    public static class DireccionInfo {
        public Long id;
        public String provincia;
        public String canton;
        public String distrito;
        public String direccionExacta;
    }
}
