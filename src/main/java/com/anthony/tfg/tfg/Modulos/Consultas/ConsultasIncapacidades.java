package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;

@Service
public class ConsultasIncapacidades implements ConsultaInterface<Incapacidades> {

    private final IncapacidadesRepositorio repo;

    public ConsultasIncapacidades(IncapacidadesRepositorio repo) {
        this.repo = repo;
    }

    public Incapacidades obtenerPorId(Long id) {
        Optional<Incapacidades> incapacidad = repo.findById(id);
        return incapacidad.orElse(null);
    }

    public List<Incapacidades> obtenerTodos() {
        return repo.findAll();
    }

    public List<Incapacidades> obtenerPorEmpleado(Long idEmpleado) {
        return repo.findByEmpleadoId(idEmpleado);
    }
}
