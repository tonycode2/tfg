package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

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

    public List<EvaluacionDeDesempeno> obtenerTodos() {
        return repo.findAll();
    }

}
