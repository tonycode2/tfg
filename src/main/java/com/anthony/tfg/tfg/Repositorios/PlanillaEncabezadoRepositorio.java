package com.anthony.tfg.tfg.Repositorios;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anthony.tfg.tfg.Entidades.PlanillaEncabezado;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;

public interface PlanillaEncabezadoRepositorio extends JpaRepository<PlanillaEncabezado, Long> {

	boolean existsByFechaInicioPeriodoAndFechaFinPeriodoAndTipoQuincena(
		LocalDate fechaInicioPeriodo,
		LocalDate fechaFinPeriodo,
		TipoQuincena tipoQuincena);

}
