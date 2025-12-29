package com.anthony.tfg.tfg.Modulos.Interfaces;

public interface MantenimientoInterface <T>{
    public T crear(T t);
    public T actualizar(T t);
    public void eliminar(Long id);
}
