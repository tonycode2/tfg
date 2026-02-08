package com.anthony.tfg.tfg.Modulos.Empleados.Servicio;

import java.util.List;

import org.springframework.stereotype.Service;

import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaEmpleadosDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudEmpleadosDTO;
import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Entidades.Enums.TipoDeJornada;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasDirecciones;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasPuestos;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosEmpleados;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioEmpleados implements ServicioInterface<RespuestaEmpleadosDTO, 
                                                            SolicitudEmpleadosDTO, 
                                                            Empleados>{

    private final ConsultasEmpleados consulta;
    private final MantenimientosEmpleados mantenimiento;
    private final ConsultasPuestos consultasPuestos;
    private final ConsultasDirecciones consultasDirecciones;
    private final UserRepository userRepository;

    public ServicioEmpleados(ConsultasEmpleados consulta, MantenimientosEmpleados mantenimiento, 
                            ConsultasPuestos consultasPuestos, ConsultasDirecciones consultasDirecciones,
                            UserRepository userRepository) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasPuestos = consultasPuestos;
        this.consultasDirecciones = consultasDirecciones;
        this.userRepository = userRepository;
    }

    public RespuestaEmpleadosDTO obtenerPorId(Long id) {
        Empleados empleado = consulta.obtenerPorId(id);
        if(empleado == null){
            log.warn("No se ha encontrado el empleado con ID: " + id);
            throw new ResourceNotFoundException("Empleados", "id", id);
        }
        log.info("Se ha encontrado el empleado con ID: " + id);
        return deEntidadDtoARespuesta(empleado);
    }

    public List<RespuestaEmpleadosDTO> obtenerTodos() {
        List<Empleados> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todos los empleados. La cantidad de registros es: " + entidades.size());
        return deListaEntidadADto(entidades);
    }

    public RespuestaEmpleadosDTO guardar(SolicitudEmpleadosDTO entidad) {
        Empleados nuevoEmpleado = deSolicitudDtoAEntidad(entidad);
        Empleados empleadoGuardado = mantenimiento.crear(nuevoEmpleado);
        log.info("Se ha guardado un nuevo empleado con ID: " + empleadoGuardado.getId());
        return deEntidadDtoARespuesta(empleadoGuardado);
    }

    public RespuestaEmpleadosDTO actualizar(Long id, SolicitudEmpleadosDTO entidad) {
        Empleados empleadoExistente = consulta.obtenerPorId(id);
        if(empleadoExistente == null){
            log.warn("No se ha encontrado el empleado con ID: " + id + " para actualizar");
            return null;
        }
        empleadoExistente.setCedula(entidad.cedula);
        empleadoExistente.setNombre(entidad.nombre);
        empleadoExistente.setPrimerApellido(entidad.primerApellido);
        empleadoExistente.setSegundoApellido(entidad.segundoApellido);
        empleadoExistente.setCorreoPersonal(entidad.correoPersonal);
        empleadoExistente.setFechaNacimiento(entidad.fechaNacimiento);
        empleadoExistente.setFechaIngreso(entidad.fechaIngreso);
        empleadoExistente.setCantidadDeHijos(entidad.cantidadDeHijos);
        empleadoExistente.setSaldoVacaciones(entidad.saldoVacaciones);
        empleadoExistente.setCuentaIban(entidad.cuentaIban);
        empleadoExistente.setEstaActivo(entidad.estaActivo);
        empleadoExistente.setEstaCasado(entidad.estaCasado);
        
        TipoDeJornada tipoJornada = obtenerTipoDeJornada(entidad.tipoDeJornada);
        if(tipoJornada != null){
            empleadoExistente.setTipoDeJornada(tipoJornada);
        }
        
        Puestos puesto = consultasPuestos.obtenerPorId(entidad.idPuesto);
        if(puesto != null){
            empleadoExistente.setPuesto(puesto);
        }
        
        Direccion direccion = consultasDirecciones.obtenerPorId(entidad.idDireccion);
        if(direccion != null){
            empleadoExistente.setDireccion(direccion);
        }
        
        // Usuario es opcional - solo se actualiza si se proporciona
        if(entidad.idUsuario != null){
            User usuario = userRepository.findById(entidad.idUsuario).orElse(null);
            if(usuario != null){
                empleadoExistente.setUsuario(usuario);
            }
        }
        
        Empleados empleadoActualizado = mantenimiento.actualizar(empleadoExistente);
        log.info("Se ha actualizado el empleado con ID: " + id);
        return deEntidadDtoARespuesta(empleadoActualizado);
    }

    public void eliminar(Long id) {
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado el empleado con ID: " + id);
    }

    public Empleados deSolicitudDtoAEntidad(SolicitudEmpleadosDTO solicitud) {
        if(solicitud == null){
            log.warn("El DTO de solicitud es nulo, no se puede convertir a entidad Empleados.");
            return null;
        }
        
        TipoDeJornada tipoJornada = obtenerTipoDeJornada(solicitud.tipoDeJornada);
        if(tipoJornada == null){
            log.warn("No se ha encontrado el tipo de jornada: " + solicitud.tipoDeJornada);
            return null;
        }
        
        Puestos puesto = consultasPuestos.obtenerPorId(solicitud.idPuesto);
        if(puesto == null){
            log.warn("No se ha encontrado el puesto con ID: " + solicitud.idPuesto);
            return null;
        }
        
        Direccion direccion = consultasDirecciones.obtenerPorId(solicitud.idDireccion);
        if(direccion == null){
            log.warn("No se ha encontrado la dirección con ID: " + solicitud.idDireccion);
            return null;
        }
        
        // Usuario es opcional - solo se asigna después con el botón "Generar Usuario"
        User usuario = null;
        if(solicitud.idUsuario != null){
            usuario = userRepository.findById(solicitud.idUsuario).orElse(null);
            if(usuario == null){
                log.warn("No se ha encontrado el usuario con ID: " + solicitud.idUsuario);
            }
        }
        
        Empleados empleado = Empleados.builder()
                    .cedula(solicitud.cedula)
                    .nombre(solicitud.nombre)
                    .primerApellido(solicitud.primerApellido)
                    .segundoApellido(solicitud.segundoApellido)
                    .correoPersonal(solicitud.correoPersonal)
                    .fechaNacimiento(solicitud.fechaNacimiento)
                    .fechaIngreso(solicitud.fechaIngreso)
                    .cantidadDeHijos(solicitud.cantidadDeHijos)
                    .saldoVacaciones(solicitud.saldoVacaciones)
                    .cuentaIban(solicitud.cuentaIban)
                    .estaActivo(solicitud.estaActivo)
                    .estaCasado(solicitud.estaCasado)
                    .tipoDeJornada(tipoJornada)
                    .puesto(puesto)
                    .direccion(direccion)
                    .usuario(usuario)
                    .build();
        log.info("Se ha convertido el DTO de solicitud a entidad Empleados: {}", empleado);
        return empleado;
    }

    public RespuestaEmpleadosDTO deEntidadDtoARespuesta(Empleados entidad) {
        if(entidad == null){
            log.warn("La entidad Empleados es nula, no se puede convertir a DTO de respuesta.");
            return null;
        }
        RespuestaEmpleadosDTO respuesta = new RespuestaEmpleadosDTO();
        respuesta.id = entidad.getId();
        respuesta.cedula = entidad.getCedula();
        respuesta.nombre = entidad.getNombre();
        respuesta.primerApellido = entidad.getPrimerApellido();
        respuesta.segundoApellido = entidad.getSegundoApellido();
        respuesta.correoPersonal = entidad.getCorreoPersonal();
        respuesta.fechaNacimiento = entidad.getFechaNacimiento();
        respuesta.fechaIngreso = entidad.getFechaIngreso();
        respuesta.cantidadDeHijos = entidad.getCantidadDeHijos();
        respuesta.saldoVacaciones = entidad.getSaldoVacaciones();
        respuesta.cuentaIban = entidad.getCuentaIban();
        respuesta.estaActivo = entidad.getEstaActivo();
        respuesta.estaCasado = entidad.getEstaCasado();
        
        if(entidad.getTipoDeJornada() != null){
            respuesta.tipoDeJornada = entidad.getTipoDeJornada().name();
        }
        
        if(entidad.getPuesto() != null){
            RespuestaEmpleadosDTO.PuestoInfo puestoInfo = new RespuestaEmpleadosDTO.PuestoInfo();
            puestoInfo.id = entidad.getPuesto().getId();
            puestoInfo.nombre = entidad.getPuesto().getNombre();
            puestoInfo.salarioMinimo = entidad.getPuesto().getSalarioMinimo();
            
            if(entidad.getPuesto().getDepartamento() != null){
                RespuestaEmpleadosDTO.DepartamentoInfo deptInfo = new RespuestaEmpleadosDTO.DepartamentoInfo();
                deptInfo.id = entidad.getPuesto().getDepartamento().getId();
                deptInfo.nombre = entidad.getPuesto().getDepartamento().getNombre();
                puestoInfo.departamento = deptInfo;
            }
            
            respuesta.puesto = puestoInfo;
        }
        
        if(entidad.getDireccion() != null){
            RespuestaEmpleadosDTO.DireccionInfo dirInfo = new RespuestaEmpleadosDTO.DireccionInfo();
            dirInfo.id = entidad.getDireccion().getId();
            dirInfo.provincia = entidad.getDireccion().getProvincia();
            dirInfo.canton = entidad.getDireccion().getCanton();
            dirInfo.distrito = entidad.getDireccion().getDistrito();
            dirInfo.direccionExacta = entidad.getDireccion().getIndicaciones();
            respuesta.direccion = dirInfo;
        }
        
        if(entidad.getUsuario() != null){
            respuesta.nombreUsuario = entidad.getUsuario().getUsername();
        }
        
        log.info("Se ha convertido la entidad Empleados a DTO de respuesta: {}", respuesta);
        return respuesta;
    }

    public List<RespuestaEmpleadosDTO> deListaEntidadADto(List<Empleados> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }
    
    private TipoDeJornada obtenerTipoDeJornada(String tipo) {
        try {
            return TipoDeJornada.valueOf(tipo.toUpperCase());
        } catch (Exception e) {
            return null;
        }
    }

}
