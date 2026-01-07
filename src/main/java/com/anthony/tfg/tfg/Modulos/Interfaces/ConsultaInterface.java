package com.anthony.tfg.tfg.Modulos.Interfaces;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ConsultaInterface<T> {
    public T obtenerPorId(Long id);
    public Page<T> obtenerTodos(Pageable pageable);
}
