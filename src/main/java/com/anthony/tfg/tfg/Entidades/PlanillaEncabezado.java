package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;
import java.util.List;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;
import com.fasterxml.jackson.annotation.JsonFormat;

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
    Date fechaInicioPeriodo;
    @Column(name = "fecha_fin_periodo")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaFinPeriodo;
    @Column(name = "fecha_pago")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    Date fechaPago;
    @Column(name = "total_planilla_bruto")
    Double totalPlanillaBruto;
    @Column(name = "total_planilla_neto")
    Double totalPlanillaNeto;
    @Column(name = "estado_planilla")
    @Enumerated(EnumType.STRING)
    EstadoPlanilla estadoPlanilla;
    
    @OneToMany(mappedBy = "planillaEncabezado")
    List<PlanillaDetalle> detalles;
}
