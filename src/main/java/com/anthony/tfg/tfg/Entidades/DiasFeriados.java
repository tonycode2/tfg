package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad que representa los días feriados nacionales.
 * Los feriados se agregan manualmente para cada año, lo que permite flexibilidad
 * cuando un feriado cae en fin de semana y se traslada a otro día.
 */
@Entity
@Getter
@Setter
@Table(name = "dias_feriados")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiasFeriados {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, length = 100)
    private String nombre;
    
    @Column(nullable = false)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate fecha;
    
    @Column(columnDefinition = "TEXT")
    private String descripcion;
}
