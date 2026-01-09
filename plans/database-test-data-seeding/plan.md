# Database Test Data Seeding with CommandLineRunner

**Branch:** `feature/database-test-data-seeding`
**Description:** Populate database with realistic test data for 20 employees, positions, departments, attendance records, and department heads using CommandLineRunner

## Goal
Implement a Spring Boot CommandLineRunner that automatically seeds the database with comprehensive test data including 20 employees across multiple departments, their associated users with pre-configured passwords, positions with realistic schedules, attendance records, vacation balances, and department heads. This eliminates the need for manual data entry during development and testing.

## Prerequisites & Clarifications Needed

### [NEEDS CLARIFICATION] Positions & Departments Structure
Based on the image provided, I can see these positions exist:
- **Sastre** (₡450,000) - 08:00:00 to 17:00:00
- **Gerente de RH** (₡995,000) - 09:00:00 to 17:00:00  
- **Vendedor** (₡350,000) - 08:00:00 to 17:00:00
- **Ingeniero de Software** (₡1,200,000) - 08:00:00 to 17:00:00

**Please confirm:**
1. What departments should these positions belong to? Suggested:
   - **Producción**: Sastre
   - **Administración**: Gerente de RH
   - **Ventas**: Vendedor
   - **Tecnología/IT**: Ingeniero de Software

2. Should we create additional positions to reach 20 employees? Suggested additional positions:
   - **Producción**: Sastre Junior (₡350,000), Cortador de Tela (₡400,000)
   - **Administración**: Contador (₡750,000), Recepcionista (₡450,000)
   - **Ventas**: Asesor de Imagen (₡450,000)

3. Should all positions have the same schedule (08:00-17:00) or vary by role?

### [NEEDS CLARIFICATION] Employee Distribution
How should the 20 employees be distributed across positions? Suggested:
- **Sastre**: 6 employees (main production role)
- **Vendedor**: 4 employees
- **Gerente de RH**: 1 employee (also department head)
- **Ingeniero de Software**: 2 employees
- **Other positions**: Remaining employees

### [NEEDS CLARIFICATION] Attendance Data Generation
For attendance records:
1. How many days of historical data should be generated? Suggested: Last 30 days
2. Should we simulate:
   - Perfect attendance (always on time)?
   - Realistic attendance (some late arrivals, early departures)?
   - Random absences?
3. Should weekends be excluded from attendance generation?

### [NEEDS CLARIFICATION] Password for Test Users
You mentioned "you can use the same password" - what password should be used for all test users?
- **Suggested default**: `TestPass123!` (meets validation: 8+ chars, uppercase, lowercase, number, special)
- Set `passwordChangeRequired = false` so users don't need to change it

### [NEEDS CLARIFICATION] Execution Trigger
Should the CommandLineRunner:
1. **Always run on startup** (checks if data exists first)
2. **Run only if database is empty** (recommended - prevents duplicates)
3. **Be manually triggered via profile** (e.g., `--spring.profiles.active=seed-data`)

**Recommended**: Option 2 - Check if departamentos table has records, skip seeding if data exists.

---

## Implementation Steps

### Step 1: Create CommandLineRunner Infrastructure
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java` (new file)

**What:** Create a `DataSeeder` component implementing `CommandLineRunner` that injects all necessary repositories (DepartamentoRepositorio, PuestosRepositorio, DireccionRepositorio, UserRepository, EmpleadosRepositorio, JefesDepartamentoRepositorio, AsistenciaRepositorio) and PasswordEncoder. Implement existence check logic to prevent duplicate seeding on subsequent application restarts.

**Testing:** 
1. Start application - verify console shows "Seeding database with test data..." log
2. Restart application - verify console shows "Database already seeded, skipping..." log
3. Check that @Component annotation is present and Spring detects the bean

- [x] Step 1 completed: `DataSeeder` created with existence check (skips seeding when employees exist)

---

### Step 2: Seed Departments and Positions
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** Create and save 3-4 departments (Producción, Administración, Ventas, Tecnología) with their associated positions. For each position, set realistic salarioMinimo, horaEntrada (08:00:00 or 09:00:00), and horaSalida (17:00:00). Use the positions from the user's screenshot as reference. Store created entities in instance variables/lists for use in subsequent steps.

**Testing:**
1. Query `SELECT * FROM departamentos` - verify 3-4 departments exist
2. Query `SELECT * FROM puestos` - verify 8-10 positions exist with correct salaries and schedules
3. Verify foreign key: `puesto.departamento_id` correctly references departamentos table

---

### Step 3: Seed Addresses for Employees
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** Create 20 realistic Costa Rican addresses using varied combinations of provincia (San José, Alajuela, Cartago, Heredia), canton, distrito from the costaRicaLocations data. Include descriptive "indicaciones" field with street-style addresses (e.g., "De la iglesia católica 200m norte, casa portón verde"). Store in a list for employee assignment.

**Testing:**
1. Query `SELECT * FROM direccion` - verify 20 addresses exist
2. Verify addresses use valid Costa Rica locations (San José, Escazú, Alajuela, etc.)
3. Check indicaciones field has realistic Costa Rican address format

---

### Step 4: Generate Users with Encoded Passwords
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** Create 20 User entities with unique usernames (format: first letter of nombre + full primerApellido, e.g., "jgarcia"). Encode the agreed password using `passwordEncoder.encode(password)`. Assign roles: 1 ADMIN, 1 HR, 4 JEFE (one per department), 14 EMPLEADO. Set `passwordChangeRequired = false` for all users so they're immediately usable. Save users and store references for employee linking.

**Testing:**
1. Query `SELECT username, role, password_change_required FROM users` - verify 20 users
2. Verify password field contains BCrypt hash (starts with `$2a$` or `$2b$`)
3. Verify role distribution: 1 ADMIN, 1 HR, 4 JEFE, 14 EMPLEADO
4. Verify passwordChangeRequired = false for all
5. Test login with one user - verify no password change redirect

---

### Step 5: Create 20 Employees with Realistic Data
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** Generate 20 Empleados entities with:
- **cedula**: Random 9-digit Costa Rican ID format (e.g., "401230567")
- **nombre, primerApellido, segundoApellido**: Realistic Costa Rican names
- **correoPersonal**: Format `{firstname}.{lastname}@gmail.com`
- **fechaNacimiento**: Random dates ensuring 18+ years old (born between 1975-2000)
- **fechaIngreso**: Random past dates within last 5 years (2020-2025)
- **salarioBase**: Match or exceed position's salarioMinimo
- **saldoVacaciones**: Calculate based on fechaIngreso (e.g., 14 days per year worked, max 30)
- **cantidadDeHijos**: Random 0-3
- **estaCasado, estaActivo**: Randomize (estaActivo mostly true)
- **tipoDeJornada**: COMPLETA for most, some MEDIA_JORNADA
- **cuentaIban**: Optional, generate for 50% of employees (Costa Rica format: 22 chars)
- Link to saved Puestos, Direccion, and User entities

**Testing:**
1. Query `SELECT COUNT(*) FROM empleados` - verify exactly 20 employees
2. Verify each employee has valid puesto_id, direccion_id, usuario_id foreign keys
3. Check salarioBase >= position's salarioMinimo for each employee
4. Verify saldoVacaciones matches employment duration (rough formula: years * 14 days)
5. Query users table - verify each user has corresponding empleado_id (bidirectional link)

---

### Step 6: Assign Department Heads (Jefes de Departamento)
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** For each of the 4 departments, select one employee with role JEFE who works in that department. Create JefesDepartamento entities with:
- **departamento**: Reference to department
- **empleado**: The selected employee
- **fechaInicio**: Their fechaIngreso date
- **fechaFin**: null (currently active)
- **estaActivo**: true

Ensure the selected employee's position belongs to the correct department.

**Testing:**
1. Query `SELECT d.nombre, e.nombre, e.primer_apellido, jd.fecha_inicio FROM jefes_departamento jd JOIN departamentos d ON jd.departamento_id = d.id JOIN empleados e ON jd.empleado_id = e.id WHERE jd.esta_activo = true` 
2. Verify exactly 4 active department heads
3. Verify each department has exactly one active jefe
4. Cross-check: employees marked as jefes have role = JEFE in users table
5. Verify fechaFin is null for all active heads

---

### Step 7: Generate Attendance Records (Last 30 Days)
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** For each active employee (estaActivo = true), generate ENTRADA and SALIDA attendance records for weekdays (Monday-Friday) over the last 30 calendar days. Calculate timestamp based on employee's position schedule with realistic variance:
- **ENTRADA**: position.horaEntrada ± random(-10 to +30 minutes)
- **SALIDA**: position.horaSalida ± random(-15 to +15 minutes)
- **fechaHora**: Combine date with calculated time as LocalDateTime
- Skip weekends (Saturday, Sunday)
- Randomly skip 5-10% of days to simulate absences/vacations
- Set observaciones to null for most, add notes for late arrivals (">15 min tarde")

**Testing:**
1. Query `SELECT COUNT(*) FROM asistencias` - verify ~400-600 total records (20 employees × ~20 workdays × 2 records)
2. Verify each workday has paired ENTRADA + SALIDA for most employees
3. Check no records exist for weekends
4. Verify fechaHora timestamps fall within last 30 days
5. Sample query: `SELECT * FROM asistencias WHERE empleado_id = 1 ORDER BY fecha_hora` - verify chronological ENTRADA/SALIDA pattern

---

### Step 8: Add Optional Test Data (Permisos, HorasExtra)
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** *[OPTIONAL - IMPLEMENT IF TIME PERMITS]* Enhance test data realism by generating:
- **Permisos**: 5-10 permission requests (mix of APROBADA, PENDIENTE, RECHAZADA) with varied tipoPermiso (PERSONAL, MEDICO, etc.)
- **HorasExtra**: 10-15 overtime requests, mostly APROBADA with cantidadDeHoras between 2-8

This step is optional but adds depth to the test dataset for more comprehensive module testing.

**Testing:**
1. Query `SELECT COUNT(*) FROM permisos` - verify 5-10 records
2. Query `SELECT COUNT(*) FROM horas_extra` - verify 10-15 records
3. Verify foreign keys link correctly to empleados
4. Verify estadoSolicitud enum values are valid

---

### Step 9: Add Logging and Error Handling
**Files:** 
- `src/main/java/com/anthony/tfg/tfg/Config/DataSeeder.java`

**What:** Enhance DataSeeder with comprehensive logging using `LoggerFactory.getLogger()`. Add try-catch blocks around each seeding phase to handle potential constraint violations or data errors gracefully. Log summary statistics after completion: "Seeded X departments, Y positions, Z employees, W attendance records". Add transactional annotations if needed to ensure atomic commits.

**Testing:**
1. Review application startup logs - verify clear progress messages for each phase
2. Verify final summary log shows correct counts
3. Intentionally corrupt data (e.g., duplicate username) - verify error handling doesn't crash application
4. Verify all transactions commit successfully (check database records persist)

---

## Post-Implementation Verification

After completing all steps, perform comprehensive validation:

### Database Queries
```sql
-- Verify record counts
SELECT 'Departments' as entity, COUNT(*) as count FROM departamentos
UNION ALL SELECT 'Positions', COUNT(*) FROM puestos
UNION ALL SELECT 'Addresses', COUNT(*) FROM direccion  
UNION ALL SELECT 'Users', COUNT(*) FROM users
UNION ALL SELECT 'Employees', COUNT(*) FROM empleados
UNION ALL SELECT 'Dept Heads', COUNT(*) FROM jefes_departamento WHERE esta_activo = true
UNION ALL SELECT 'Attendance', COUNT(*) FROM asistencias;

-- Verify referential integrity
SELECT e.id, e.nombre, e.primer_apellido, 
       p.nombre as puesto, 
       d.provincia, 
       u.username, u.role
FROM empleados e
JOIN puestos p ON e.puesto_id = p.id
JOIN direccion d ON e.direccion_id = d.id
JOIN users u ON e.usuario_id = u.id
LIMIT 5;
```

### Application Testing
1. **Login Test**: Use generated credentials (e.g., `jgarcia` / agreed password) - verify successful login without password change prompt
2. **Frontend Visualization**: 
   - Navigate to Empleados module - verify 20 employees display
   - Check Asistencia module - verify attendance records appear
   - View Puestos in admin panel - verify positions with correct departments
3. **Role-Based Access**: Login as JEFE user - verify department management access
4. **Data Quality**: Spot-check employee details - verify realistic names, valid cédulas, positive vacation balances

---

## Technical Notes

### Dependencies
All required dependencies already exist:
- Spring Boot Starter Data JPA
- PostgreSQL Driver
- Spring Security (PasswordEncoder)
- Lombok (for entity builders)

### Execution Order is Critical
```
Departamentos → Puestos (needs departamentos)
                 ↓
              Direcciones (independent)
                 ↓
              Users (independent)
                 ↓
              Empleados (needs all above)
                 ↓
         JefesDepartamento (needs empleados + departamentos)
                 ↓
         Asistencias, Permisos, etc. (need empleados)
```

### Performance Considerations
- Use `.saveAll()` for batch inserts when saving multiple entities of same type
- Consider `@Transactional` on `run()` method for atomic commits
- Expected execution time: 2-5 seconds for 20 employees + 30 days attendance

### Data Cleanup (for testing)
To reset and re-seed database:
```sql
-- Truncate tables in reverse dependency order
TRUNCATE asistencias, jefes_departamento, empleados, users, direccion, puestos, departamentos CASCADE;
```
Then restart application to trigger seeding.

---

## Rollback Plan

If issues arise:
1. Comment out `@Component` annotation on DataSeeder - prevents execution
2. Use database truncate script to clear test data
3. Fix issues in DataSeeder code
4. Uncomment `@Component` and restart

---

## Success Criteria

✅ Application starts without errors  
✅ Database contains exactly 20 employees with complete profiles  
✅ All employees have valid users with encoded passwords  
✅ Each of 4 departments has one assigned jefe  
✅ Attendance records span last 30 workdays  
✅ Test login succeeds with generated credentials  
✅ Frontend modules display seeded data correctly  
✅ No manual SQL execution required  
✅ Subsequent application restarts don't duplicate data
