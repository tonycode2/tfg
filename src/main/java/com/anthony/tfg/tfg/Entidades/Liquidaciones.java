package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Data;

@Entity
@Data
@Table(name = "liquidaciones")
@Builder
public class Liquidaciones {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_entrada")
    Date fechaSalida;
    @Column(name = "monto_preaviso")
    Double montoPreaviso;
    @Column(name = "monto_cesantia")
    Double montoCesantia;
    @Column(name = "monto_vacaciones_pendientes")
    Double montoVacacionesPendientes;
    @Column(name = "monto_aguinaldo_pendiente")
    Double montoAguinaldoPendiente;
    @Column(name = "total_liquidacion")
    Double totalLiquidacion;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_salida")
    MotivoSalida motivoSalida;
}
