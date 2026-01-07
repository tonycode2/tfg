package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.EvaluacionDeDesempeno;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.EvaluacionDeDesempenoRepositorio;

@Service
public class ConsultasEvaluacionDeDesempeno implements ConsultaInterface<EvaluacionDeDesempeno>{

    private final EvaluacionDeDesempenoRepositorio repo;

    public ConsultasEvaluacionDeDesempeno(EvaluacionDeDesempenoRepositorio repo) {
        this.repo = repo;
    }

    public EvaluacionDeDesempeno obtenerPorId(Long id) {
        Optional<EvaluacionDeDesempeno> evaluacion = repo.findById(id);
        return evaluacion.orElse(null);
    }

    public Page<EvaluacionDeDesempeno> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

}
