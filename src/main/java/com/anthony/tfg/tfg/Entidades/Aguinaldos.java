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
@Getter
@Setter
@Table(name = "aguinaldos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Aguinaldos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    Integer anio;
    @Column(name = "fecha_inicio_periodo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaInicioPeriodo;
    @Column(name = "fecha_fin_periodo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaFinPeriodo;
    @Column(name = "total_salarios_devengados")
    Double totalSalariosDevengados;
    @Column(name = "monto_aguinaldo")
    Double montoAguinaldo;
    @Column(name = "fecha_calculo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaCalculo;
    @Column(name = "fecha_pago")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaPago;
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
