# Database Test Data Seeding Implementation

## Goal
Implement a Spring Boot CommandLineRunner that automatically seeds the database with comprehensive test data including 20 employees (15 new + 5 existing users), positions, addresses, attendance records, and department heads on every startup, checking if employees table is empty first.

## Prerequisites
Make sure you are currently on the `feature/database-test-data-seeding` branch before beginning implementation.
If not, move to the correct branch. If the branch does not exist, create it from main.

---

### Step-by-Step Instructions

#### Step 1: Create DataSeeder CommandLineRunner

- [ ] Create the Config directory if it doesn't exist
- [ ] Copy and paste the code below into `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`:

```java
package com.anthony.tfg.tfg.Config;

import java.sql.Date;
import java.sql.Time;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
import com.anthony.tfg.tfg.Entidades.Enums.TipoDeJornada;
import com.anthony.tfg.tfg.Entidades.Enums.TipoEvento;
import com.anthony.tfg.tfg.Entidades.JefesDepartamento;
import com.anthony.tfg.tfg.Entidades.Puestos;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.Role;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.User;
import com.anthony.tfg.tfg.Modulos.Seguridad.user.UserRepository;
import com.anthony.tfg.tfg.Repositorios.AsistenciaRepositorio;
import com.anthony.tfg.tfg.Repositorios.DepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.DireccionRepositorio;
import com.anthony.tfg.tfg.Repositorios.EmpleadosRepositorio;
import com.anthony.tfg.tfg.Repositorios.JefesDepartamentoRepositorio;
import com.anthony.tfg.tfg.Repositorios.PuestosRepositorio;

import lombok.RequiredArgsConstructor;

/**
 * DataSeeder - Seeds the database with test data on application startup.
 * 
 * Checks if employees table is empty before seeding to prevent duplicates.
 * Creates 20 employees with users, addresses, positions, department heads,
 * and 30 days of attendance records.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);
    private static final String DEFAULT_PASSWORD = "TestPass123!";
    private static final int ATTENDANCE_DAYS = 30;

    private final DepartamentoRepositorio departamentoRepositorio;
    private final PuestosRepositorio puestosRepositorio;
    private final DireccionRepositorio direccionRepositorio;
    private final UserRepository userRepository;
    private final EmpleadosRepositorio empleadosRepositorio;
    private final JefesDepartamentoRepositorio jefesDepartamentoRepositorio;
    private final AsistenciaRepositorio asistenciaRepositorio;
    private final PasswordEncoder passwordEncoder;

    private final Random random = new Random(42); // Fixed seed for reproducibility

    @Override
    @Transactional
    public void run(String... args) {
        if (empleadosRepositorio.count() > 0) {
            logger.info("Database already seeded (employees exist), skipping...");
            return;
        }

        logger.info("=== Starting database seeding ===");

        try {
            // Step 1: Create additional positions (departments and some positions already exist)
            List<Puestos> puestos = createAdditionalPositions();
            logger.info("Created/retrieved {} positions", puestos.size());

            // Step 2: Create addresses for 20 employees
            List<Direccion> direcciones = createAddresses();
            logger.info("Created {} addresses", direcciones.size());

            // Step 3: Create users (15 new, 5 existing)
            List<User> users = createUsers();
            logger.info("Created/retrieved {} users", users.size());

            // Step 4: Create 20 employees
            List<Empleados> empleados = createEmployees(puestos, direcciones, users);
            logger.info("Created {} employees", empleados.size());

            // Step 5: Assign department heads
            int jefeCount = assignDepartmentHeads(empleados);
            logger.info("Assigned {} department heads", jefeCount);

            // Step 6: Generate attendance records (last 30 days)
            int attendanceCount = generateAttendanceRecords(empleados);
            logger.info("Generated {} attendance records", attendanceCount);

            logger.info("=== Database seeding completed successfully ===");
            logger.info("Summary: {} employees, {} addresses, {} users, {} dept heads, {} attendance records",
                    empleados.size(), direcciones.size(), users.size(), jefeCount, attendanceCount);

        } catch (Exception e) {
            logger.error("Error during database seeding: {}", e.getMessage(), e);
            throw new RuntimeException("Database seeding failed", e);
        }
    }

    /**
     * Creates additional positions using existing departments.
     * Existing positions (from DB): Sastre, Gerente de RH, Vendedor, Ingeniero de Software
     * New positions will be created for other departments.
     */
    private List<Puestos> createAdditionalPositions() {
        List<Puestos> allPositions = new ArrayList<>();
        
        // Get existing positions first
        allPositions.addAll(puestosRepositorio.findAll());
        
        // Get departments by ID (based on existing data)
        Departamento recursosHumanos = departamentoRepositorio.findById(1L).orElseThrow();
        Departamento tecnologia = departamentoRepositorio.findById(2L).orElseThrow();
        Departamento ventas = departamentoRepositorio.findById(3L).orElseThrow();
        Departamento taller = departamentoRepositorio.findById(4L).orElseThrow();
        Departamento administracion = departamentoRepositorio.findById(5L).orElseThrow();
        Departamento gerencia = departamentoRepositorio.findById(6L).orElseThrow();
        Departamento finanzas = departamentoRepositorio.findById(8L).orElseThrow();

        Time horaEntrada = Time.valueOf("08:00:00");
        Time horaSalida = Time.valueOf("17:00:00");

        // Create additional positions for each department to have enough for 20 employees
        List<Puestos> newPositions = List.of(
            // Taller - additional positions
            Puestos.builder()
                .nombre("Sastre Junior")
                .salarioMinimo(350000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(taller)
                .build(),
            Puestos.builder()
                .nombre("Cortador de Tela")
                .salarioMinimo(400000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(taller)
                .build(),
            // Administracion
            Puestos.builder()
                .nombre("Asistente Administrativo")
                .salarioMinimo(380000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(administracion)
                .build(),
            Puestos.builder()
                .nombre("Recepcionista")
                .salarioMinimo(320000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(administracion)
                .build(),
            // Recursos Humanos - additional
            Puestos.builder()
                .nombre("Asistente de RH")
                .salarioMinimo(420000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(recursosHumanos)
                .build(),
            // Finanzas
            Puestos.builder()
                .nombre("Contador")
                .salarioMinimo(650000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(finanzas)
                .build(),
            Puestos.builder()
                .nombre("Asistente Contable")
                .salarioMinimo(400000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(finanzas)
                .build(),
            // Gerencia
            Puestos.builder()
                .nombre("Gerente General")
                .salarioMinimo(1500000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(gerencia)
                .build(),
            // Ventas - additional
            Puestos.builder()
                .nombre("Asesor de Imagen")
                .salarioMinimo(450000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(ventas)
                .build(),
            // Tecnologia - additional
            Puestos.builder()
                .nombre("Soporte Técnico")
                .salarioMinimo(480000.0)
                .horaEntrada(horaEntrada)
                .horaSalida(horaSalida)
                .departamento(tecnologia)
                .build()
        );

        puestosRepositorio.saveAll(newPositions);
        allPositions.addAll(newPositions);
        
        return allPositions;
    }

    /**
     * Creates 20 realistic Costa Rican addresses.
     */
    private List<Direccion> createAddresses() {
        List<Direccion> addresses = new ArrayList<>();
        
        // Costa Rica address data
        String[][] locationData = {
            {"San José", "San José", "Carmen", "Del Teatro Nacional 100m norte, edificio azul"},
            {"San José", "Escazú", "San Rafael", "Centro comercial Multiplaza, 200m oeste"},
            {"San José", "Desamparados", "Desamparados", "Frente al parque central, casa esquinera"},
            {"San José", "Curridabat", "Curridabat", "Pinares, de la iglesia 150m sur"},
            {"San José", "Santa Ana", "Santa Ana", "Forum, edificio torre 2, piso 3"},
            {"Alajuela", "Alajuela", "Alajuela", "Del mercado central 300m norte"},
            {"Alajuela", "San Ramón", "San Ramón", "Barrio San José, casa portón verde"},
            {"Alajuela", "Grecia", "Grecia", "Del parque central 50m este"},
            {"Cartago", "Cartago", "Oriental", "Barrio Los Ángeles, frente a la escuela"},
            {"Cartago", "La Unión", "Tres Ríos", "Condominio Valle Verde, casa 15"},
            {"Heredia", "Heredia", "Heredia", "Del correo 200m sur, apartamento 3B"},
            {"Heredia", "Santo Domingo", "Santo Domingo", "Urbanización La Valencia, casa 22"},
            {"Heredia", "Barva", "Barva", "Centro, del banco 100m oeste"},
            {"San José", "Moravia", "San Vicente", "Trinidad, del super 50m norte"},
            {"San José", "Tibás", "San Juan", "Cuatro Reinas, avenida 8"},
            {"San José", "Montes de Oca", "San Pedro", "Barrio Los Yoses, calle 35"},
            {"San José", "Goicoechea", "Guadalupe", "Urbanización Monterrey, casa 8"},
            {"Alajuela", "Atenas", "Atenas", "Centro, de la iglesia 200m sur"},
            {"Heredia", "San Pablo", "San Pablo", "Calle principal, edificio Don Carlos"},
            {"San José", "Vázquez de Coronado", "San Isidro", "Residencial El Alto, casa 45"}
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
     * Creates 15 new users + retrieves 5 existing users = 20 total.
     * Existing users: Empleado, Admin, Jefe, HHRR, aalvarez
     */
    private List<User> createUsers() {
        List<User> allUsers = new ArrayList<>();
        
        // Retrieve existing users (from DB screenshot)
        userRepository.findByUsername("Empleado").ifPresent(allUsers::add);
        userRepository.findByUsername("Admin").ifPresent(allUsers::add);
        userRepository.findByUsername("Jefe").ifPresent(allUsers::add);
        userRepository.findByUsername("HHRR").ifPresent(allUsers::add);
        userRepository.findByUsername("aalvarez").ifPresent(allUsers::add);

        String encodedPassword = passwordEncoder.encode(DEFAULT_PASSWORD);

        // Employee data: {username, role}
        // Distribution: 1 ADMIN (existing), 1 HR (existing), 4 JEFE (2 existing + 2 new), 14 EMPLEADO
        Object[][] newUserData = {
            {"jgarcia", Role.EMPLEADO},
            {"mrodriguez", Role.EMPLEADO},
            {"alopez", Role.EMPLEADO},
            {"cfernandez", Role.EMPLEADO},
            {"rmorales", Role.EMPLEADO},
            {"lhernandez", Role.EMPLEADO},
            {"jsanchez", Role.EMPLEADO},
            {"mvargas", Role.EMPLEADO},
            {"pcastro", Role.EMPLEADO},
            {"djimenez", Role.JEFE},       // Jefe Taller
            {"squiros", Role.JEFE},        // Jefe Finanzas
            {"cchaves", Role.EMPLEADO},
            {"rmora", Role.EMPLEADO},
            {"aaraya", Role.EMPLEADO},
            {"ngomez", Role.EMPLEADO}
        };

        for (Object[] userData : newUserData) {
            String username = (String) userData[0];
            Role role = (Role) userData[1];
            
            User user = User.builder()
                .username(username)
                .password(encodedPassword)
                .role(role)
                .passwordChangeRequired(false)
                .build();
            allUsers.add(userRepository.save(user));
        }

        return allUsers;
    }

    /**
     * Creates 20 employees linked to users, positions, and addresses.
     */
    private List<Empleados> createEmployees(List<Puestos> puestos, List<Direccion> direcciones, List<User> users) {
        List<Empleados> empleados = new ArrayList<>();

        // Employee data: name, firstLastName, secondLastName, birthDate, startDate, position index, user index
        // Position distribution based on available positions:
        // ID 1: Sastre (Taller), ID 2: Gerente de RH, ID 3: Vendedor, ID 4: Ingeniero de Software
        // New positions (IDs 5-14): Sastre Junior, Cortador, Asist Admin, Recepcionista, Asist RH,
        //                           Contador, Asist Contable, Gerente General, Asesor Imagen, Soporte Técnico
        
        Object[][] employeeData = {
            // Existing users will be matched to employees
            {"Carlos", "Rodríguez", "Vargas", "1990-03-15", "2022-01-10", 0, 0, 500000.0, "101230456"},   // Sastre - Empleado user
            {"María", "González", "Solano", "1985-07-22", "2020-06-15", 1, 1, 1100000.0, "102340567"},    // Gerente RH - Admin user
            {"José", "Fernández", "Castro", "1988-11-08", "2021-03-20", 2, 2, 400000.0, "103450678"},     // Vendedor - Jefe user
            {"Ana", "López", "Mora", "1992-05-12", "2023-02-01", 1, 3, 995000.0, "104560789"},            // Gerente RH - HHRR user
            {"Pedro", "Álvarez", "Jiménez", "1987-09-30", "2019-08-05", 3, 4, 1300000.0, "105670890"},    // Ing Software - aalvarez user
            
            // New users - new employees
            {"Juan", "García", "Pérez", "1995-02-18", "2024-01-15", 4, 5, 380000.0, "201230456"},         // Sastre Junior - jgarcia
            {"Mónica", "Rodríguez", "Vega", "1991-08-25", "2023-05-10", 5, 6, 420000.0, "202340567"},     // Cortador - mrodriguez
            {"Alejandro", "López", "Soto", "1989-12-03", "2022-09-01", 6, 7, 400000.0, "203450678"},      // Asist Admin - alopez
            {"Carmen", "Fernández", "Rojas", "1993-04-17", "2024-03-01", 7, 8, 340000.0, "204560789"},    // Recepcionista - cfernandez
            {"Roberto", "Morales", "Arias", "1986-06-28", "2021-11-15", 8, 9, 450000.0, "205670890"},     // Asist RH - rmorales
            {"Laura", "Hernández", "Quesada", "1994-10-09", "2023-07-20", 9, 10, 700000.0, "206780901"},  // Contador - lhernandez
            {"Jorge", "Sánchez", "Villalobos", "1990-01-14", "2022-04-05", 10, 11, 420000.0, "207890012"},// Asist Contable - jsanchez
            {"Melissa", "Vargas", "Solís", "1988-03-21", "2020-02-10", 11, 12, 1600000.0, "208901123"},   // Gerente General - mvargas
            {"Pablo", "Castro", "Montero", "1996-07-05", "2024-06-01", 12, 13, 480000.0, "209012234"},    // Asesor Imagen - pcastro
            {"Daniel", "Jiménez", "Cordero", "1992-11-30", "2023-01-10", 0, 14, 480000.0, "210123345"},   // Sastre - djimenez (Jefe Taller)
            {"Sandra", "Quirós", "Brenes", "1987-05-08", "2019-10-01", 9, 15, 750000.0, "211234456"},     // Contador - squiros (Jefe Finanzas)
            {"Carlos", "Chaves", "Ramírez", "1993-09-12", "2024-02-15", 4, 16, 370000.0, "212345567"},    // Sastre Junior - cchaves
            {"Ricardo", "Mora", "Valverde", "1991-02-28", "2022-08-20", 2, 17, 380000.0, "213456678"},    // Vendedor - rmora
            {"Andrea", "Araya", "Calderón", "1989-06-15", "2021-05-12", 13, 18, 520000.0, "214567789"},   // Soporte Técnico - aaraya
            {"Nelson", "Gómez", "Vindas", "1994-12-22", "2023-09-05", 3, 19, 1250000.0, "215678890"}      // Ing Software - ngomez
        };

        for (int i = 0; i < employeeData.length; i++) {
            Object[] data = employeeData[i];
            
            String nombre = (String) data[0];
            String primerApellido = (String) data[1];
            String segundoApellido = (String) data[2];
            Date fechaNacimiento = Date.valueOf((String) data[3]);
            Date fechaIngreso = Date.valueOf((String) data[4]);
            int puestoIndex = (Integer) data[5];
            int userIndex = (Integer) data[6];
            Double salario = (Double) data[7];
            String cedula = (String) data[8];
            
            // Calculate vacation balance based on years worked (14 days per year, max 30)
            long yearsWorked = java.time.temporal.ChronoUnit.YEARS.between(
                fechaIngreso.toLocalDate(), LocalDate.now());
            int saldoVacaciones = Math.min((int) (yearsWorked * 14), 30);
            if (saldoVacaciones < 14) saldoVacaciones = 14; // Minimum 14 days

            // Generate IBAN for ~50% of employees
            String cuentaIban = (i % 2 == 0) ? generateCostaRicanIban() : null;

            Empleados empleado = Empleados.builder()
                .cedula(cedula)
                .nombre(nombre)
                .primerApellido(primerApellido)
                .segundoApellido(segundoApellido)
                .correoPersonal(nombre.toLowerCase() + "." + primerApellido.toLowerCase() + "@gmail.com")
                .fechaNacimiento(fechaNacimiento)
                .fechaIngreso(fechaIngreso)
                .salarioBase(salario)
                .cantidadDeHijos(random.nextInt(4)) // 0-3 children
                .saldoVacaciones(saldoVacaciones)
                .cuentaIban(cuentaIban)
                .estaActivo(true)
                .estaCasado(random.nextBoolean())
                .tipoDeJornada(TipoDeJornada.COMPLETA)
                .puesto(puestos.get(puestoIndex))
                .direccion(direcciones.get(i))
                .usuario(users.get(userIndex))
                .build();

            empleados.add(empleadosRepositorio.save(empleado));
        }

        return empleados;
    }

    /**
     * Assigns department heads for key departments.
     * Uses employees with JEFE role.
     */
    private int assignDepartmentHeads(List<Empleados> empleados) {
        int count = 0;
        
        // Find employees with JEFE role and assign them as department heads
        for (Empleados empleado : empleados) {
            if (empleado.getUsuario() != null && 
                empleado.getUsuario().getRole() == Role.JEFE &&
                empleado.getPuesto() != null &&
                empleado.getPuesto().getDepartamento() != null) {
                
                JefesDepartamento jefe = JefesDepartamento.builder()
                    .departamento(empleado.getPuesto().getDepartamento())
                    .empleado(empleado)
                    .fechaInicio(empleado.getFechaIngreso().toLocalDate())
                    .fechaFin(null)
                    .estaActivo(true)
                    .build();
                
                jefesDepartamentoRepositorio.save(jefe);
                count++;
            }
        }
        
        return count;
    }

    /**
     * Generates realistic attendance records for the last 30 days.
     * - Weekdays only (Monday-Friday)
     * - ENTRADA around 08:00 with slight variance (-5 to +10 minutes)
     * - SALIDA around 17:00 with slight variance (-10 to +15 minutes)
     * - No absences (realistic attendance as requested)
     */
    private int generateAttendanceRecords(List<Empleados> empleados) {
        List<Asistencia> attendanceRecords = new ArrayList<>();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(ATTENDANCE_DAYS);

        for (Empleados empleado : empleados) {
            if (!empleado.getEstaActivo()) continue;

            // Get position schedule
            Time horaEntrada = empleado.getPuesto().getHoraEntrada();
            Time horaSalida = empleado.getPuesto().getHoraSalida();
            
            LocalTime baseEntrada = horaEntrada.toLocalTime();
            LocalTime baseSalida = horaSalida.toLocalTime();

            // Generate attendance for each weekday
            LocalDate currentDate = startDate;
            while (!currentDate.isAfter(today)) {
                DayOfWeek dayOfWeek = currentDate.getDayOfWeek();
                
                // Skip weekends
                if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                    // Generate ENTRADA with small variance (-5 to +10 minutes)
                    int entradaVariance = random.nextInt(16) - 5;
                    LocalTime entradaTime = baseEntrada.plusMinutes(entradaVariance);
                    LocalDateTime entradaDateTime = LocalDateTime.of(currentDate, entradaTime);
                    
                    // Check if late (more than 5 min after scheduled)
                    String observacionesEntrada = null;
                    if (entradaVariance > 5) {
                        observacionesEntrada = "Llegada " + entradaVariance + " minutos tarde";
                    }
                    
                    Asistencia entrada = Asistencia.builder()
                        .tipoEvento(TipoEvento.ENTRADA)
                        .fechaHora(entradaDateTime)
                        .observaciones(observacionesEntrada)
                        .empleado(empleado)
                        .build();
                    attendanceRecords.add(entrada);

                    // Generate SALIDA with small variance (-10 to +15 minutes)
                    int salidaVariance = random.nextInt(26) - 10;
                    LocalTime salidaTime = baseSalida.plusMinutes(salidaVariance);
                    LocalDateTime salidaDateTime = LocalDateTime.of(currentDate, salidaTime);
                    
                    String observacionesSalida = null;
                    if (salidaVariance > 10) {
                        observacionesSalida = "Horas extra: " + salidaVariance + " minutos";
                    }
                    
                    Asistencia salida = Asistencia.builder()
                        .tipoEvento(TipoEvento.SALIDA)
                        .fechaHora(salidaDateTime)
                        .observaciones(observacionesSalida)
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

    /**
     * Generates a valid Costa Rican IBAN (22 characters).
     * Format: CR + 2 check digits + 3 bank code + 14 account number
     */
    private String generateCostaRicanIban() {
        StringBuilder iban = new StringBuilder("CR");
        // Check digits (2)
        iban.append(String.format("%02d", random.nextInt(100)));
        // Bank code (3) - using common CR banks: 152 (BAC), 102 (Banco Nacional), 107 (BCR)
        String[] bankCodes = {"152", "102", "107"};
        iban.append(bankCodes[random.nextInt(bankCodes.length)]);
        // Account number (14 digits)
        for (int i = 0; i < 14; i++) {
            iban.append(random.nextInt(10));
        }
        return iban.toString();
    }
}
```

##### Step 1 Verification Checklist
- [ ] No build errors when running `./mvnw compile`
- [ ] File exists at `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`
- [ ] All imports resolve correctly

#### Step 1 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

#### Step 2: Start Application and Verify Seeding

- [ ] Start the backend application: `./mvnw spring-boot:run`
- [ ] Watch console logs for seeding messages:
  ```
  === Starting database seeding ===
  Created/retrieved X positions
  Created X addresses
  Created/retrieved X users
  Created X employees
  Assigned X department heads
  Generated X attendance records
  === Database seeding completed successfully ===
  ```

##### Step 2 Verification Checklist

Run these SQL queries in pgAdmin or your database client:

- [ ] **Verify positions created:**
```sql
SELECT id, nombre, salario_minimo, id_departamento 
FROM puestos ORDER BY id;
-- Should show 14 total positions (4 existing + 10 new)
```

- [ ] **Verify addresses created:**
```sql
SELECT COUNT(*) as total FROM direccion;
-- Should return 20
```

- [ ] **Verify users created:**
```sql
SELECT id, username, role, password_change_required 
FROM users ORDER BY id;
-- Should show 20 users total (5 existing + 15 new)
-- All new users should have password_change_required = false
```

- [ ] **Verify employees created:**
```sql
SELECT e.id, e.nombre, e.primer_apellido, e.cedula, 
       p.nombre as puesto, u.username
FROM empleados e
JOIN puestos p ON e.id_puesto = p.id
JOIN users u ON e.id_usuario = u.id
ORDER BY e.id;
-- Should show 20 employees
```

- [ ] **Verify department heads assigned:**
```sql
SELECT jd.id, d.nombre as departamento, 
       e.nombre || ' ' || e.primer_apellido as jefe,
       jd.fecha_inicio, jd.esta_activo
FROM jefes_departamento jd
JOIN departamentos d ON jd.id_departamento = d.id
JOIN empleados e ON jd.id_empleado = e.id
WHERE jd.esta_activo = true;
-- Should show 4 active department heads
```

- [ ] **Verify attendance records generated:**
```sql
SELECT COUNT(*) as total_records,
       COUNT(DISTINCT id_empleado) as employees_with_records
FROM asistencias;
-- Should show ~800+ records (20 employees × ~22 workdays × 2 events)
```

- [ ] **Verify attendance pattern for one employee:**
```sql
SELECT fecha_hora, tipo_evento, observaciones
FROM asistencias
WHERE id_empleado = 1
ORDER BY fecha_hora
LIMIT 20;
-- Should show alternating ENTRADA/SALIDA pairs for weekdays only
```

- [ ] **Verify no weekend records:**
```sql
SELECT COUNT(*) as weekend_records
FROM asistencias
WHERE EXTRACT(DOW FROM fecha_hora) IN (0, 6);
-- Should return 0 (no Saturday/Sunday records)
```

- [ ] **Test login with new user:**
  - Username: `jgarcia`
  - Password: `TestPass123!`
  - Should login successfully without password change prompt

#### Step 2 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

#### Step 3: Verify Idempotency (Re-run Protection)

- [ ] Restart the application again: `./mvnw spring-boot:run`
- [ ] Verify console shows: `Database already seeded (employees exist), skipping...`
- [ ] Verify no duplicate records were created:

```sql
SELECT COUNT(*) FROM empleados;  -- Should still be 20
SELECT COUNT(*) FROM users;      -- Should still be 20  
SELECT COUNT(*) FROM direccion;  -- Should still be 20
SELECT COUNT(*) FROM asistencias; -- Should still be ~800
```

##### Step 3 Verification Checklist
- [ ] Application starts without errors
- [ ] Skip message appears in logs
- [ ] Record counts unchanged after restart

#### Step 3 STOP & COMMIT
**STOP & COMMIT:** Agent must stop here and wait for the user to test, stage, and commit the change.

---

## Summary of Created Data

### Positions (14 total)
| ID | Position | Department | Salary |
|----|----------|------------|--------|
| 1 | Sastre | Taller | ₡450,000 |
| 2 | Gerente de RH | Recursos Humanos | ₡995,000 |
| 3 | Vendedor | Ventas | ₡350,000 |
| 4 | Ingeniero de Software | Tecnología | ₡1,200,000 |
| 5 | Sastre Junior | Taller | ₡350,000 |
| 6 | Cortador de Tela | Taller | ₡400,000 |
| 7 | Asistente Administrativo | Administración | ₡380,000 |
| 8 | Recepcionista | Administración | ₡320,000 |
| 9 | Asistente de RH | Recursos Humanos | ₡420,000 |
| 10 | Contador | Finanzas | ₡650,000 |
| 11 | Asistente Contable | Finanzas | ₡400,000 |
| 12 | Gerente General | Gerencia | ₡1,500,000 |
| 13 | Asesor de Imagen | Ventas | ₡450,000 |
| 14 | Soporte Técnico | Tecnología | ₡480,000 |

### Users (20 total)
| Username | Role | Type |
|----------|------|------|
| Empleado | EMPLEADO | Existing |
| Admin | ADMIN | Existing |
| Jefe | JEFE | Existing |
| HHRR | HR | Existing |
| aalvarez | JEFE | Existing |
| jgarcia | EMPLEADO | New |
| mrodriguez | EMPLEADO | New |
| alopez | EMPLEADO | New |
| cfernandez | EMPLEADO | New |
| rmorales | EMPLEADO | New |
| lhernandez | EMPLEADO | New |
| jsanchez | EMPLEADO | New |
| mvargas | EMPLEADO | New |
| pcastro | EMPLEADO | New |
| djimenez | JEFE | New |
| squiros | JEFE | New |
| cchaves | EMPLEADO | New |
| rmora | EMPLEADO | New |
| aaraya | EMPLEADO | New |
| ngomez | EMPLEADO | New |

### Test Credentials
- **Password for all new users:** `TestPass123!`
- **Example logins:** `jgarcia`, `mrodriguez`, `djimenez`

### Employee Distribution
- **Taller (Sastre/Sastre Junior/Cortador):** 5 employees
- **Recursos Humanos:** 3 employees
- **Ventas:** 3 employees
- **Tecnología:** 3 employees
- **Administración:** 2 employees
- **Finanzas:** 3 employees
- **Gerencia:** 1 employee
