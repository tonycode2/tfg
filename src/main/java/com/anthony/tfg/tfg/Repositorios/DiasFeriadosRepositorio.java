package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.Entidades.DiasFeriados;

/**
 * Repositorio para la gestión de días feriados.
 * Proporciona métodos para validar fechas contra feriados.
 */
public interface DiasFeriadosRepositorio extends JpaRepository<DiasFeriados, Long> {
    
    /**
     * Verifica si una fecha específica es un día feriado.
     * @param fecha La fecha a verificar
     * @return Optional con el feriado si existe, vacío si no
     */
    Optional<DiasFeriados> findByFecha(LocalDate fecha);
    
    /**
     * Obtiene todos los feriados dentro de un rango de fechas.
     * @param fechaInicio Fecha de inicio del rango (inclusivo)
     * @param fechaFin Fecha de fin del rango (inclusivo)
     * @return Lista de feriados en el rango
     */
    @Query("SELECT df FROM DiasFeriados df WHERE df.fecha >= :fechaInicio AND df.fecha <= :fechaFin ORDER BY df.fecha ASC")
    List<DiasFeriados> findByFechaBetween(@Param("fechaInicio") LocalDate fechaInicio, @Param("fechaFin") LocalDate fechaFin);
    
    /**
     * Verifica si existe un feriado en la fecha especificada.
     * @param fecha La fecha a verificar
     * @return true si la fecha es un feriado
     */
    boolean existsByFecha(LocalDate fecha);
    
    /**
     * Obtiene todos los feriados de un año específico.
     * @param anio El año a consultar
     * @return Lista de feriados del año
     */
    @Query("SELECT df FROM DiasFeriados df WHERE YEAR(df.fecha) = :anio ORDER BY df.fecha ASC")
    List<DiasFeriados> findByAnio(@Param("anio") int anio);
    
    /**
     * Obtiene todos los feriados ordenados por fecha descendente.
     * @return Lista de todos los feriados ordenados
     */
    List<DiasFeriados> findAllByOrderByFechaDesc();
}
