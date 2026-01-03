package com.anthony.tfg.tfg.Modulos.Interfaces;

public interface MantenimientoInterface <T>{
    public T crear(T entidad);
    public T actualizar(T entidad);
    public void eliminar(Long id);
}
