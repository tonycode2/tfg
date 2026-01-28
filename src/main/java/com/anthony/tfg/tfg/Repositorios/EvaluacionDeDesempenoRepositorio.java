package com.anthony.tfg.tfg.Repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.DTOs.Respuesta.EmpleadoEvaluacionResumenDTO;
import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;

public interface EvaluacionDeDesempenoRepositorio extends JpaRepository<EvaluacionDeDesempeno, Long> {

	    @Query("SELECT new com.anthony.tfg.tfg.DTOs.Respuesta.EmpleadoEvaluacionResumenDTO(" +
		    "emp.id, emp.nombre, emp.primerApellido, emp.segundoApellido, emp.puesto.nombre, AVG(ev.puntuacionFinal), COUNT(ev)) " +
		    "FROM Empleados emp " +
		    "LEFT JOIN emp.evaluaciones ev " +
		    "WHERE emp.puesto.departamento.id = :departamentoId " +
		    "GROUP BY emp.id, emp.nombre, emp.primerApellido, emp.segundoApellido, emp.puesto.nombre " +
		    "ORDER BY AVG(ev.puntuacionFinal) DESC")
	    List<EmpleadoEvaluacionResumenDTO> findResumenPorDepartamento(@Param("departamentoId") Long departamentoId);

	java.util.List<EvaluacionDeDesempeno> findByEmpleadoId(Long empleadoId);

}
