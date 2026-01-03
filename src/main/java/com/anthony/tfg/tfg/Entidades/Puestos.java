package com.anthony.tfg.tfg.Entidades;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "puestos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Puestos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String nombre;
    @Column(name = "salario_minimo")
    Double salarioMinimo;
    
    @ManyToOne
    @JoinColumn(name = "id_departamento")
    Departamento departamento;
    
    @OneToMany(mappedBy = "puesto")
    List<Empleados> empleados;
}
