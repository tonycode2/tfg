package com.anthony.tfg.tfg.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anthony.tfg.tfg.Entidades.JefesDepartamento;

@Repository
public interface JefesDepartamentoRepositorio extends JpaRepository<JefesDepartamento, Long> {
    
    /**
     * Find all active department head assignments for an employee
     */
    @Query("SELECT jd FROM JefesDepartamento jd WHERE jd.empleado.id = :idEmpleado " +
	    "AND jd.estaActivo = true")
    List<JefesDepartamento> findByEmpleadoIdAndEstaActivoTrue(@Param("idEmpleado") Long idEmpleado);
    
    /**
     * Check if an employee is a department head for a specific department
     */
    @Query("SELECT jd FROM JefesDepartamento jd WHERE jd.empleado.id = :idEmpleado " +
	    "AND jd.departamento.id = :idDepartamento AND jd.estaActivo = true")
    Optional<JefesDepartamento> findByEmpleadoIdAndDepartamentoIdAndEstaActivoTrue(
	     @Param("idEmpleado") Long idEmpleado,
	     @Param("idDepartamento") Long idDepartamento);
    
    /**
     * Find all departments managed by an employee
     */
    @Query("SELECT jd.departamento.id FROM JefesDepartamento jd WHERE jd.empleado.id = :idEmpleado " +
	    "AND jd.estaActivo = true")
    List<Long> findDepartamentoIdsByEmpleadoId(@Param("idEmpleado") Long idEmpleado);
}
