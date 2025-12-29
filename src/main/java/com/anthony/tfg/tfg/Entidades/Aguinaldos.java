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
@Data
@Table(name = "aguinaldos")
@Builder
public class Aguinaldos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Integer anio;
    @Column(name = "fecha_inicio_periodo")
    Date fechaInicioPeriodo;
    @Column(name = "fecha_fin_periodo")
    Date fechaFinPeriodo;
    @Column(name = "total_salarios_devengados")
    Double totalSalariosDevengados;
    @Column(name = "monto_aguinaldo")
    Double montoAguinaldo;
    @Column(name = "fecha_calculo")
    Date fechaCalculo;
    @Column(name = "fecha_pago")
    Date fechaPago;
}
