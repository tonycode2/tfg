package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;

public interface PlanillaDetalleRepositorio extends JpaRepository<PlanillaDetalle, Long> {

    @Query("SELECT pd FROM PlanillaDetalle pd WHERE pd.empleado.id = :empleadoId ORDER BY pd.planillaEncabezado.fechaInicioPeriodo DESC")
    List<PlanillaDetalle> findByEmpleadoId(@Param("empleadoId") Long empleadoId);

    @Query("SELECT pd FROM PlanillaDetalle pd WHERE pd.planillaEncabezado.id = :planillaId ORDER BY pd.empleado.primerApellido, pd.empleado.nombre")
    List<PlanillaDetalle> findByPlanillaEncabezadoId(@Param("planillaId") Long planillaId);

    @Query("""
        SELECT COALESCE(SUM(
            COALESCE(pd.salarioBasePeriodo, 0) + COALESCE(pd.montoHorasExtra, 0) + COALESCE(pd.montoIncapacidad, 0)
        ), 0)
        FROM PlanillaDetalle pd
        WHERE pd.empleado.id = :empleadoId
          AND pd.planillaEncabezado.fechaPago BETWEEN :fechaInicio AND :fechaFin
        """)
    Double sumDevengadoByEmpleadoAndFechaPagoBetween(
            @Param("empleadoId") Long empleadoId,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

}
