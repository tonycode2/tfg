package com.anthony.tfg.tfg.Modulos.Interfaces;

import java.util.List;

public interface ServicioInterface<T, K, J> { //T: Respuesta, K: Solicitud, J: Entidad
    public T obtenerPorId(Long id);
    public List<T> obtenerTodos();
    public T guardar(K entidad);
    public T actualizar(Long id, K entidad);
    public void eliminar(Long id);
    public J deSolicitudDtoAEntidad(K solicitud);
    public T deEntidadDtoARespuesta(J entidad);
    public List<T> deListaEntidadADto(List<J> entidades);
}
