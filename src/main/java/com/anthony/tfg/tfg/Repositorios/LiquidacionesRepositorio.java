package com.anthony.tfg.tfg.Repositorios;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anthony.tfg.tfg.Entidades.Liquidaciones;

public interface LiquidacionesRepositorio extends JpaRepository<Liquidaciones, Long> {
    List<Liquidaciones> findByEmpleadoIdOrderByFechaSalidaDesc(Long empleadoId);

}
