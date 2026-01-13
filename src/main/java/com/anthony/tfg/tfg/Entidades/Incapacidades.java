package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;

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
    LocalDate fechaInicio;

    @Column(name = "fecha_fin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaFin;

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

    @Column(columnDefinition = "TEXT")
    String observaciones;

    @Column(name = "url_documento_adjunto")
    String urlDocumentoAdjunto;

    // Fechas de auditoría
    @Column(name = "fecha_solicitud")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaSolicitud;

    @Column(name = "fecha_aprobacion_jefe")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaAprobacionJefe;

    @Column(name = "fecha_aprobacion_rh")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaAprobacionRH;

    // Comentarios de aprobadores
    @Column(name = "comentarios_jefe", columnDefinition = "TEXT")
    String comentariosJefe;

    @Column(name = "comentarios_rh", columnDefinition = "TEXT")
    String comentariosRH;

    // Relaciones
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
