package com.anthony.tfg.tfg.Repositorios;

import java.sql.Date;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anthony.tfg.tfg.Entidades.Incapacidades;

public interface IncapacidadesRepositorio extends JpaRepository<Incapacidades, Long> {
    
    List<Incapacidades> findByEmpleadoId(Long idEmpleado);
    
    List<Incapacidades> findByFechaInicioBetween(Date fechaInicio, Date fechaFin);
    
    List<Incapacidades> findByEmpleadoIdAndFechaInicioBetween(Long idEmpleado, Date fechaInicio, Date fechaFin);
}
