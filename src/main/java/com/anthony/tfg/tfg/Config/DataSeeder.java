package com.anthony.tfg.tfg.Config;

import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.anthony.tfg.tfg.Entidades.Asistencia;
import com.anthony.tfg.tfg.Entidades.Departamento;
import com.anthony.tfg.tfg.Entidades.Direccion;
import com.anthony.tfg.tfg.Entidades.Empleados;
import com.anthony.tfg.tfg.Entidades.HorasExtra;
import com.anthony.tfg.tfg.Entidades.Incapacidades;
import com.anthony.tfg.tfg.Entidades.JornadaDiaria;
import com.anthony.tfg.tfg.Entidades.Permisos;
import com.anthony.tfg.tfg.Entidades.Enums.EstadoSolicitud;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEntidadEmisora;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.anthony.tfg.tfg.Entidades.Enums.TipoIncapacidad;
import com.anthony.tfg.tfg.Entidades.Enums.TipoPermiso;
import com.anthony.tfg.tfg.Entidades.Enums.TipoTarifa;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.HorasExtraRepositorio;
import com.anthony.tfg.tfg.Repositorios.IncapacidadesRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.JornadaDiariaRepositorio;
import com.anthony.tfg.tfg.Repositorios.PermisosRepositorio;
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
 * - Attendance records from Jan 1, 2026 to Mar 16, 2026
 * - Random overtime, vacations, incapacities, and occasional permits
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "TestPass123!";
    private static final LocalDate ATTENDANCE_START = LocalDate.of(2026, 1, 1);
    private static final LocalDate ATTENDANCE_END = LocalDate.of(2026, 4, 16);

    private final DepartamentoRepositorio departamentoRepositorio;
    private final PuestosRepositorio puestosRepositorio;
    private final DireccionRepositorio direccionRepositorio;
    private final UserRepository userRepository;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepositorio;
    private final AsistenciaRepositorio asistenciaRepositorio;
    private final JornadaDiariaRepositorio jornadaDiariaRepositorio;
    private final HorasExtraRepositorio horasExtraRepositorio;
    private final PermisosRepositorio permisosRepositorio;
    private final IncapacidadesRepositorio incapacidadesRepositorio;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // Fixed seed for reproducibility

    @Override
    @Transactional
    public void run(String... args) {
        if (empleadosRepositorio.count() > 0) {
            logger.info("Database already seeded (employees exist), skipping...");
            return;
        }

        logger.info("=== Starting database seeding for Sastrería department ===");

        try {
            // Step 1: Get or create Sastrería department
            Departamento sastreria = departamentoRepositorio.findById(4L)
                .orElseGet(() -> {
                    logger.info("Sastrería department not found, creating it...");
                    Departamento newDept = new Departamento();
                    newDept.setNombre("Sastrería");
                    return departamentoRepositorio.save(newDept);
                });
            logger.info("Using department: {}", sastreria.getNombre());

            // Step 2: Create positions for Sastrería
            List<Puestos> puestos = createSastreriaPositions(sastreria);
            logger.info("Created {} positions for Sastrería", puestos.size());

            // Step 3: Create addresses for 5 employees
            List<Direccion> direcciones = createAddresses();
            logger.info("Created {} addresses", direcciones.size());

            // Step 4: Create users (1 ADMIN, 1 RH, 1 JEFE, 3 EMPLEADO)
            List<User> users = createUsers();
            logger.info("Created {} users", users.size());

            // Step 5: Create 5 employees
            List<Empleados> empleados = createEmployees(puestos, direcciones, users);
            logger.info("Created {} employees", empleados.size());

            // Step 6: Assign department head
            assignDepartmentHead(empleados.get(2), sastreria); // El jefe es el 3er empleado
            logger.info("Assigned department head");

            // Step 7: Generate attendance records (Jan 1 - Mar 16, 2026)
            int attendanceCount = generateAttendanceRecords(empleados);
            logger.info("Generated {} attendance records", attendanceCount);

            // Step 8: Generate random overtime hours
            int overtimeCount = generateOvertimeRecords(empleados);
            logger.info("Generated {} overtime records", overtimeCount);

            // Step 9: Generate random vacations
            int vacationCount = generateVacationRecords(empleados);
            logger.info("Generated {} vacation records", vacationCount);

            // Step 10: Generate random incapacities
            int incapacityCount = generateIncapacityRecords(empleados);
            logger.info("Generated {} incapacity records", incapacityCount);

            // Step 11: Generate occasional permits
            int permitCount = generatePermitRecords(empleados);
            logger.info("Generated {} permit records", permitCount);

            logger.info("=== Database seeding completed successfully ===");
            logger.info("Summary: {} employees, {} addresses, {} users",
                    empleados.size(), direcciones.size(), users.size());

        } catch (Exception e) {
            logger.error("Error during database seeding: {}", e.getMessage(), e);
            throw new RuntimeException("Database seeding failed", e);
        }
    }

    /**
     * Creates positions for Sastrería department.
     */
    private List<Puestos> createSastreriaPositions(Departamento sastreria) {
        List<Puestos> positions = new ArrayList<>();
        
        Time horaEntrada = Time.valueOf("08:00:00");
        Time horaSalida = Time.valueOf("17:00:00");

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

        // Gerente de RH (different department, but needed for RH user)
        Departamento rhDept = departamentoRepositorio.findById(1L)
            .orElseThrow(() -> new IllegalStateException("RH department not found"));
        Puestos gerenteRH = Puestos.builder()
            .nombre("Gerente de RH")
            .salarioMinimo(1100000.0)
            .horaEntrada(horaEntrada)
            .horaSalida(horaSalida)
            .departamento(rhDept)
            .build();
        positions.add(puestosRepositorio.save(gerenteRH));

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
     */
    private List<Empleados> createEmployees(List<Puestos> puestos, List<Direccion> direcciones, List<User> users) {
        List<Empleados> empleados = new ArrayList<>();

        // {nombre, primerApellido, segundoApellido, birthDate, startDate, positionIndex, userIndex, salary, cedula}
        Object[][] employeeData = {
            {"Carlos", "Administrador", "Vargas", "1985-03-15", "2020-01-10", 0, 0, 800000.0, "101230456"},   // ADMIN
            {"María", "González", "Solano", "1988-07-22", "2021-06-15", 3, 1, 1100000.0, "102340567"},        // RH (Gerente RH)
            {"José", "Fernández", "Castro", "1980-11-08", "2019-03-20", 0, 2, 600000.0, "103450678"},         // JEFE (Sastre)
            {"Ana", "López", "Mora", "1992-05-12", "2023-02-01", 1, 3, 380000.0, "104560789"},                // EMPLEADO (Sastre Jr)
            {"Pedro", "Álvarez", "Jiménez", "1995-09-30", "2024-01-15", 1, 4, 370000.0, "105670890"},         // EMPLEADO (Sastre Jr)
            {"Laura", "Rodríguez", "Pérez", "1993-04-18", "2023-08-10", 2, 5, 420000.0, "106780901"}          // EMPLEADO (Cortador)
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
            Double salario = (Double) data[7];
            String cedula = (String) data[8];
            
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
                .salarioBase(salario)
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
     * Generates attendance records from Jan 1, 2026 to Mar 16, 2026.
     * - Weekdays only (Monday-Friday)
     * - ENTRADA around 08:00 with slight variance
     * - SALIDA around 17:00 with variance for some overtime
     * - Creates JornadaDiaria records for payroll calculations
     */
    private int generateAttendanceRecords(List<Empleados> empleados) {
        List<Asistencia> attendanceRecords = new ArrayList<>();
        List<JornadaDiaria> jornadaRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            if (!empleado.getEstaActivo()) continue;

            Time horaEntrada = empleado.getPuesto().getHoraEntrada();
            Time horaSalida = empleado.getPuesto().getHoraSalida();
            
            LocalTime baseEntrada = horaEntrada.toLocalTime();
            LocalTime baseSalida = horaSalida.toLocalTime();

            LocalDate currentDate = ATTENDANCE_START;
            while (!currentDate.isAfter(ATTENDANCE_END)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                
                // Skip weekends
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    // Randomly skip some days (vacations/sick days will be handled separately)
                    if (random.nextInt(100) < 95) { // 95% attendance rate
                        // ENTRADA with small variance (-5 to +10 minutes)
                        int entradaVariance = random.nextInt(16) - 5;
                        LocalTime entradaTime = baseEntrada.plusMinutes(entradaVariance);
                        LocalDateTime entradaDateTime = LocalDateTime.of(currentDate, entradaTime);
                        
                        String observacionesEntrada = null;
                        if (entradaVariance > 5) {
                            observacionesEntrada = "Llegada tardía: " + entradaVariance + " minutos";
                        }
                        
                        Asistencia entrada = Asistencia.builder()
                            .tipoEvento(TipoEvento.ENTRADA)
                            .fechaHora(entradaDateTime)
                            .observaciones(observacionesEntrada)
                            .empleado(empleado)
                            .build();
                        attendanceRecords.add(entrada);

                        // SALIDA with variance for possible overtime
                        int salidaVariance = random.nextInt(100) < 30 ? random.nextInt(61) : random.nextInt(26) - 10;
                        LocalTime salidaTime = baseSalida.plusMinutes(salidaVariance);
                        LocalDateTime salidaDateTime = LocalDateTime.of(currentDate, salidaTime);
                        
                        String observacionesSalida = null;
                        if (salidaVariance > 30) {
                            observacionesSalida = "Tiempo extra trabajado";
                        }
                        
                        Asistencia salida = Asistencia.builder()
                            .tipoEvento(TipoEvento.SALIDA)
                            .fechaHora(salidaDateTime)
                            .observaciones(observacionesSalida)
                            .empleado(empleado)
                            .build();
                        attendanceRecords.add(salida);

                        // Calculate hours worked
                        double hoursWorked = ChronoUnit.MINUTES.between(entradaTime, salidaTime) / 60.0;
                        double regularHours = Math.min(hoursWorked, 8.0);
                        double extraHours = Math.max(0, hoursWorked - 8.0);

                        // Create JornadaDiaria record
                        JornadaDiaria jornada = JornadaDiaria.builder()
                            .fecha(currentDate)
                            .horaEntrada(entradaTime)
                            .horaSalida(salidaTime)
                            .horasRegulares(regularHours)
                            .horasExtra(extraHours)
                            .observaciones(extraHours > 0 ? "Horas extra trabajadas" : null)
                            .empleado(empleado)
                            .build();
                        jornadaRecords.add(jornada);
                    }
                }
                
                currentDate = currentDate.plusDays(1);
            }
        }

        asistenciaRepositorio.saveAll(attendanceRecords);
        jornadaDiariaRepositorio.saveAll(jornadaRecords);
        return attendanceRecords.size();
    }

    /**
     * Generates random overtime requests for employees.
     * Each employee has 20% chance of 1-2 overtime requests.
     */
    private int generateOvertimeRecords(List<Empleados> empleados) {
        List<HorasExtra> overtimeRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            // Skip ADMIN and RH users from overtime
            if (empleado.getUsuario().getRole() == Role.ADMIN || 
                empleado.getUsuario().getRole() == Role.HR) {
                continue;
            }

            // 20% chance of having overtime requests
            if (random.nextInt(100) < 20) {
                int numRequests = random.nextInt(2) + 1; // 1-2 requests
                
                for (int i = 0; i < numRequests; i++) {
                    // Random date between Jan 1 and Apr 16, 2026
                    LocalDate requestDate = ATTENDANCE_START.plusDays(random.nextInt(107));
                    
                    // Random hours: 2-5 hours
                    int hours = random.nextInt(4) + 2;
                    
                    TipoTarifa tarifa = random.nextBoolean() ? TipoTarifa.SIMPLE : TipoTarifa.DOBLE;
                    
                    HorasExtra horasExtra = HorasExtra.builder()
                        .fechaSolicitud(requestDate)
                        .cantidadDeHoras(hours)
                        .motivo("Trabajo adicional en proyecto urgente")
                        .tipoTarifa(tarifa)
                        .estadoSolicitud(EstadoSolicitud.APROBADA)
                        .aprobado(true)
                        .procesado(false)
                        .empleado(empleado)
                        .build();
                    
                    overtimeRecords.add(horasExtra);
                }
            }
        }

        horasExtraRepositorio.saveAll(overtimeRecords);
        return overtimeRecords.size();
    }

    /**
     * Generates random vacation requests (using Permisos with TipoPermiso.VACACIONES).
     * Each employee has 30% chance of 1 vacation request.
     */
    private int generateVacationRecords(List<Empleados> empleados) {
        List<Permisos> vacationRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            // 30% chance of vacation
            if (random.nextInt(100) < 30) {
                // Random start date between Jan 15 and Apr 10, 2026
                LocalDate startDate = ATTENDANCE_START.plusDays(random.nextInt(87) + 15);
                
                // Random duration: 3-7 days
                int days = random.nextInt(5) + 3;
                LocalDate endDate = startDate.plusDays(days - 1);
                
                Permisos vacation = Permisos.builder()
                    .fechaInicio(startDate)
                    .fechaFin(endDate)
                    .diasTotales(days)
                    .tipoPermiso(TipoPermiso.VACACIONES)
                    .motivo("Vacaciones planificadas")
                    .estadoSolicitud(EstadoSolicitud.APROBADA)
                    .fechaSolicitud(startDate.minusDays(10))
                    .fechaAprobacionJefe(startDate.minusDays(7))
                    .fechaAprobacionRH(startDate.minusDays(5))
                    .empleado(empleado)
                    .build();
                
                vacationRecords.add(vacation);
            }
        }

        permisosRepositorio.saveAll(vacationRecords);
        return vacationRecords.size();
    }

    /**
     * Generates random incapacity records.
     * Each employee has 15% chance of 1 incapacity.
     */
    private int generateIncapacityRecords(List<Empleados> empleados) {
        List<Incapacidades> incapacityRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            // 15% chance of incapacity
            if (random.nextInt(100) < 15) {
                // Random start date between Jan 1 and Apr 10, 2026
                LocalDate startDate = ATTENDANCE_START.plusDays(random.nextInt(101));
                
                // Random duration: 2-5 days
                int days = random.nextInt(4) + 2;
                LocalDate endDate = startDate.plusDays(days - 1);
                
                TipoIncapacidad tipo = random.nextBoolean() ? 
                    TipoIncapacidad.ENFERMEDAD_COMUN : TipoIncapacidad.ACCIDENTE_LABORAL;
                
                Incapacidades incapacidad = Incapacidades.builder()
                    .fechaInicio(startDate)
                    .fechaFin(endDate)
                    .diasTotales(days)
                    .tipoIncapacidad(tipo)
                    .estadoSolicitud(EstadoSolicitud.APROBADA)
                    .porcentajePago(tipo == TipoIncapacidad.ENFERMEDAD_COMUN ? 60.0 : 100.0)
                    .entidadEmisora(TipoEntidadEmisora.CCSS)
                    .numeroDocumento("INC-" + random.nextInt(100000))
                    .observaciones("Incapacidad médica por " + tipo.name().toLowerCase().replace("_", " "))
                    .fechaSolicitud(startDate)
                    .fechaAprobacionJefe(startDate)
                    .fechaAprobacionRH(startDate.plusDays(1))
                    .esExtension(false)
                    .empleado(empleado)
                    .build();
                
                incapacityRecords.add(incapacidad);
            }
        }

        incapacidadesRepositorio.saveAll(incapacityRecords);
        return incapacityRecords.size();
    }

    /**
     * Generates occasional permit requests.
     * Each employee has 25% chance of 1 permit.
     */
    private int generatePermitRecords(List<Empleados> empleados) {
        List<Permisos> permitRecords = new ArrayList<>();

        for (Empleados empleado : empleados) {
            // 25% chance of permit
            if (random.nextInt(100) < 25) {
                // Random date between Jan 1 and Apr 15, 2026
                LocalDate permitDate = ATTENDANCE_START.plusDays(random.nextInt(106));
                
                // Random permit type (excluding VACACIONES)
                TipoPermiso[] tiposPermiso = {
                    TipoPermiso.PERSONAL, 
                    TipoPermiso.MEDICO, 
                    TipoPermiso.ESTUDIO
                };
                TipoPermiso tipo = tiposPermiso[random.nextInt(tiposPermiso.length)];
                
                // Most permits are for 1 day, some for hours
                boolean isHourly = random.nextBoolean();
                
                Permisos.PermisosBuilder permitBuilder = Permisos.builder()
                    .fechaInicio(permitDate)
                    .tipoPermiso(tipo)
                    .estadoSolicitud(EstadoSolicitud.APROBADA)
                    .fechaSolicitud(permitDate.minusDays(3))
                    .fechaAprobacionJefe(permitDate.minusDays(2))
                    .fechaAprobacionRH(permitDate.minusDays(1))
                    .empleado(empleado);
                
                if (isHourly) {
                    permitBuilder
                        .fechaFin(permitDate)
                        .diasTotales(0)
                        .horaInicio("09:00")
                        .horaFin("12:00")
                        .totalHoras(3.0)
                        .motivo("Trámite personal - 3 horas");
                } else {
                    permitBuilder
                        .fechaFin(permitDate)
                        .diasTotales(1)
                        .motivo("Permiso de " + tipo.name().toLowerCase());
                }
                
                permitRecords.add(permitBuilder.build());
            }
        }

        permisosRepositorio.saveAll(permitRecords);
        return permitRecords.size();
    }
}