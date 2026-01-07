package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;

@Service
public class ConsultasDepartamentos implements ConsultaInterface<Departamento>{
    private final DepartamentoRepositorio repo;
    
    public ConsultasDepartamentos(DepartamentoRepositorio repo) {
        this.repo = repo;
    }
    
    public Departamento obtenerPorId(Long id) {
        Optional<Departamento> departamento = repo.findById(id);
        return departamento.orElse(null);
    }

    public Page<Departamento> obtenerTodos(Pageable pageable) {
        return repo.findAll(pageable);
    }

}
