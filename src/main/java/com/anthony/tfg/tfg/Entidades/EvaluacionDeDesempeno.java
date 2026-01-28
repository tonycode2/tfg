package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

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
@Table(name = "evaluaciones_de_desempeno")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvaluacionDeDesempeno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_evaluacion")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaEvaluacion;
    @Column(name = "periodo_evaluado")
    String periodoEvaluado;
    @Column(name = "puntuacion_final")
    Double puntuacionFinal;
    String observaciones;
    @Column(name = "plan_de_mejora")
    String planDeMejora;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
