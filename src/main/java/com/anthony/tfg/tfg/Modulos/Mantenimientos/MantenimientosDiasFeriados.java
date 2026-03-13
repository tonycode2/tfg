package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.DiasFeriados;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;

/**
 * Servicio de mantenimiento para días feriados.
 * Maneja las operaciones CRUD básicas.
 */
@Service
public class MantenimientosDiasFeriados implements MantenimientoInterface<DiasFeriados> {
    
    private final DiasFeriadosRepositorio repo;
    
    public MantenimientosDiasFeriados(DiasFeriadosRepositorio repo) {
        this.repo = repo;
    }
    
    /** 
     * @param entidad
     * @return DiasFeriados
     */
    @Override
    public DiasFeriados crear(DiasFeriados entidad) {
        return repo.save(entidad);
    }
    
    /** 
     * @param entidad
     * @return DiasFeriados
     */
    @Override
    public DiasFeriados actualizar(DiasFeriados entidad) {
        return repo.save(entidad);
    }
    
    /** 
     * @param id
     */
    @Override
    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}
