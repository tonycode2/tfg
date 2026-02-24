package com.anthony.tfg.tfg.Modulos.Liquidacion.Servicios;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.Entidades.Empleados;
// import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.Enums.MotivoSalida;
// import com.anthony.tfg.tfg.Entidades.Enums.TipoIncapacidad;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.PlanillaDetalleRepositorio;

import lombok.extern.slf4j.Slf4j;

/**
 * Servicio de cálculo de liquidaciones según la legislación laboral costarricense.
 */
@Service
@Slf4j
public class LiquidacionesCalculoServicio {

    private final PlanillaDetalleRepositorio planillaDetalleRepositorio;
    // private final IncapacidadesRepositorio incapacidadesRepositorio;

    public LiquidacionesCalculoServicio(PlanillaDetalleRepositorio planillaDetalleRepositorio,
                                         IncapacidadesRepositorio incapacidadesRepositorio) {
        this.planillaDetalleRepositorio = planillaDetalleRepositorio;
        // this.incapacidadesRepositorio = incapacidadesRepositorio;
    }

    @Transactional(readOnly = true)
    public double calcularSalarioPromedioPorDia(Long empleadoId, LocalDate fechaHasta, int meses) {
        if (meses <= 0) {
            log.warn("Meses inválidos para cálculo de salario promedio diario: {}", meses);
            return 0.0;
        }
        LocalDate fechaDesde = fechaHasta.minusMonths(meses);

        Double totalDevengado = planillaDetalleRepositorio
                .sumDevengadoByEmpleadoAndFechaPagoBetween(empleadoId, fechaDesde, fechaHasta);

        double total = totalDevengado != null ? totalDevengado : 0.0;

        double salarioPromedioMensual = total / (double) meses;
        double salarioDiario = salarioPromedioMensual / 30.0;

        log.info("Salario promedio diario para empleado {}: ₡{} (total 6 meses: ₡{}, promedio mensual: ₡{})",
                empleadoId,
                String.format("%.2f", salarioDiario),
                String.format("%.2f", total),
                String.format("%.2f", salarioPromedioMensual));
        return salarioDiario;
    }

    // private long calcularDiasIncapacidadNoProtegida(Long empleadoId, LocalDate desde, LocalDate hasta) {
    //     List<Incapacidades> incapacidades = incapacidadesRepositorio
    //             .findByEmpleadoIdOrderByFechaSolicitudDesc(empleadoId);

    //     return incapacidades.stream()
    //             .filter(i -> i.getEstadoSolicitud() != null && "APROBADA".equals(i.getEstadoSolicitud().name()))
    //             .filter(i -> i.getFechaInicio() != null && i.getFechaFin() != null)
    //             .filter(i -> {
    //                 TipoIncapacidad tipo = i.getTipoIncapacidad();
    //                 return tipo != TipoIncapacidad.LICENCIA_DE_MATERNIDAD && tipo != TipoIncapacidad.LICENCIA_DE_PATERNIDAD;
    //             })
    //             .mapToLong(i -> {
    //                 LocalDate inicio = i.getFechaInicio();
    //                 LocalDate fin = i.getFechaFin();
    //                 LocalDate overlapStart = inicio.isBefore(desde) ? desde : inicio;
    //                 LocalDate overlapEnd = fin.isAfter(hasta) ? hasta : fin;
    //                 if (overlapEnd.isBefore(overlapStart)) return 0L;
    //                 return ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    //             })
    //             .sum();
    // }

    public long calcularDiasTotalesAntiguedad(Empleados empleado, LocalDate fechaSalida) {
        LocalDate fechaIngreso = empleado.getFechaIngreso();
        if (fechaIngreso == null) {
            log.warn("El empleado {} no tiene fecha de ingreso registrada", empleado.getId());
            return 0;
        }
        return ChronoUnit.DAYS.between(fechaIngreso, fechaSalida);
    }

    public int calcularDiasPreaviso(long diasTotales) {
        if (diasTotales < 90) {
            return 0;
        } else if (diasTotales < 180) {
            return 7;
        } else if (diasTotales < 365) {
            return 15;
        } else {
            return 30;
        }
    }

    public double calcularMontoPreaviso(double salarioDiario, int diasPreaviso, boolean preaviso_pagado) {
        if (!preaviso_pagado) {
            return 0.0;
        }
        return salarioDiario * diasPreaviso;
    }

    public double calcularCesantia(long diasTotales, double salarioDiario, MotivoSalida motivoSalida) {
        if (motivoSalida != MotivoSalida.DESPIDO_CON_RESPONSABILIDAD) {
            log.info("Cesantía no aplica para motivo de salida: {}", motivoSalida);
            return 0.0;
        }

        if (diasTotales < 90) {
            return 0.0;
        }
        if (diasTotales < 180) {
            return 7.0 * salarioDiario;
        }
        if (diasTotales < 365) {
            return 14.0 * salarioDiario;
        }

        int aniosCompletos = (int) (diasTotales / 365);
        int diasRestantes = (int) (diasTotales % 365);

        int aniosParaCalculo = aniosCompletos;
        if (aniosCompletos >= 1 && diasRestantes > 183) {
            aniosParaCalculo = aniosCompletos + 1;
        }
        aniosParaCalculo = Math.min(aniosParaCalculo, 8);

        double diasPorAnio = obtenerDiasCesantiaPorAnio(aniosParaCalculo);
        double totalDiasCesantia = diasPorAnio * aniosParaCalculo;

        double montoCesantia = totalDiasCesantia * salarioDiario;
        log.info("Cesantía calculada: {} días × ₡{} = ₡{}",
                String.format("%.2f", totalDiasCesantia), String.format("%.2f", salarioDiario), String.format("%.2f", montoCesantia));
        return montoCesantia;
    }

    private double obtenerDiasCesantiaPorAnio(int anio) {
        return switch (anio) {
            case 1 -> 19.5;
            case 2 -> 20.0;
            case 3 -> 20.5;
            case 4 -> 21.0;
            case 5 -> 21.24;
            case 6 -> 21.5;
            case 7, 8, 9 -> 22.0;
            case 10 -> 21.5;
            case 11 -> 21.0;
            case 12 -> 20.5;
            default -> 20.0;
        };
    }

    @Transactional(readOnly = true)
    public double calcularAguinaldoProporcional(Long empleadoId, LocalDate fechaSalida) {
        int anioSalida = fechaSalida.getYear();
        LocalDate inicioAguinaldo = LocalDate.of(anioSalida - 1, 12, 1);

        if (fechaSalida.isBefore(inicioAguinaldo)) {
            inicioAguinaldo = LocalDate.of(anioSalida - 2, 12, 1);
        }

        Double totalDevengado = planillaDetalleRepositorio
                .sumDevengadoByEmpleadoAndFechaPagoBetween(empleadoId, inicioAguinaldo, fechaSalida);

        double total = totalDevengado != null ? totalDevengado : 0.0;
        double aguinaldoProporcional = total / 12.0;

        log.info("Aguinaldo proporcional para empleado {}: ₡{} (devengado: ₡{}, periodo: {} a {})",
                empleadoId, String.format("%.2f", aguinaldoProporcional), String.format("%.2f", total), inicioAguinaldo, fechaSalida);
        return aguinaldoProporcional;
    }

    public double calcularVacacionesPendientes(int saldoVacaciones, double salarioDiario) {
        double monto = saldoVacaciones * salarioDiario;
        log.info("Vacaciones pendientes: {} días × ₡{} = ₡{}",
                saldoVacaciones, String.format("%.2f", salarioDiario), String.format("%.2f", monto));
        return monto;
    }

    public double calcularSalarioProporcional(double salarioDiario, LocalDate fechaSalida) {
        int diaDelMes = fechaSalida.getDayOfMonth();
        double monto = salarioDiario * diaDelMes;
        log.info("Salario proporcional: {} días × ₡{} = ₡{}",
                diaDelMes, String.format("%.2f", salarioDiario), String.format("%.2f", monto));
        return monto;
    }

    @Transactional(readOnly = true)
    public ResultadoCalculo calcularLiquidacionCompleta(Empleados empleado,
                                                         LocalDate fechaSalida,
                                                         MotivoSalida motivoSalida,
                                                         boolean preaviso_pagado) {
        Long empleadoId = empleado.getId();
        log.info("Iniciando cálculo de liquidación para empleado {} ({}), motivo: {}",
                empleadoId, empleado.getNombre() + " " + empleado.getPrimerApellido(), motivoSalida);

        long diasTotales = calcularDiasTotalesAntiguedad(empleado, fechaSalida);

        double salarioDiario = calcularSalarioPromedioPorDia(empleadoId, fechaSalida, 6);

        if (salarioDiario <= 0
                && empleado.getPuesto() != null
                && empleado.getPuesto().getSalarioMinimo() != null
                && empleado.getPuesto().getSalarioMinimo() > 0) {
            salarioDiario = empleado.getPuesto().getSalarioMinimo() / 30.0;
        }

        int diasPreaviso = calcularDiasPreaviso(diasTotales);
        double montoPreaviso = calcularMontoPreaviso(salarioDiario, diasPreaviso, preaviso_pagado);

        double montoCesantia = calcularCesantia(diasTotales, salarioDiario, motivoSalida);

        double montoAguinaldo = calcularAguinaldoProporcional(empleadoId, fechaSalida);

        int saldoVacaciones = empleado.getSaldoVacaciones() != null ? empleado.getSaldoVacaciones() : 0;
        double montoVacaciones = calcularVacacionesPendientes(saldoVacaciones, salarioDiario);

        double montoSalarioProporcional = calcularSalarioProporcional(salarioDiario, fechaSalida);

        double total = montoPreaviso + montoCesantia + montoAguinaldo + montoVacaciones + montoSalarioProporcional;

        log.info("Liquidación calculada para empleado {}: Total = ₡{}", empleadoId, String.format("%.2f", total));

        return new ResultadoCalculo(
                salarioDiario,
                diasTotales,
                diasPreaviso,
                preaviso_pagado,
                montoPreaviso,
                montoCesantia,
                montoAguinaldo,
                montoVacaciones,
                montoSalarioProporcional,
                saldoVacaciones,
                total);
    }

    public record ResultadoCalculo(
            double salarioPromedioDiario,
            long diasTrabajadosTotal,
            int diasPreaviso,
            boolean preaviso_pagado,
            double montoPreaviso,
            double montoCesantia,
            double montoAguinaldoProporcional,
            double montoVacacionesPendientes,
            double montoSalarioProporcional,
            int saldoVacaciones,
            double totalLiquidacion) {
    }
}
