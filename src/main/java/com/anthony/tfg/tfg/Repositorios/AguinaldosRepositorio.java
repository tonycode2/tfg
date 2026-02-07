package com.anthony.tfg.tfg.Repositorios;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anthony.tfg.tfg.Entidades.Aguinaldos;

public interface AguinaldosRepositorio extends JpaRepository<Aguinaldos, Long> {

	Optional<Aguinaldos> findByEmpleadoIdAndAnio(Long empleadoId, Integer anio);

}
