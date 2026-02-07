package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;

import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;
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
@Table(name = "liquidaciones")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Liquidaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "fecha_salida")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaSalida;

    @Column(name = "monto_preaviso")
    Double montoPreaviso;

    @Column(name = "monto_cesantia")
    Double montoCesantia;

    @Column(name = "monto_vacaciones_pendientes")
    Double montoVacacionesPendientes;

    @Column(name = "monto_aguinaldo_pendiente")
    Double montoAguinaldoPendiente;

    @Column(name = "monto_salario_proporcional")
    Double montoSalarioProporcional;

    @Column(name = "total_liquidacion")
    Double totalLiquidacion;

    @Column(name = "salario_promedio_diario")
    Double salarioPromedioDiario;

    @Column(name = "dias_trabajados_total")
    Long diasTrabajadosTotal;

    @Column(name = "preaviso_pagado")
    Boolean preaviso_pagado;

    @Column(name = "descripcion", columnDefinition = "TEXT")
    String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_salida")
    MotivoSalida motivoSalida;

    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}

