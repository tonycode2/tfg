package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoIncapacidad;
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
@Table(name = "incapacidades")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Incapacidades {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;

    @Column(name = "fecha_inicio")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaInicio;

    @Column(name = "fecha_fin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaFin;

    @Column(name = "dias_totales")
    Integer diasTotales;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_incapacidad")
    TipoIncapacidad tipoIncapacidad;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud")
    EstadoSolicitud estadoSolicitud;

    @Column(name = "porcentaje_pago")
    Double porcentajePago;

    @Enumerated(EnumType.STRING)
    @Column(name = "entidad_emisora")
    TipoEntidadEmisora entidadEmisora;

    @Column(name = "numero_documento")
    String numeroDocumento;

    String observaciones;

    @Column(name = "url_documento_adjunto")
    String urlDocumentoAdjunto;

    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
}
