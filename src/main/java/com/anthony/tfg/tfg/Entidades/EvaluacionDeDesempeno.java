package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Table(name = "evaluaciones_de_desempeno")
@Data
@Builder
public class EvaluacionDeDesempeno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "empleado_id")
    Date fechaEvaluacion;
    @Column(name = "empleado_nombre")
    String periodoEvaluado;
    @Column(name = "puntuacion_final")
    Double puntuacionFinal;
    String observaciones;
    @Column(name = "plan_de_mejora")
    String planDeMejora;
}
