package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;
import java.sql.Time;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Table(name = "asistencias")
@Builder
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Date fecha;
    @Column(name = "hora_entrada")
    Time horaEntrada;
    @Column(name = "hora_salida")
    Time horaSalida;
    @Column(name = "horas_trabajadas")
    Double horasTrabajadas;
}
