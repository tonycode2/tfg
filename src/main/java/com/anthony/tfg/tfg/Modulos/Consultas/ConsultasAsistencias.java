package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Modulos.Interfaces.ConsultaInterface;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;

@Service
public class ConsultasAsistencias implements ConsultaInterface<Asistencia>{

    private final AsistenciaRepositorio repo;

    public ConsultasAsistencias(AsistenciaRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param id
     * @return Asistencia
     */
    public Asistencia obtenerPorId(Long id) {
        Optional<Asistencia> asistencia = repo.findById(id);
        return asistencia.orElse(null);
    }

    /** 
     * @return List<Asistencia>
     */
    public List<Asistencia> obtenerTodos() {
        return repo.findAll();
    }
}
