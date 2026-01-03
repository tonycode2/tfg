package com.anthony.tfg.tfg.Entidades;

import java.util.List;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name = "direccion")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Direccion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    String provincia;
    String canton;
    String distrito;
    String indicaciones;
    
    @OneToMany(mappedBy = "direccion")
    List<Empleados> empleados;
}
