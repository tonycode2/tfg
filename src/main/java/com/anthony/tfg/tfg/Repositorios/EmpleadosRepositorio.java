package com.anthony.tfg.tfg.Repositorios;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.Entidades.Empleados;

public interface EmpleadosRepositorio extends JpaRepository<Empleados, Long> {
    
    @Modifying
    @Query(value = "DELETE FROM permisos WHERE id_empleado = :id", nativeQuery = true)
    void deletePermisosByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM asistencias WHERE id_empleado = :id", nativeQuery = true)
    void deleteAsistenciasByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM horas_extra WHERE id_empleado = :id", nativeQuery = true)
    void deleteHorasExtraByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM aguinaldos WHERE id_empleado = :id", nativeQuery = true)
    void deleteAguinaldosByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM evaluaciones_de_desempeno WHERE id_empleado = :id", nativeQuery = true)
    void deleteEvaluacionesByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM liquidaciones WHERE id_empleado = :id", nativeQuery = true)
    void deleteLiquidacionesByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM planilla_detalle WHERE id_empleado = :id", nativeQuery = true)
    void deletePlanillaDetallesByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM empleados WHERE id = :id", nativeQuery = true)
    void deleteEmpleadoById(@Param("id") Long id);
    
    @Query(value = "SELECT id_usuario FROM empleados WHERE id = :id", nativeQuery = true)
    Long getUsuarioIdByEmpleadoId(@Param("id") Long id);
    
    @Query(value = "SELECT id_direccion FROM empleados WHERE id = :id", nativeQuery = true)
    Long getDireccionIdByEmpleadoId(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM users WHERE id = :id", nativeQuery = true)
    void deleteUsuarioById(@Param("id") Long id);
    
    @Modifying
    @Query(value = "DELETE FROM direccion WHERE id = :id", nativeQuery = true)
    void deleteDireccionById(@Param("id") Long id);
    
        /**
         * Find all active employees in a specific department
         */
        @Query("SELECT e FROM Empleados e WHERE e.puesto.departamento.id = :idDepartamento " +
            "AND e.estaActivo = true")
        List<Empleados> findByDepartamentoIdAndEstaActivoTrue(@Param("idDepartamento") Long idDepartamento);
    
        /**
         * Find employee by user ID
         */
        @Query("SELECT e FROM Empleados e WHERE e.usuario.id = :idUsuario")
        Optional<Empleados> findByUsuarioId(@Param("idUsuario") Long idUsuario);
    
        /**
         * Find all active employees
         */
        List<Empleados> findByEstaActivoTrue();
}
