package com.anthony.tfg.tfg.Entidades;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "planilla_detalle")
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlanillaDetalle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "salario_base_periodo")
    Double salarioBasePeriodo;
    @Column(name = "cantidad_dias_feriados")
    Integer cantidadDiasFeriados;
    @Column(name = "monto_horas_extra")
    Double montoHorasExtra;
    @Column(name = "monto_incapacidad")
    Double montoIncapacidad;
    @Column(name = "deducciones_ccss_ivm")
    Double deduccionCcssIvm;
    @Column(name = "deducciones_ccss_sem")
    Double deduccionCcssSem;
    @Column(name = "impuesto_de_renta")
    Double impuestoDeRenta;
    @Column(name = "otras_deducciones")
    Double otrasDeducciones;
    
    @ManyToOne
    @JoinColumn(name = "id_empleado")
    Empleados empleado;
    
    @ManyToOne
    @JoinColumn(name = "id_encabezado_planilla")
    PlanillaEncabezado planillaEncabezado;

}
