package com.anthony.tfg.tfg.Util;

/**
 * Constantes utilizadas en la generación de reportes de RH.
 */
public final class ReportesConstantes {

    private ReportesConstantes() {
        // Utility class
    }

    // ===== Empresa =====
    public static final String NOMBRE_EMPRESA = "Sastrería Gerson Andre S.A.";
    public static final String LOGO_PATH = "/static/logo.png";

    // ===== Colores Corporativos =====
    public static final String COLOR_AZUL_OSCURO = "#003366";
    public static final String COLOR_ORO = "#DAA520";

    // ===== Márgenes de impresión (mm) =====
    public static final int MARGEN_SUPERIOR = 20;
    public static final int MARGEN_INFERIOR = 20;
    public static final int MARGEN_IZQUIERDO = 15;
    public static final int MARGEN_DERECHO = 15;

    // ===== Formato de moneda =====
    public static final String FORMATO_MONEDA = "###,##0.00";
    public static final String SIMBOLO_MONEDA = "₡";
    public static final int DECIMALES_MONETARIOS = 2;

    // ===== Formato de fechas (español) =====
    public static final String FORMATO_FECHA_REPORTE = "dd 'de' MMMM 'de' yyyy";
    public static final String FORMATO_FECHA_HORA_REPORTE = "dd 'de' MMMM 'de' yyyy, HH:mm";

    // ===== Nombres de Reportes =====
    public static final String TITULO_REPORTE_PLANILLA = "Reporte de Planilla";
    public static final String TITULO_COLILLA_PAGO = "Colilla de Pago";
    public static final String TITULO_REPORTE_VACACIONES = "Reporte de Vacaciones";
    public static final String TITULO_REPORTE_ANTIGUEDAD = "Reporte de Antigüedad";
    public static final String TITULO_REPORTE_DEDUCCIONES = "Reporte de Deducciones Legales";
    public static final String TITULO_REPORTE_LIQUIDACIONES = "Reporte de Liquidación";
    public static final String TITULO_REPORTE_INCAPACIDADES = "Reporte de Incapacidades";
    public static final String TITULO_PROYECCION_CESANTIA = "Proyección de Cesantía";

    /** 
     * @param mesesTrabajados
     * @return int
     */
    // ===== Tabla de Cesantía (Art 29 Código de Trabajo CR) =====
    // Antigüedad en meses → días de cesantía
    public static int diasCesantiaPorAntiguedad(long mesesTrabajados) {
        if (mesesTrabajados < 3) return 0;
        // Simple tiered mapping: 3-11 months => 7 days, 12-59 months => linear pro-rata, cap 240
        if (mesesTrabajados < 12) return 7;
        double anios = mesesTrabajados / 12.0;
        // Use 15 days per year as an approximation, cap at 240 days
        double totalDias = anios * 15.0;
        return (int) Math.round(Math.min(totalDias, 240.0));
    }

    /** 
     * @param mesesTrabajados
     * @return int
     */
    // ===== Preaviso (Art 28 Código de Trabajo CR) =====
    public static int diasPreaviso(long mesesTrabajados) {
        if (mesesTrabajados < 3) return 0;
        if (mesesTrabajados < 6) return 7;    // 7 days
        if (mesesTrabajados < 12) return 14;  // 14 days
        return 30;                               // 1 month
    }
}
