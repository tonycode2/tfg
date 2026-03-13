package com.anthony.tfg.tfg.Modulos.Liquidacion.Servicio;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.DTOs.Respuesta.DetalleCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Respuesta.RespuestaLiquidacionesDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudCalculoLiquidacionDTO;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudLiquidacionesDTO;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Liquidaciones;
import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;
import com.anthony.tfg.tfg.Exceptions.BadRequestException;
import com.anthony.tfg.tfg.Exceptions.ResourceNotFoundException;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasEmpleados;
import com.anthony.tfg.tfg.Modulos.Consultas.ConsultasLiquidaciones;
import com.anthony.tfg.tfg.Modulos.Interfaces.ServicioInterface;
import com.anthony.tfg.tfg.Modulos.Liquidacion.Servicios.LiquidacionesCalculoServicio;
import com.anthony.tfg.tfg.Modulos.Liquidacion.Servicios.LiquidacionesCalculoServicio.ResultadoCalculo;
import com.anthony.tfg.tfg.Modulos.Mantenimientos.MantenimientosLiquidaciones;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class ServicioLiquidacion implements ServicioInterface<RespuestaLiquidacionesDTO,
                                                            SolicitudLiquidacionesDTO,
                                                            Liquidaciones> {

    private final ConsultasLiquidaciones consulta;
    private final MantenimientosLiquidaciones mantenimiento;
    private final ConsultasEmpleados consultasEmpleados;
    private final LiquidacionesCalculoServicio calculoServicio;

    public ServicioLiquidacion(ConsultasLiquidaciones consulta,
                                MantenimientosLiquidaciones mantenimiento,
                                ConsultasEmpleados consultasEmpleados,
                                LiquidacionesCalculoServicio calculoServicio) {
        this.consulta = consulta;
        this.mantenimiento = mantenimiento;
        this.consultasEmpleados = consultasEmpleados;
        this.calculoServicio = calculoServicio;
    }

    // ===================== CÁLCULO DE LIQUIDACIÓN =====================

    /**
     * Realiza un calculo de negocio segun los datos de entrada.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    @Transactional
    public RespuestaCalculoLiquidacionDTO calcularYGuardar(SolicitudCalculoLiquidacionDTO solicitud) {
        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", solicitud.getIdEmpleado());
        }

        MotivoSalida motivoSalida = obtenerMotivoSalida(solicitud.getMotivoSalida());
        if (motivoSalida == null) {
            throw new BadRequestException("Motivo de salida inválido: " + solicitud.getMotivoSalida()
                    + ". Valores permitidos: RENUNCIA_VOLUNTARIA, DESPIDO_CON_RESPONSABILIDAD, DESPIDO_SIN_RESPONSABILIDAD, "
                    + "FINALIZACION_CONTRATO, JUBILACION, MUERTE, MUTUO_ACUERDO");
        }

        ResultadoCalculo resultado = calculoServicio.calcularLiquidacionCompleta(
                empleado,
                solicitud.getFechaSalida(),
                motivoSalida,
                Boolean.TRUE.equals(solicitud.getPreaviso_pagado()));

        Liquidaciones liquidacion = Liquidaciones.builder()
                .fechaSalida(solicitud.getFechaSalida())
                .montoPreaviso(resultado.montoPreaviso())
                .montoCesantia(resultado.montoCesantia())
                .montoVacacionesPendientes(resultado.montoVacacionesPendientes())
                .montoAguinaldoPendiente(resultado.montoAguinaldoProporcional())
                .montoSalarioProporcional(resultado.montoSalarioProporcional())
                .totalLiquidacion(resultado.totalLiquidacion())
                .salarioPromedioDiario(resultado.salarioPromedioDiario())
                .diasTrabajadosTotal(resultado.diasTrabajadosTotal())
                .preaviso_pagado(resultado.preaviso_pagado())
                .descripcion(solicitud.getDescripcion())
                .motivoSalida(motivoSalida)
                .empleado(empleado)
                .build();

        Liquidaciones guardada = mantenimiento.crear(liquidacion);
        log.info("Liquidación calculada y guardada con ID: {} para empleado: {}",
                guardada.getId(), empleado.getId());

        List<DetalleCalculoLiquidacionDTO> detalles = construirDetalles(resultado);

        return new RespuestaCalculoLiquidacionDTO(
                guardada.getId(),
                empleado.getId(),
                empleado.getNombre(),
                empleado.getPrimerApellido(),
                empleado.getSegundoApellido(),
                guardada.getFechaSalida(),
                motivoSalida.name(),
                resultado.salarioPromedioDiario(),
                resultado.diasTrabajadosTotal(),
                resultado.preaviso_pagado(),
                resultado.montoPreaviso(),
                resultado.montoCesantia(),
                resultado.montoVacacionesPendientes(),
                resultado.montoAguinaldoProporcional(),
                resultado.montoSalarioProporcional(),
                resultado.totalLiquidacion(),
                resultado.saldoVacaciones(),
                solicitud.getDescripcion(),
                detalles);
    }

    /**
     * Ejecuta la logica principal de construirDetalles.
     * @param resultado parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private List<DetalleCalculoLiquidacionDTO> construirDetalles(ResultadoCalculo resultado) {
        List<DetalleCalculoLiquidacionDTO> detalles = new ArrayList<>();

        long anios = resultado.diasTrabajadosTotal() / 365;
        long meses = (resultado.diasTrabajadosTotal() % 365) / 30;
        long dias = resultado.diasTrabajadosTotal() % 30;
        String antiguedadStr = anios + " años, " + meses + " meses, " + dias + " días";

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Antigüedad",
                resultado.diasTrabajadosTotal() + " días (" + antiguedadStr + ")",
                (double) resultado.diasTrabajadosTotal()));

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Salario promedio diario",
                "Promedio últimos 6 meses de planilla",
                resultado.salarioPromedioDiario()));

        if (resultado.preaviso_pagado() && resultado.montoPreaviso() > 0) {
            detalles.add(new DetalleCalculoLiquidacionDTO(
                    "Preaviso",
                    resultado.diasPreaviso() + " días × ₡" + String.format("%,.2f", resultado.salarioPromedioDiario()),
                    resultado.montoPreaviso()));
        } else {
            detalles.add(new DetalleCalculoLiquidacionDTO(
                    "Preaviso",
                    "No aplica (será trabajado o menos de 3 meses)",
                    0.0));
        }

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Cesantía",
                resultado.montoCesantia() > 0
                        ? "Tabla Art. 29 Código de Trabajo (máx. 8 años)"
                        : "No aplica para este motivo de salida",
                resultado.montoCesantia()));

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Aguinaldo proporcional",
                "Salarios devengados dic-salida / 12",
                resultado.montoAguinaldoProporcional()));

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Vacaciones pendientes",
                resultado.saldoVacaciones() + " días × ₡" + String.format("%,.2f", resultado.salarioPromedioDiario()),
                resultado.montoVacacionesPendientes()));

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "Salario proporcional",
                "Días del mes de salida × ₡" + String.format("%,.2f", resultado.salarioPromedioDiario()),
                resultado.montoSalarioProporcional()));

        detalles.add(new DetalleCalculoLiquidacionDTO(
                "TOTAL LIQUIDACIÓN",
                "Suma de todos los componentes",
                resultado.totalLiquidacion()));

        return detalles;
    }

    // ===================== CRUD ESTÁNDAR =====================

    /**
     * Obtiene un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaLiquidacionesDTO obtenerPorId(Long id) {
        Liquidaciones liquidacion = consulta.obtenerPorId(id);
        if (liquidacion == null) {
            log.warn("No se ha encontrado la liquidación con ID: {}", id);
            throw new ResourceNotFoundException("Liquidaciones", "id", id);
        }
        log.info("Se ha encontrado la liquidación con ID: {}", id);
        return deEntidadDtoARespuesta(liquidacion);
    }

    /**
     * Obtiene todos los registros disponibles.
     * @return resultado de la operacion.
     */
    public List<RespuestaLiquidacionesDTO> obtenerTodos() {
        List<Liquidaciones> entidades = consulta.obtenerTodos();
        log.info("Se han obtenido todas las liquidaciones. Cantidad: {}", entidades.size());
        return deListaEntidadADto(entidades);
    }

    /**
     * Guarda un nuevo registro.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaLiquidacionesDTO guardar(SolicitudLiquidacionesDTO entidad) {
        Liquidaciones nuevaLiquidacion = deSolicitudDtoAEntidad(entidad);
        if (nuevaLiquidacion == null) {
            throw new BadRequestException("No se pudo crear la liquidación con los datos proporcionados");
        }
        Liquidaciones liquidacionGuardada = mantenimiento.crear(nuevaLiquidacion);
        log.info("Se ha guardado una nueva liquidación con ID: {}", liquidacionGuardada.getId());
        return deEntidadDtoARespuesta(liquidacionGuardada);
    }

    /**
     * Actualiza un registro existente.
     * @param id parametro de entrada de la operacion.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaLiquidacionesDTO actualizar(Long id, SolicitudLiquidacionesDTO entidad) {
        Liquidaciones liquidacionExistente = consulta.obtenerPorId(id);
        if (liquidacionExistente == null) {
            throw new ResourceNotFoundException("Liquidaciones", "id", id);
        }
        liquidacionExistente.setFechaSalida(entidad.getFechaSalida());
        liquidacionExistente.setMontoPreaviso(entidad.getMontoPreaviso());
        liquidacionExistente.setMontoCesantia(entidad.getMontoCesantia());
        liquidacionExistente.setMontoVacacionesPendientes(entidad.getMontoVacacionesPendientes());
        liquidacionExistente.setMontoAguinaldoPendiente(entidad.getMontoAguinaldoPendiente());
        liquidacionExistente.setTotalLiquidacion(entidad.getTotalLiquidacion());

        MotivoSalida motivoSalida = obtenerMotivoSalida(entidad.getMotivoSalida());
        if (motivoSalida != null) {
            liquidacionExistente.setMotivoSalida(motivoSalida);
        }

        Empleados empleado = consultasEmpleados.obtenerPorId(entidad.getIdEmpleado());
        if (empleado != null) {
            liquidacionExistente.setEmpleado(empleado);
        }

        Liquidaciones liquidacionActualizada = mantenimiento.actualizar(liquidacionExistente);
        log.info("Se ha actualizado la liquidación con ID: {}", id);
        return deEntidadDtoARespuesta(liquidacionActualizada);
    }

    /**
     * Elimina un registro por su identificador.
     * @param id parametro de entrada de la operacion.
     */
    public void eliminar(Long id) {
        Liquidaciones liquidacion = consulta.obtenerPorId(id);
        if (liquidacion == null) {
            throw new ResourceNotFoundException("Liquidaciones", "id", id);
        }
        mantenimiento.eliminar(id);
        log.info("Se ha eliminado la liquidación con ID: {}", id);
    }

    // ===================== DTO CONVERSIONS =====================

    /**
     * Convierte un DTO de solicitud a entidad.
     * @param solicitud parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public Liquidaciones deSolicitudDtoAEntidad(SolicitudLiquidacionesDTO solicitud) {
        if (solicitud == null) {
            log.warn("El DTO de solicitud es nulo");
            return null;
        }

        Empleados empleado = consultasEmpleados.obtenerPorId(solicitud.getIdEmpleado());
        if (empleado == null) {
            throw new ResourceNotFoundException("Empleados", "id", solicitud.getIdEmpleado());
        }

        MotivoSalida motivoSalida = obtenerMotivoSalida(solicitud.getMotivoSalida());
        if (motivoSalida == null) {
            throw new BadRequestException("Motivo de salida inválido: " + solicitud.getMotivoSalida());
        }

        return Liquidaciones.builder()
                .id(solicitud.getId())
                .fechaSalida(solicitud.getFechaSalida())
                .montoPreaviso(solicitud.getMontoPreaviso())
                .montoCesantia(solicitud.getMontoCesantia())
                .montoVacacionesPendientes(solicitud.getMontoVacacionesPendientes())
                .montoAguinaldoPendiente(solicitud.getMontoAguinaldoPendiente())
                .totalLiquidacion(solicitud.getTotalLiquidacion())
                .motivoSalida(motivoSalida)
                .empleado(empleado)
                .build();
    }

    /**
     * Convierte una entidad a DTO de respuesta.
     * @param entidad parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public RespuestaLiquidacionesDTO deEntidadDtoARespuesta(Liquidaciones entidad) {
        if (entidad == null) {
            log.warn("La entidad Liquidaciones es nula");
            return null;
        }
        var respuesta = new RespuestaLiquidacionesDTO();
        respuesta.id = entidad.getId();
        respuesta.fechaSalida = entidad.getFechaSalida();
        respuesta.montoPreaviso = entidad.getMontoPreaviso();
        respuesta.montoCesantia = entidad.getMontoCesantia();
        respuesta.montoVacacionesPendientes = entidad.getMontoVacacionesPendientes();
        respuesta.montoAguinaldoPendiente = entidad.getMontoAguinaldoPendiente();
        respuesta.montoSalarioProporcional = entidad.getMontoSalarioProporcional();
        respuesta.totalLiquidacion = entidad.getTotalLiquidacion();
        respuesta.salarioPromedioDiario = entidad.getSalarioPromedioDiario();
        respuesta.diasTrabajadosTotal = entidad.getDiasTrabajadosTotal();
        respuesta.preaviso_pagado = entidad.getPreaviso_pagado();
        respuesta.descripcion = entidad.getDescripcion();

        if (entidad.getMotivoSalida() != null) {
            respuesta.motivoSalida = entidad.getMotivoSalida().name();
        }

        if (entidad.getEmpleado() != null) {
            respuesta.idEmpleado = entidad.getEmpleado().getId();
            respuesta.nombreEmpleado = entidad.getEmpleado().getNombre();
            respuesta.primerApellidoEmpleado = entidad.getEmpleado().getPrimerApellido();
            respuesta.segundoApellidoEmpleado = entidad.getEmpleado().getSegundoApellido();
        }

        return respuesta;
    }

    /**
     * Convierte una lista de entidades a DTOs de respuesta.
     * @param entidades parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    public List<RespuestaLiquidacionesDTO> deListaEntidadADto(List<Liquidaciones> entidades) {
        return entidades.stream()
                .map(this::deEntidadDtoARespuesta)
                .toList();
    }

    /**
     * Obtiene informacion necesaria para la operacion.
     * @param motivo parametro de entrada de la operacion.
     * @return resultado de la operacion.
     */
    private MotivoSalida obtenerMotivoSalida(String motivo) {
        if (motivo == null || motivo.isBlank()) {
            return null;
        }
        try {
            return MotivoSalida.valueOf(motivo.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}

