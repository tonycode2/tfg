package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "asistencias")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Asistencia {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_evento")
    TipoEvento tipoEvento;
    @Column(name = "fecha_hora")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    LocalDateTime fechaHora;
    
    String observaciones;
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
