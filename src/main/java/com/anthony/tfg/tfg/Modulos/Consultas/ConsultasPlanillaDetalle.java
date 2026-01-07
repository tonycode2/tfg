package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.PlanillaDetalle;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

@Service
public class ConsultasPlanillaDetalle implements ConsultaInterface<PlanillaDetalle>{

    private final PlanillaDetalleRepositorio repo;

    public ConsultasPlanillaDetalle(PlanillaDetalleRepositorio repo) {
        this.repo = repo;
    }

    public PlanillaDetalle obtenerPorId(Long id) {
        Optional<PlanillaDetalle> resultado = repo.findById(id);
        return resultado.orElse(null);
    }

    public List<PlanillaDetalle> obtenerTodos() {
        return repo.findAll();
    }

    

}
