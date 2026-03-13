package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

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
    
    /** 
     * @param id
     * @return Departamento
     */
    public Departamento obtenerPorId(Long id) {
        Optional<Departamento> departamento = repo.findById(id);
        return departamento.orElse(null);
    }

    /** 
     * @return List<Departamento>
     */
    public List<Departamento> obtenerTodos() {
        return repo.findAll();
    }

}
