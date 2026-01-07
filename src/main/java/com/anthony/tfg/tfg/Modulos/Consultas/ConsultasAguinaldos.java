package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Aguinaldos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;

@Service
public class ConsultasAguinaldos implements  ConsultaInterface<Aguinaldos>{

    private final AguinaldosRepositorio repo;

    public ConsultasAguinaldos(AguinaldosRepositorio repo) {
        this.repo = repo;
    }

    public Aguinaldos obtenerPorId(Long id) {
        Optional<Aguinaldos> aguinaldo = repo.findById(id);
        return aguinaldo.orElse(null);
    }

    public Page<Aguinaldos> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }
}
