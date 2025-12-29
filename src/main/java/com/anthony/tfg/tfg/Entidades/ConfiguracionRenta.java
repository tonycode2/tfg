package com.anthony.tfg.tfg.Entidades;

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
@Table(name = "configuracion_renta")
@Builder
public class ConfiguracionRenta {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "monto_minimo")
    Double montoMinimo;
    @Column(name = "monto_maximo")
    Double montoMaximo;
    Double porcentaje;
}
