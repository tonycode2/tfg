package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Aguinaldos;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.AguinaldosRepositorio;

@Service
public class MantenimientosAguinaldo implements MantenimientoInterface<Aguinaldos>{

    private final AguinaldosRepositorio repo;

    public MantenimientosAguinaldo(AguinaldosRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return Aguinaldos
     */
    public Aguinaldos crear(Aguinaldos entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return Aguinaldos
     */
    public Aguinaldos actualizar(Aguinaldos entidad) {
        return repo.save(entidad);
    }
    
    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
