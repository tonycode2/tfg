package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;

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
@Table(name = "permisos")
@Builder
public class Permisos {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_inicio")
    Date fechaInicio;
    @Column(name = "fecha_fin")
    Date fechaFin;
    @Column(name = "dias_totales")
    Integer diasTotales;
    String motivo;
    @Column(name = "url_documento_adjunto")
    String urlDocumentoAdjunto;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud")
    EstadoSolicitud estadoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_permiso")
    TipoPermiso tipoPermiso;
}
