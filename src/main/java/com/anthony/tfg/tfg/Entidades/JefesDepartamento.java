package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;

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
@Table(name = "jefes_departamento")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JefesDepartamento {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    
    @ManyToOne
    @JoinColumn(name = "id_departamento")
    Departamento departamento;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
    
    @Column(name = "fecha_inicio")
    LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    LocalDate fechaFin;
    
    @Column(name = "esta_activo")
    Boolean estaActivo;
}
