package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

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

    /** 
     * @param id
     * @return Liquidaciones
     */
    public Liquidaciones obtenerPorId(Long id) {
        Optional<Liquidaciones> liquidacion = repo.findById(id);
        return liquidacion.orElse(null);
    }

    /** 
     * @return List<Liquidaciones>
     */
    public List<Liquidaciones> obtenerTodos() {
        return repo.findAll();
    }

}
