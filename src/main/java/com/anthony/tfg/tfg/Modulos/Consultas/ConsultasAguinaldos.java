package com.anthony.tfg.tfg.Modulos.Consultas;

import java.util.List;
import java.util.Optional;

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

    /** 
     * @param id
     * @return Aguinaldos
     */
    public Aguinaldos obtenerPorId(Long id) {
        Optional<Aguinaldos> aguinaldo = repo.findById(id);
        return aguinaldo.orElse(null);
    }

    /** 
     * @return List<Aguinaldos>
     */
    public List<Aguinaldos> obtenerTodos() {
        return repo.findAll();
    }

    /**
     * Obtiene los aguinaldos de un empleado específico.
     * Los resultados se ordenan por año descendente.
     * @param idEmpleado el ID del empleado
     * @return Lista de aguinaldos del empleado
     */
    public List<Aguinaldos> obtenerPorEmpleadoId(Long idEmpleado) {
        return repo.findByEmpleadoIdOrderByAnioDesc(idEmpleado);
    }
}
