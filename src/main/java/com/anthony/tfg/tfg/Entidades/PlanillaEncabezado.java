package com.anthony.tfg.tfg.Entidades;

import java.time.LocalDate;
import java.util.List;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
@Table(name = "planilla_encabezado")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanillaEncabezado {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "fecha_inicio_periodo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaInicioPeriodo;
    @Column(name = "fecha_fin_periodo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaFinPeriodo;
    @Column(name = "fecha_pago")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate fechaPago;
    @Column(name = "tipo_quincena")
    @Enumerated(EnumType.STRING)
    TipoQuincena tipoQuincena;
    @Column(name = "total_planilla_bruto")
    Double totalPlanillaBruto;
    @Column(name = "total_planilla_neto")
    Double totalPlanillaNeto;
    @Column(name = "estado_planilla")
    @Enumerated(EnumType.STRING)
    EstadoPlanilla estadoPlanilla;
    
    // Eliminación en cascada para evitar referencias huérfanas en detalles al borrar planillas.
    @OneToMany(mappedBy = "planillaEncabezado", cascade = CascadeType.ALL, orphanRemoval = true)
    List<PlanillaDetalle> detalles;
}
