package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;
import java.time.LocalTime;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "jornada_diaria")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JornadaDiaria {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @Column(name = "fecha")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fecha;
    
    @Column(name = "hora_entrada")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaEntrada;
    
    @Column(name = "hora_salida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss")
    LocalTime horaSalida;
    
    @Column(name = "horas_regulares")
    Double horasRegulares;
    
    @Column(name = "horas_extra")
    Double horasExtra;
    
    @Column(name = "observaciones")
    String observaciones;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
