package com.anthony.tfg.tfg.Modulos.Mantenimientos;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.Entidades.Liquidaciones;
import com.anthony.tfg.tfg.Modulos.Interfaces.MantenimientoInterface;
import com.anthony.tfg.tfg.Repositorios.LiquidacionesRepositorio;

@Service
public class MantenimientosLiquidaciones implements MantenimientoInterface<Liquidaciones>{

    private final LiquidacionesRepositorio repo;

    public MantenimientosLiquidaciones(LiquidacionesRepositorio repo) {
        this.repo = repo;
    }

    /** 
     * @param entidad
     * @return Liquidaciones
     */
    public Liquidaciones crear(Liquidaciones entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param entidad
     * @return Liquidaciones
     */
    public Liquidaciones actualizar(Liquidaciones entidad) {
        return repo.save(entidad);
    }

    /** 
     * @param id
     */
    public void eliminar(Long id) {
        repo.deleteById(id);
    }

}
