package com.anthony.tfg.tfg.DTOs.Respuesta;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * DTO para la Colilla de Pago individual de un empleado.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ColillaPagoDTO {

    private String nombreEmpresa;
    private String tituloReporte;
    private String fechaGeneracion;

    // Datos del empleado
    private Long idEmpleado;
    private String cedula;
    private String nombreCompleto;
    private String puesto;
    private String departamento;
    private String cuentaIban;

    // Datos del período
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaInicioPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaFinPeriodo;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fechaPago;

    private String tipoQuincena;

    // Ingresos
    private Double salarioBase;
    private Double montoHorasExtra;
    private Double montoIncapacidad;
    private Double totalDevengado;

    // Deducciones
    private Double deduccionCcssIvm;
    private Double deduccionCcssSem;
    private Double impuestoDeRenta;
    private Double otrasDeducciones;
    private Double totalDeducciones;

    // Neto
    private Double salarioNeto;
}
