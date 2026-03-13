package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;

@Service
public class MantenimientosDirecciones implements MantenimientoInterface<Direccion>{

    private final DireccionRepositorio repo;

    public MantenimientosDirecciones(DireccionRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return Direccion
     */
    public Direccion crear(Direccion entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return Direccion
     */
    public Direccion actualizar(Direccion entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
