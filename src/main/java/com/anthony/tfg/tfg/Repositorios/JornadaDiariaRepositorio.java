package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anthony.tfg.tfg.Entidades.JornadaDiaria;

@Repository
public interface JornadaDiariaRepositorio extends JpaRepository<JornadaDiaria, Long> {
    
    /**
     * Find all daily records for a specific employee
     */
    List<JornadaDiaria> findByEmpleadoId(Long idEmpleado);
    
    /**
     * Find daily record for a specific employee on a specific date
     */
    Optional<JornadaDiaria> findByEmpleadoIdAndFecha(Long idEmpleado, LocalDate fecha);
    
    /**
     * Find all daily records within a date range for a specific employee
     */
    @Query("SELECT j FROM JornadaDiaria j WHERE j.empleado.id = :idEmpleado " +
           "AND j.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY j.fecha DESC")
    List<JornadaDiaria> findByEmpleadoIdAndFechaBetween(
            @Param("idEmpleado") Long idEmpleado,
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Find all daily records within a date range (for all employees)
     */
    @Query("SELECT j FROM JornadaDiaria j WHERE j.fecha BETWEEN :fechaInicio AND :fechaFin " +
           "ORDER BY j.fecha DESC")
    List<JornadaDiaria> findByFechaBetween(
            @Param("fechaInicio") LocalDate fechaInicio,
            @Param("fechaFin") LocalDate fechaFin);

    @Query("SELECT COALESCE(MAX(j.diaPermiso), 0) FROM JornadaDiaria j WHERE j.permiso.id = :idPermiso")
    Integer findMaxDiaPermisoByPermisoId(@Param("idPermiso") Long idPermiso);

    @Query("SELECT COALESCE(MAX(j.diaPermiso), 0) FROM JornadaDiaria j WHERE j.incapacidad.id = :idIncapacidad")
    Integer findMaxDiaPermisoByIncapacidadId(@Param("idIncapacidad") Long idIncapacidad);
}
