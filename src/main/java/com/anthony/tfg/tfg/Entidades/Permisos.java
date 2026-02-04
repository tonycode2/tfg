package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;
import java.util.List;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Entidades.Enums.UnidadTiempo;
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
import jakarta.persistence.OneToMany;
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
    LocalDate fechaInicio;
    
    @Column(name = "fecha_fin")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaFin;
    
    @Column(name = "dias_totales")
    Integer diasTotales;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "unidad_tiempo")
    @Builder.Default
    UnidadTiempo unidadTiempo = UnidadTiempo.DIAS;
    
    @Column(name = "hora_inicio")
    String horaInicio;  // Formato HH:mm para permisos por horas
    
    @Column(name = "hora_fin")
    String horaFin;  // Formato HH:mm para permisos por horas
    
    @Column(name = "total_horas")
    Double totalHoras;  // Total de horas cuando unidadTiempo = HORAS
    
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
    LocalDate fechaSolicitud;
    
    @Column(name = "fecha_aprobacion_jefe")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaAprobacionJefe;
    
    @Column(name = "fecha_aprobacion_rh")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaAprobacionRH;

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
    
    @OneToMany(mappedBy = "permiso")
    List<JornadaDiaria> jornadasDiarias;
}
