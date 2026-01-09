package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;

@Repository
public interface AsistenciaRepositorio extends JpaRepository<Asistencia, Long> {
    
    /**
     * Find all attendance records for a specific employee
     */
    List<Asistencia> findByEmpleadoId(Long idEmpleado);
    
    /**
     * Find all attendance records for a specific employee within a date range
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado.id = :idEmpleado " +
	    "AND a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
	    "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByEmpleadoIdAndFechaHoraBetween(
	     @Param("idEmpleado") Long idEmpleado,
	     @Param("fechaInicio") LocalDateTime fechaInicio,
	     @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Find all attendance records within a date range (for all employees)
     */
    @Query("SELECT a FROM Asistencia a WHERE a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
	    "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByFechaHoraBetween(
	     @Param("fechaInicio") LocalDateTime fechaInicio,
	     @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Find the last attendance record for a specific employee
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado.id = :idEmpleado " +
	    "ORDER BY a.fechaHora DESC LIMIT 1")
    Optional<Asistencia> findUltimoRegistroByEmpleadoId(@Param("idEmpleado") Long idEmpleado);
    
    /**
     * Find the last ENTRADA (clock-in) record for a specific employee that doesn't have a matching SALIDA
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado.id = :idEmpleado " +
	    "AND a.tipoEvento = :tipoEvento " +
	    "ORDER BY a.fechaHora DESC LIMIT 1")
    Optional<Asistencia> findUltimoRegistroByEmpleadoIdAndTipoEvento(
	     @Param("idEmpleado") Long idEmpleado,
	     @Param("tipoEvento") TipoEvento tipoEvento);
    
    /**
     * Find today's attendance records for a specific employee
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado.id = :idEmpleado " +
	    "AND CAST(a.fechaHora AS date) = CAST(:fecha AS date) " +
	    "ORDER BY a.fechaHora ASC")
    List<Asistencia> findByEmpleadoIdAndFecha(
	     @Param("idEmpleado") Long idEmpleado,
	     @Param("fecha") LocalDateTime fecha);
    
    /**
     * Find all attendance records for employees in a specific department (via puesto)
     */
    @Query("SELECT a FROM Asistencia a WHERE a.empleado.puesto.departamento.id = :idDepartamento " +
	    "AND a.fechaHora BETWEEN :fechaInicio AND :fechaFin " +
	    "ORDER BY a.fechaHora DESC")
    List<Asistencia> findByDepartamentoIdAndFechaHoraBetween(
	     @Param("idDepartamento") Long idDepartamento,
	     @Param("fechaInicio") LocalDateTime fechaInicio,
	     @Param("fechaFin") LocalDateTime fechaFin);
    
    /**
     * Find the last attendance record for each employee in a department (for status summary)
     */
    @Query(value = "SELECT a.* FROM asistencias a " +
	    "INNER JOIN empleados e ON a.id_empleado = e.id " +
	    "INNER JOIN puestos p ON e.id_puesto = p.id " +
	    "WHERE p.id_departamento = :idDepartamento " +
	    "AND a.id IN (SELECT MAX(a2.id) FROM asistencias a2 INNER JOIN empleados e2 ON a2.id_empleado = e2.id " +
	    "WHERE e2.id = e.id) ", nativeQuery = true)
    List<Asistencia> findUltimosRegistrosByDepartamentoId(@Param("idDepartamento") Long idDepartamento);
}
