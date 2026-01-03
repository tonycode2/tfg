package com.anthony.tfg.tfg.Modulos.Interfaces;

public interface MantenimientoInterface <T, K>{
    public T crear(K k);
    public T actualizar(K k);
    public void eliminar(Long id);
}
