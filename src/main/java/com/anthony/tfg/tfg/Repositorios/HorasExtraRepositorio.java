package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anthony.tfg.tfg.Entidades.HorasExtra;

public interface HorasExtraRepositorio extends JpaRepository<HorasExtra, Long> {
	List<HorasExtra> findByEmpleadoIdAndFechaSolicitud(Long empleadoId, LocalDate fechaSolicitud);
	List<HorasExtra> findByEmpleadoIdAndFechaSolicitudAndAprobadoTrue(Long empleadoId, LocalDate fechaSolicitud);
}
