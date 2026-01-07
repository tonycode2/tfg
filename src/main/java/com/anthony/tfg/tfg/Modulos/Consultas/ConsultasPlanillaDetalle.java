package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<PlanillaDetalle> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

    

}
