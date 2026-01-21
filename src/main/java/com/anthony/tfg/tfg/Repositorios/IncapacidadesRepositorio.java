package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.Entidades.Incapacidades;

public interface IncapacidadesRepositorio extends JpaRepository<Incapacidades, Long> {
    
    // Obtener incapacidades por empleado ordenadas por fecha de solicitud DESC
    List<Incapacidades> findByEmpleadoIdOrderByFechaSolicitudDesc(@Param("idEmpleado") Long idEmpleado);
    
    // Obtener incapacidades pendientes de un departamento (para jefes)
    @Query("SELECT i FROM Incapacidades i WHERE i.empleado.puesto.departamento.id = :idDepartamento " +
           "AND i.estadoSolicitud = 'PENDIENTE' " +
           "ORDER BY i.fechaSolicitud ASC")
    List<Incapacidades> findIncapacidadesPendientesByDepartamento(@Param("idDepartamento") Long idDepartamento);
    
    // Obtener incapacidades que necesitan aprobación de RH
    @Query("SELECT i FROM Incapacidades i WHERE i.estadoSolicitud IN ('APROBADA_POR_JEFE', 'PENDIENTE_RH') " +
           "ORDER BY i.fechaSolicitud ASC")
    List<Incapacidades> findIncapacidadesParaRH();
    
    // Obtener todas las incapacidades ordenadas por fecha (para auditoría RH)
    List<Incapacidades> findAllByOrderByFechaSolicitudDesc();
    
    // Obtener incapacidades por rango de fechas (para reportes)
    List<Incapacidades> findByFechaInicioBetween(LocalDate fechaInicio, LocalDate fechaFin);
    
    // Obtener incapacidades activas (aprobadas y en curso)
    @Query("SELECT i FROM Incapacidades i WHERE i.estadoSolicitud = 'APROBADA' " +
           "AND i.fechaInicio <= :fecha AND i.fechaFin >= :fecha " +
           "ORDER BY i.empleado.puesto.departamento.nombre, i.empleado.primerApellido")
    List<Incapacidades> findIncapacidadesActivas(@Param("fecha") LocalDate fecha);
    
    // Obtener incapacidades activas de un departamento específico
    @Query("SELECT i FROM Incapacidades i WHERE i.estadoSolicitud = 'APROBADA' " +
           "AND i.empleado.puesto.departamento.id = :idDepartamento " +
           "AND i.fechaInicio <= :fecha AND i.fechaFin >= :fecha " +
           "ORDER BY i.empleado.primerApellido")
    List<Incapacidades> findIncapacidadesActivasByDepartamento(@Param("idDepartamento") Long idDepartamento, 
                                                                @Param("fecha") LocalDate fecha);
}
