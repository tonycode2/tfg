package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;

@Service
public class ConsultasJefesDepartamento implements ConsultaInterface<JefesDepartamento>{

    private final JefesDepartamentoRepositorio repo;

    public ConsultasJefesDepartamento(JefesDepartamentoRepositorio repo) {
        this.repo = repo;
    }

    public JefesDepartamento obtenerPorId(Long id) {
        Optional<JefesDepartamento> resultado = repo.findById(id);
        return resultado.orElse(null);
    }

    public List<JefesDepartamento> obtenerTodos() {
        return repo.findAll();
    }
}
