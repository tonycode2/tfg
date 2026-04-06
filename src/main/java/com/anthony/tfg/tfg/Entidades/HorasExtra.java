package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoTarifa;
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
@Table(name = "horas_extra")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HorasExtra {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_solicitud")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaSolicitud;
    @Column(name = "cantidad_de_horas")
    Integer cantidadDeHoras;
    String motivo;
    Boolean aprobado;
    Boolean procesado;
    
    @Column(name = "comentarios_rh", columnDefinition = "TEXT")
    String comentariosRH;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud")
    EstadoSolicitud estadoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_tarifa")
    TipoTarifa tipoTarifa;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
