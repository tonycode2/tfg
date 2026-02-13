package com.anthony.tfg.tfg.Config;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.ConfiguracionRenta;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.DiasFeriados;
import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.anthony.tfg.tfg.Entidades.Enums.TipoQuincena;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.DTOs.Solicitud.SolicitudGenerarPlanillaDTO;
import com.anthony.tfg.tfg.Exceptions.ConflictException;
import com.anthony.tfg.tfg.Modulos.Planilla.Servicio.ServicioPlanilla;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;
import com.anthony.tfg.tfg.Modulos.JornadaDiaria.Servicio.ServicioJornadaDiaria;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.ConfiguracionRentaRepositorio;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.DiasFeriadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.PuestosRepositorio;

import lombok.RequiredArgsConstructor;

/**
 * DataSeeder - Seeds the database with focused test data for Sastrería department.
 * 
 * Creates:
 * - 1 ADMIN user
 * - 1 RH user  
 * - 1 JEFE for Sastrería department
 * - 3 EMPLEADO users for Sastrería
 * - Attendance records from Dec 1, 2024 to Mar 31, 2026 (punctual)
 * - March 2026: approved vacations, incapacities, permit without pay, and approved overtime
 * - JornadaDiaria generated after all records are saved
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "TestPass123!";
    private static final LocalDate ATTENDANCE_START = LocalDate.of(2024, 12, 1);
    // Default end date for non-employee users (ADMIN, RH, JEFE, etc.)
    private static final LocalDate ATTENDANCE_END_DEFAULT = LocalDate.of(2026, 4, 15);
    // End date for seeded EMPLEADO users (three empleados) — last day of March
    private static final LocalDate ATTENDANCE_END_EMPLEADO = LocalDate.of(2026, 3, 31);

    private final DepartamentoRepositorio departamentoRepositorio;
    private final PuestosRepositorio puestosRepositorio;
    private final DireccionRepositorio direccionRepositorio;
    private final UserRepository userRepository;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final ConfiguracionRentaRepositorio configuracionRentaRepositorio;
    private final DiasFeriadosRepositorio diasFeriadosRepositorio;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepositorio;
    private final AsistenciaRepositorio asistenciaRepositorio;
    private final PasswordEncoder passwordEncoder;
    private final ServicioJornadaDiaria servicioJornadaDiaria;
    private final ServicioPlanilla servicioPlanilla;

    @Override
    @Transactional
    public void run(String... args) {
        int tramosRenta = seedRentaConfig();
        if (tramosRenta > 0) {
            logger.info("Seeded {} renta tax brackets", tramosRenta);
        }

        int holidays2025 = seedHolidays2025();
        int holidays2026 = seedHolidays2026();
        int totalHolidays = holidays2025 + holidays2026;
        if (totalHolidays > 0) {
            logger.info("Seeded {} holidays (2025: {}, 2026: {})", totalHolidays, holidays2025, holidays2026);
        }

        if (empleadosRepositorio.count() > 0) {
            logger.info("Database already seeded (employees exist), skipping...");
            return;
        }

        logger.info("=== Starting database seeding for Sastrería department ===");

        try {
            // Step 1: Get or create Admin department
            Departamento admin = departamentoRepositorio.findById(5L)
                .orElseGet(() -> {
                    logger.info("Admin department not found, creating it...");
                    Departamento newDept = new Departamento();
                    newDept.setNombre("Admin");
                    return departamentoRepositorio.save(newDept);
                });
            logger.info("Using department: {}", admin.getNombre());

            // Step 1b: Get or create Sastrería department
            Departamento sastreria = departamentoRepositorio.findById(4L)
                .orElseGet(() -> {
                    logger.info("Sastrería department not found, creating it...");
                    Departamento newDept = new Departamento();
                    newDept.setNombre("Sastrería");
                    return departamentoRepositorio.save(newDept);
                });
            logger.info("Using department: {}", sastreria.getNombre());

            // Step 2: Create positions for Admin and Sastrería
            List<Puestos> puestos = createPositions(admin, sastreria);
            logger.info("Created {} positions for Admin and Sastrería", puestos.size());

            // Step 3: Create addresses for 6 employees
            List<Direccion> direcciones = createAddresses();
            logger.info("Created {} addresses", direcciones.size());

            // Step 4: Create users (1 ADMIN, 1 RH, 1 JEFE, 3 EMPLEADO)
            List<User> users = createUsers();
            logger.info("Created {} users", users.size());

            // Step 5: Create 6 employees
            List<Empleados> empleados = createEmployees(puestos, direcciones, users);
            logger.info("Created {} employees", empleados.size());

            // Step 6: Assign department head
            assignDepartmentHead(empleados.get(2), sastreria); // El jefe es el 3er empleado
            logger.info("Assigned department head");

            // Step 7: Generate attendance records (Dec 1, 2024 - Mar 31, 2026)
            int attendanceCount = generateAttendanceRecords(empleados);
            logger.info("Generated {} attendance records", attendanceCount);

            // Step 9: Generate jornada diaria for all employees after data is saved
            int jornadasGeneradas = generateJornadasFromAttendance(empleados);
            logger.info("Generated {} jornada diaria records", jornadasGeneradas);

                // No special monthly events: keep months uniform (no leave/jornada injections)

            generarPlanillasHistoricas();

            logger.info("=== Database seeding completed successfully ===");
            logger.info("Summary: {} employees, {} addresses, {} users",
                    empleados.size(), direcciones.size(), users.size());

        } catch (Exception e) {
            logger.error("Error during database seeding: {}", e.getMessage(), e);
            throw new RuntimeException("Database seeding failed", e);
        }
    }

    /**
     * Seeds Costa Rica national holidays for 2026.
     */
    private int seedHolidays2025() {
        List<DiasFeriados> holidays = List.of(
            DiasFeriados.builder()
                .nombre("Año Nuevo")
                .fecha(LocalDate.of(2025, 1, 1))
                .descripcion("Celebración de Año Nuevo")
                .build(),
            DiasFeriados.builder()
                .nombre("Jueves Santo")
                .fecha(LocalDate.of(2025, 4, 17))
                .descripcion("Semana Santa - Jueves Santo")
                .build(),
            DiasFeriados.builder()
                .nombre("Viernes Santo")
                .fecha(LocalDate.of(2025, 4, 18))
                .descripcion("Semana Santa - Viernes Santo")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Juan Santamaría")
                .fecha(LocalDate.of(2025, 4, 11))
                .descripcion("Conmemoración de la Batalla de Rivas")
                .build(),
            DiasFeriados.builder()
                .nombre("Día Internacional del Trabajo")
                .fecha(LocalDate.of(2025, 5, 1))
                .descripcion("Celebración del Día del Trabajo")
                .build(),
            DiasFeriados.builder()
                .nombre("Anexión del Partido de Nicoya a Costa Rica")
                .fecha(LocalDate.of(2025, 7, 25))
                .descripcion("Anexión del Partido de Nicoya")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Nuestra Señora de los Ángeles")
                .fecha(LocalDate.of(2025, 8, 2))
                .descripcion("Festividad de Nuestra Señora de los Ángeles")
                .build(),
            DiasFeriados.builder()
                .nombre("Independencia de Costa Rica")
                .fecha(LocalDate.of(2025, 9, 15))
                .descripcion("Conmemoración de la Independencia")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Abolición del Ejército")
                .fecha(LocalDate.of(2025, 12, 1))
                .descripcion("Abolición del Ejército en Costa Rica")
                .build(),
            DiasFeriados.builder()
                .nombre("Navidad")
                .fecha(LocalDate.of(2025, 12, 25))
                .descripcion("Celebración de Navidad")
                .build()
        );

        List<DiasFeriados> toCreate = holidays.stream()
            .filter(holiday -> !diasFeriadosRepositorio.existsByFecha(holiday.getFecha()))
            .toList();

        if (toCreate.isEmpty()) {
            return 0;
        }

        diasFeriadosRepositorio.saveAll(toCreate);
        return toCreate.size();
    }

    private int seedHolidays2026() {
        List<DiasFeriados> holidays = List.of(
            DiasFeriados.builder()
                .nombre("Año Nuevo")
                .fecha(LocalDate.of(2026, 1, 1))
                .descripcion("Celebración de Año Nuevo")
                .build(),
            DiasFeriados.builder()
                .nombre("Jueves Santo")
                .fecha(LocalDate.of(2026, 4, 2))
                .descripcion("Semana Santa - Jueves Santo")
                .build(),
            DiasFeriados.builder()
                .nombre("Viernes Santo")
                .fecha(LocalDate.of(2026, 4, 3))
                .descripcion("Semana Santa - Viernes Santo")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Juan Santamaría")
                .fecha(LocalDate.of(2026, 4, 11))
                .descripcion("Conmemoración de la Batalla de Rivas")
                .build(),
            DiasFeriados.builder()
                .nombre("Día Internacional del Trabajo")
                .fecha(LocalDate.of(2026, 5, 1))
                .descripcion("Celebración del Día del Trabajo")
                .build(),
            DiasFeriados.builder()
                .nombre("Anexión del Partido de Nicoya a Costa Rica")
                .fecha(LocalDate.of(2026, 7, 25))
                .descripcion("Anexión del Partido de Nicoya")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Nuestra Señora de los Ángeles")
                .fecha(LocalDate.of(2026, 8, 2))
                .descripcion("Festividad de Nuestra Señora de los Ángeles")
                .build(),
            DiasFeriados.builder()
                .nombre("Independencia de Costa Rica")
                .fecha(LocalDate.of(2026, 9, 15))
                .descripcion("Conmemoración de la Independencia")
                .build(),
            DiasFeriados.builder()
                .nombre("Día de Abolición del Ejército")
                .fecha(LocalDate.of(2026, 12, 1))
                .descripcion("Abolición del Ejército en Costa Rica")
                .build(),
            DiasFeriados.builder()
                .nombre("Navidad")
                .fecha(LocalDate.of(2026, 12, 25))
                .descripcion("Celebración de Navidad")
                .build()
        );

        List<DiasFeriados> toCreate = holidays.stream()
            .filter(holiday -> !diasFeriadosRepositorio.existsByFecha(holiday.getFecha()))
            .toList();

        if (toCreate.isEmpty()) {
            return 0;
        }

        diasFeriadosRepositorio.saveAll(toCreate);
        return toCreate.size();
    }

    /**
     * Seeds Costa Rica income tax brackets (monthly gross salary).
     */
    private int seedRentaConfig() {
        if (configuracionRentaRepositorio.count() > 0) {
            return 0;
        }

        List<ConfiguracionRenta> tramos = List.of(
            ConfiguracionRenta.builder()
                .montoMinimo(0.0)
                .montoMaximo(922000.0)
                .porcentaje(0.0)
                .build(),
            ConfiguracionRenta.builder()
                .montoMinimo(922000.0)
                .montoMaximo(1352000.0)
                .porcentaje(10.0)
                .build(),
            ConfiguracionRenta.builder()
                .montoMinimo(1352000.0)
                .montoMaximo(2373000.0)
                .porcentaje(15.0)
                .build(),
            ConfiguracionRenta.builder()
                .montoMinimo(2373000.0)
                .montoMaximo(4745000.0)
                .porcentaje(20.0)
                .build(),
            ConfiguracionRenta.builder()
                .montoMinimo(4745000.0)
                .montoMaximo(999_999_999.0)
                .porcentaje(25.0)
                .build()
        );

        configuracionRentaRepositorio.saveAll(tramos);
        return tramos.size();
    }

    /**
     * Creates positions for Admin and Sastrería departments.
     */
    private List<Puestos> createPositions(Departamento admin, Departamento sastreria) {
        List<Puestos> positions = new ArrayList<>();
        
        Time horaEntrada = Time.valueOf("08:00:00");
        Time horaSalida = Time.valueOf("17:00:00");

        // ===== ADMIN DEPARTMENT POSITIONS =====
        // Administrador
        Puestos administrador = Puestos.builder()
            .nombre("Administrador")
            .salarioMinimo(800000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(admin)
            .build();
        positions.add(puestosRepositorio.save(administrador));

        // Gerente de RH
        Puestos gerenteRH = Puestos.builder()
            .nombre("Gerente de RH")
            .salarioMinimo(1100000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(admin)
            .build();
        positions.add(puestosRepositorio.save(gerenteRH));

        // ===== SASTRERÍA DEPARTMENT POSITIONS =====
        // Sastre (for jefe)
        Puestos sastre = Puestos.builder()
            .nombre("Sastre")
            .salarioMinimo(500000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(sastreria)
            .build();
        positions.add(puestosRepositorio.save(sastre));

        // Sastre Junior (for empleados)
        Puestos sastreJunior = Puestos.builder()
            .nombre("Sastre Junior")
            .salarioMinimo(350000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(sastreria)
            .build();
        positions.add(puestosRepositorio.save(sastreJunior));

        // Cortador de Tela
        Puestos cortador = Puestos.builder()
            .nombre("Cortador de Tela")
            .salarioMinimo(400000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(sastreria)
            .build();
        positions.add(puestosRepositorio.save(cortador));

        return positions;
    }

    /**
     * Creates 6 realistic Costa Rican addresses.
     */
    private List<Direccion> createAddresses() {
        List<Direccion> addresses = new ArrayList<>();
        
        String[][] locationData = {
            {"San José", "San José", "Carmen", "Del Teatro Nacional 100m norte, edificio azul"},
            {"San José", "Escazú", "San Rafael", "Centro comercial Multiplaza, 200m oeste"},
            {"San José", "Desamparados", "Desamparados", "Frente al parque central, casa esquinera"},
            {"San José", "Curridabat", "Curridabat", "Pinares, de la iglesia 150m sur"},
            {"San José", "Santa Ana", "Santa Ana", "Forum, edificio torre 2, piso 3"},
            {"Alajuela", "Alajuela", "Alajuela", "Del mercado central 300m norte"}
        };

        for (String[] loc : locationData) {
            Direccion direccion = Direccion.builder()
                .provincia(loc[0])
                .canton(loc[1])
                .distrito(loc[2])
                .indicaciones(loc[3])
                .build();
            addresses.add(direccionRepositorio.save(direccion));
        }

        return addresses;
    }

    /**
     * Creates 6 users: 1 ADMIN, 1 RH, 1 JEFE, 3 EMPLEADO.
     */
    private List<User> createUsers() {
        List<User> users = new ArrayList<>();
        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        // {username, role}
        Object[][] userData = {
            {"admin_sasteria", Role.ADMIN},
            {"rh_sasteria", Role.HR},
            {"jefe_sasteria", Role.JEFE},
            {"empleado1_sasteria", Role.EMPLEADO},
            {"empleado2_sasteria", Role.EMPLEADO},
            {"empleado3_sasteria", Role.EMPLEADO}
        };

        for (Object[] data : userData) {
            String username = (String) data[0];
            Role role = (Role) data[1];
            
            User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .role(role)
                .passwordChangeRequired(false)
                .build();
            users.add(userRepository.save(user));
        }

        return users;
    }

    /**
     * Creates 6 employees: 1 ADMIN, 1 RH, 1 JEFE, 3 EMPLEADO.
     * Admin and RH are in the Admin department, JEFE and EMPLEADO are in Sastrería.
     */
    private List<Empleados> createEmployees(List<Puestos> puestos, List<Direccion> direcciones, List<User> users) {
        List<Empleados> empleados = new ArrayList<>();

        // {nombre, primerApellido, segundoApellido, birthDate, startDate, positionIndex, userIndex, cedula}
        // positionIndex: 0=Administrador(Admin), 1=GerenteRH(Admin), 2=Sastre(Sastrería), 3=SastreJr, 4=SastreJr, 5=Cortador
        Object[][] employeeData = {
            {"Carlos", "Administrador", "Vargas", "1985-03-15", "2020-01-10", 0, 0, "101230456"},   // ADMIN (Administrador/Admin dept)
            {"María", "González", "Solano", "1988-07-22", "2021-06-15", 1, 1, "102340567"},        // RH (Gerente RH/Admin dept)
            {"José", "Fernández", "Castro", "1980-11-08", "2019-03-20", 2, 2, "103450678"},         // JEFE (Sastre/Sastrería)
            {"Ana", "López", "Mora", "1992-05-12", "2023-02-01", 3, 3, "104560789"},                // EMPLEADO (Sastre Jr/Sastrería)
            {"Pedro", "Álvarez", "Jiménez", "1995-09-30", "2024-01-15", 3, 4, "105670890"},         // EMPLEADO (Sastre Jr/Sastrería)
            {"Laura", "Rodríguez", "Pérez", "1993-04-18", "2023-08-10", 4, 5, "106780901"}          // EMPLEADO (Cortador/Sastrería)
        };

        for (int i = 0; i < employeeData.length; i++) {
            Object[] data = employeeData[i];
            
            String nombre = (String) data[0];
            String primerApellido = (String) data[1];
            String segundoApellido = (String) data[2];
            LocalDate fechaNacimiento = LocalDate.parse((String) data[3]);
            LocalDate fechaIngreso = LocalDate.parse((String) data[4]);
            int puestoIndex = (Integer) data[5];
            int userIndex = (Integer) data[6];
            String cedula = (String) data[7];
            
            // Calculate vacation balance based on years worked (14 days per year, max 30)
            long yearsWorked = ChronoUnit.YEARS.between(fechaIngreso, LocalDate.now());
            int vacationDays = Math.min((int) (yearsWorked * 14), 30);
            
            Empleados empleado = Empleados.builder()
                .nombre(nombre)
                .primerApellido(primerApellido)
                .segundoApellido(segundoApellido)
                .cedula(cedula)
                .fechaNacimiento(fechaNacimiento)
                .fechaIngreso(fechaIngreso)
                .puesto(puestos.get(puestoIndex))
                .usuario(users.get(userIndex))
                .direccion(direcciones.get(i))
                .saldoVacaciones(vacationDays)
                .estaActivo(true)
                .build();
            
            empleados.add(empleadosRepositorio.save(empleado));
        }

        return empleados;
    }

    /**
     * Assigns a department head.
     */
    private void assignDepartmentHead(Empleados jefe, Departamento departamento) {
        JefesDepartamento jefeDept = JefesDepartamento.builder()
            .departamento(departamento)
            .empleado(jefe)
            .fechaInicio(jefe.getFechaIngreso())
            .fechaFin(null)
            .estaActivo(true)
            .build();
        
        jefesDepartamentoRepositorio.save(jefeDept);
    }

    /**
    * Generates attendance records per-employee:
    * - Seeded `EMPLEADO` users: from Dec 1, 2024 to 2026-03-31 (inclusive)
    * - Other users (ADMIN, RH, JEFE): from Dec 1, 2024 to 2026-04-15 (inclusive)
    * - Weekdays only (Monday-Friday)
    * - Punctual ENTRADA and SALIDA (no overtime)
     */
    private int generateAttendanceRecords(List<Empleados> empleados) {
        List<Asistencia> attendanceRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            if (!empleado.getEstaActivo()) continue;

            Time horaEntrada = empleado.getPuesto().getHoraEntrada();
            Time horaSalida = empleado.getPuesto().getHoraSalida();
            
            LocalTime baseEntrada = horaEntrada.toLocalTime();
            LocalTime baseSalida = horaSalida.toLocalTime();

            // Determine end date depending on role: seeded EMPLEADO users stop on ATTENDANCE_END_EMPLEADO
            LocalDate endDate = ATTENDANCE_END_DEFAULT;
            if (empleado.getUsuario() != null && empleado.getUsuario().getRole() == Role.EMPLEADO) {
                endDate = ATTENDANCE_END_EMPLEADO;
            }

            LocalDate currentDate = ATTENDANCE_START;
            while (!currentDate.isAfter(endDate)) {
                if (isLaborable(currentDate)) {
                    LocalDateTime entradaDateTime = LocalDateTime.of(currentDate, baseEntrada);
                    LocalDateTime salidaDateTime = LocalDateTime.of(currentDate, baseSalida);

                    Asistencia entrada = Asistencia.builder()
                        .tipoEvento(TipoEvento.ENTRADA)
                        .fechaHora(entradaDateTime)
                        .observaciones(null)
                        .empleado(empleado)
                        .build();
                    attendanceRecords.add(entrada);

                    Asistencia salida = Asistencia.builder()
                        .tipoEvento(TipoEvento.SALIDA)
                        .fechaHora(salidaDateTime)
                        .observaciones(null)
                        .empleado(empleado)
                        .build();
                    attendanceRecords.add(salida);
                }
                
                currentDate = currentDate.plusDays(1);
            }
        }

        asistenciaRepositorio.saveAll(attendanceRecords);
        return attendanceRecords.size();
    }

    // No month-specific events: keep all months uniform for seeding.

    private int generateJornadasFromAttendance(List<Empleados> empleados) {
        int jornadasCreadas = 0;

        for (Empleados empleado : empleados) {
            if (!empleado.getEstaActivo()) continue;

            Time horaSalida = empleado.getPuesto().getHoraSalida();
            LocalTime baseSalida = horaSalida.toLocalTime();

            // Use same per-employee end date logic as attendance generation
            LocalDate endDate = ATTENDANCE_END_DEFAULT;
            if (empleado.getUsuario() != null && empleado.getUsuario().getRole() == Role.EMPLEADO) {
                endDate = ATTENDANCE_END_EMPLEADO;
            }

            LocalDate currentDate = ATTENDANCE_START;
            while (!currentDate.isAfter(endDate)) {
                if (isLaborable(currentDate)) {
                    LocalDateTime salidaDateTime = LocalDateTime.of(currentDate, baseSalida);
                    if (servicioJornadaDiaria.registrarJornadaPorClockOut(empleado.getId(), salidaDateTime) != null) {
                        jornadasCreadas++;
                    }
                }
                currentDate = currentDate.plusDays(1);
            }
        }

        return jornadasCreadas;
    }

    // applyLeaveJornadas removed: no special leave events will be injected into jornadas

    private void generarPlanillasHistoricas() {
        YearMonth current = YearMonth.of(2024, 12);
        YearMonth end = YearMonth.of(2026, 1);

        while (!current.isAfter(end)) {
            generarPlanillaParaPeriodo(current, TipoQuincena.PRIMERA);
            generarPlanillaParaPeriodo(current, TipoQuincena.SEGUNDA);
            current = current.plusMonths(1);
        }
    }

    private void generarPlanillaParaPeriodo(YearMonth periodo, TipoQuincena tipoQuincena) {
        try {
            SolicitudGenerarPlanillaDTO solicitud = new SolicitudGenerarPlanillaDTO(
                periodo.getMonthValue(),
                periodo.getYear(),
                tipoQuincena);
            servicioPlanilla.generarPlanilla(solicitud);
            logger.info("Planilla generada: {} {}", tipoQuincena, periodo);
        } catch (ConflictException ex) {
            logger.info("Planilla ya existe: {} {}", tipoQuincena, periodo);
        }
    }

    private boolean isLaborable(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        if (dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY) {
            return false;
        }
        return true;
    }

    // MarchEvents record removed - no month-specific events
}