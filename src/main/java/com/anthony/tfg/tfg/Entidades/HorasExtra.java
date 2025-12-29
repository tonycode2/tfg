package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoTarifa;

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
@Table(name = "horas_extra")
@Builder
public class HorasExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_solicitud")
    Date fechaSolicitud;
    @Column(name = "cantidad_de_horas")
    Integer cantidadDeHoras;
    String motivo;
    Boolean aprobado;
    Boolean procesado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud")
    EstadoSolicitud estadoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa")
    TipoTarifa tipoTarifa;
}
