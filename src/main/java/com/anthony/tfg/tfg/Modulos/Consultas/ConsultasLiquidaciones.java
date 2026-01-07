package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Liquidaciones;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.LiquidacionesRepositorio;

@Service
public class ConsultasLiquidaciones implements ConsultaInterface<Liquidaciones>{

    private final LiquidacionesRepositorio repo;

    public ConsultasLiquidaciones(LiquidacionesRepositorio repo) {
        this.repo = repo;
    }

    public Liquidaciones obtenerPorId(Long id) {
        Optional<Liquidaciones> liquidacion = repo.findById(id);
        return liquidacion.orElse(null);
    }

    public Page<Liquidaciones> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

}
