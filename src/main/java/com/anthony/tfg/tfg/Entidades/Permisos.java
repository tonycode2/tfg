package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
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
@Table(name = "permisos")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Permisos {
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
    
    @Column(columnDefinition = "TEXT")
    String motivo;
    
    @Column(name = "url_documento_adjunto")
    String urlDocumentoAdjunto;
    
    @Column(name = "observaciones_empleado", columnDefinition = "TEXT")
    String observacionesEmpleado;
    
    @Column(name = "comentarios_jefe", columnDefinition = "TEXT")
    String comentariosJefe;
    
    @Column(name = "comentarios_rh", columnDefinition = "TEXT")
    String comentariosRH;
    
    @Column(name = "fecha_solicitud")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaSolicitud;
    
    @Column(name = "fecha_aprobacion_jefe")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaAprobacionJefe;
    
    @Column(name = "fecha_aprobacion_rh")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaAprobacionRH;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_solicitud")
    EstadoSolicitud estadoSolicitud;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_permiso")
    TipoPermiso tipoPermiso;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
    
    @ManyToOne
    @JoinColumn(name = "id_aprobador_jefe")
    Empleados aprobadorJefe;
    
    @ManyToOne
    @JoinColumn(name = "id_aprobador_rh")
    Empleados aprobadorRH;
}
