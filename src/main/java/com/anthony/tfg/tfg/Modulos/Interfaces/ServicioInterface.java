package com.anthony.tfg.tfg.Modulos.Interfaces;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ServicioInterface<T, K, J> { //T: Respuesta, K: Solicitud, J: Entidad
    public T obtenerPorId(Long id);
    public Page<T> obtenerTodos(Pageable pageable);
    public T guardar(K entidad);
    public T actualizar(Long id, K entidad);
    public void eliminar(Long id);
    public J deSolicitudDtoAEntidad(K solicitud);
    public T deEntidadDtoARespuesta(J entidad);
    public List<T> deListaEntidadADto(List<J> entidades);
}
