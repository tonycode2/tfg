package com.anthony.tfg.tfg.Repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Permisos;

public interface PermisosRepositorio extends JpaRepository<Permisos, Long> {
    
    // Obtener permisos por empleado ordenados por fecha de solicitud
    List<Permisos> findByEmpleadoIdOrderByFechaSolicitudDesc(@Param("idEmpleado") Long idEmpleado);
    
    // Obtener permisos pendientes de un departamento (para jefes)
    @Query("SELECT p FROM Permisos p WHERE p.empleado.puesto.departamento.id = :idDepartamento " +
           "AND p.estadoSolicitud = 'PENDIENTE' " +
           "ORDER BY p.fechaSolicitud ASC")
    List<Permisos> findPermisosPendientesByDepartamento(@Param("idDepartamento") Long idDepartamento);
    
    // Obtener permisos que necesitan aprobación de RH
    @Query("SELECT p FROM Permisos p WHERE p.estadoSolicitud IN ('APROBADA_POR_JEFE', 'PENDIENTE_RH') " +
           "ORDER BY p.fechaSolicitud ASC")
    List<Permisos> findPermisosParaRH();
    
    // Obtener permisos por estado
    List<Permisos> findByEstadoSolicitudOrderByFechaSolicitudDesc(@Param("estadoSolicitud") EstadoSolicitud estadoSolicitud);
    
    // Obtener todas las solicitudes ordenadas por fecha (para auditoría RH)
    List<Permisos> findAllByOrderByFechaSolicitudDesc();
}
