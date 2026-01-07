package com.anthony.tfg.tfg.Modulos.Interfaces;

import java.util.List;

public interface ConsultaInterface<T> {
    public T obtenerPorId(Long id);
    public List<T> obtenerTodos();
}
