package com.anthony.tfg.tfg.Entidades;

import java.sql.Date;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoPlanilla;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
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
    Date fechaInicioPeriodo;
    @Column(name = "fecha_fin_periodo")
    Date fechaFinPeriodo;
    @Column(name = "fecha_pago")
    Date fechaPago;
    @Column(name = "total_planilla_bruto")
    Double totalPlanillaBruto;
    @Column(name = "total_planilla_neto")
    Double totalPlanillaNeto;
    @Column(name = "estado_planilla")
    @Enumerated(EnumType.STRING)
    EstadoPlanilla estadoPlanilla;
}
