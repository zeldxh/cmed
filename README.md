# CitaMed

Sistema web de gestión de citas médicas desarrollado como proyecto final del curso SC-403 Desarrollo de Aplicaciones Web y Patrones de la Universidad Fidélitas.

## Descripción

CitaMed permite que los pacientes registren su cuenta, consulten disponibilidad y reserven citas. Los doctores administran su agenda, bloquean horarios y registran la atención. Los administradores gestionan pacientes, médicos, especialidades, citas, reportes y el historial de notificaciones enviadas por correo.

La aplicación utiliza una estructura MVC con persistencia relacional, autenticación y autorización con Spring Security, vistas dinámicas construidas con Thymeleaf y Bootstrap, e internacionalización funcional en español e inglés.

## Autor

Desarrollado por [zeldxh](https://github.com/zeldxh).

## Tecnologías

- Java 21
- Spring Boot
- Spring Security (autenticación, autorización por rol, BCrypt)
- Spring Mail (notificaciones automáticas del ciclo de vida de una cita)
- OpenPDF (exportación de reportes a PDF)
- Thymeleaf + thymeleaf-extras-springsecurity6
- Bootstrap 5
- Hibernate / JPA
- MySQL
- Maven

## Estructura

```text
src/main/java/com/citamed/
├── SecurityConfig.java
├── ProjectConfig.java
├── controller/
├── domain/
├── repository/
└── service/

src/main/resources/
├── templates/
├── static/css/
├── application.properties
├── messages.properties, messages_es.properties, messages_en.properties
└── citamed.sql
```

## Ejecución

Requisitos:

- Java 21
- Maven 3.8 o superior
- MySQL 8 o superior
- NetBeans con soporte para Maven (o cualquier IDE con soporte para Maven)

Pasos:

1. Ejecutar `src/main/resources/citamed.sql` en MySQL. El script crea la base `citamed_db`, las 8 tablas del sistema, el usuario de aplicación y los datos de prueba.
2. (Opcional) Copiar `.env.example` como `.env` y ajustar los valores según tu entorno local; ver la sección "Variables de entorno" más abajo.
3. Abrir `cmed` como proyecto Maven en NetBeans (o ejecutar `mvn spring-boot:run`).
4. Ejecutar el proyecto.
5. Abrir `http://localhost:91/`.

### Variables de entorno

La configuración sensible (conexión a MySQL y credenciales SMTP) se lee de variables de entorno, con valores por defecto de desarrollo local si no se definen:

| Variable | Uso | Valor por defecto |
| --- | --- | --- |
| `DB_URL` | URL JDBC de MySQL | `jdbc:mysql://localhost:3306/citamed_db` |
| `DB_USERNAME` | Usuario de la base de datos | `usuario_citamed` |
| `DB_PASSWORD` | Contraseña de la base de datos | `la_Clave` |
| `MAIL_USERNAME` | Cuenta SMTP para las notificaciones | vacío (los envíos quedan registrados como no enviados) |
| `MAIL_PASSWORD` | Contraseña de aplicación de la cuenta SMTP | vacío |

El archivo `.env.example` documenta estas variables con valores de ejemplo. Para usarlas, copia `.env.example` como `.env` (ya está en `.gitignore`, no se sube al repositorio) y completa los valores reales.

**El proyecto carga `.env` automáticamente al arrancar** (ver `CmedApplication.main`, con la librería `dotenv-java`): no hace falta exportar nada a mano ni configurar variables de entorno en el IDE. Solo hace falta que el archivo `.env` exista en la raíz de `cmed/` (junto a `pom.xml`) antes de correr la aplicación. Si además defines la variable en el sistema operativo (por ejemplo desde NetBeans o PowerShell), esa tiene prioridad sobre `.env`:

- **NetBeans:** en las propiedades del proyecto, sección "Run" → "Environment Variables (Variables de entorno)", agregar cada variable con su valor.
- **PowerShell:** `$env:DB_PASSWORD = "la_Clave"` antes de correr `mvn spring-boot:run`.
- **Terminal Unix/Git Bash:** `export DB_PASSWORD=la_Clave` antes de correr `mvn spring-boot:run`, o anteponer las variables al comando: `DB_PASSWORD=la_Clave mvn spring-boot:run`.

Si no se define ninguna variable, la aplicación funciona igual usando los valores por defecto de desarrollo local.

Usuarios de prueba (la contraseña de los cuatro está protegida con BCrypt):

| Rol | Correo | Contraseña |
| --- | --- | --- |
| Administrador | `admin@citamed.com` | `123` |
| Paciente | `paciente@citamed.com` | `123` |
| Doctor | `doctor1@citamed.com` | `123` |
| Doctor | `doctor2@citamed.com` | `123` |

## Seguridad

El control de acceso se implementa con Spring Security (`SecurityConfig`), siguiendo el mismo enfoque de `SecurityFilterChain` visto en el curso:

- Autenticación por formulario (`/login`) contra un `UserDetailsService` propio (`UsuarioDetailsService`) que valida el correo y el estado `activo` del usuario.
- Contraseñas protegidas con `BCryptPasswordEncoder` (antes se usaba un resumen SHA-256 manual).
- Autorización por prefijo de ruta: `/admin/**` requiere `ROLE_ADMIN`, `/doctor/**` requiere `ROLE_DOCTOR`, `/paciente/**` requiere `ROLE_PACIENTE`.
- Ocultamiento de menús por rol en las plantillas mediante `sec:authorize`.
- Cierre de sesión mediante formulario POST a `/logout`, protegido con CSRF.
- Página de acceso denegado propia (`/acceso-denegado`) para intentos de acceso a rutas fuera del rol.

## Base de datos

8 tablas: `usuario`, `especialidad`, `paciente`, `doctor`, `horario`, `horario_bloqueado`, `cita` (tabla transaccional principal) y `notificacion` (registro de los correos enviados por el sistema).

## Estado del proyecto: Entrega final

Las 20 historias de usuario definidas en el Avance 1 están implementadas.

### Historias implementadas

| ID | Funcionalidad |
| --- | --- |
| HU-01 | Registro de médicos con especialidad y horario. |
| HU-02 | Registro, edición, búsqueda y desactivación de pacientes. |
| HU-03 | Registro público y almacenamiento protegido de contraseña (BCrypt). |
| HU-04 | Inicio de sesión del paciente y restricción por rol (Spring Security). |
| HU-05 | Reserva con especialidad, médico, disponibilidad y confirmación. |
| HU-06 | Agenda diaria y semanal del doctor. |
| HU-07 | Consulta administrativa de citas con filtros. |
| HU-08 | Cancelación de citas propias con más de 24 horas. |
| HU-09 | Confirmación y rechazo de citas con motivo, con notificación por correo. |
| HU-10 | Inicio de sesión administrativo y restricción de rutas con Spring Security. |
| HU-11 | Cierre de cita y registro de nota médica. |
| HU-12 | Historial de citas del paciente separado de las citas próximas. |
| HU-13 | Especialidades: alta, edición y eliminación controlada (bloqueada si tiene médicos asignados). |
| HU-14 | Reporte de citas por rango y estado. |
| HU-15 | Bloqueo de horarios del doctor. |
| HU-16 | Médicos y horarios disponibles por especialidad. |
| HU-17 | Reprogramación administrativa de citas, con notificación por correo. |
| HU-18 | Doctor consulta historial de un paciente, con búsqueda por nombre o cédula. |
| HU-19 | Paciente edita nombre, correo, teléfono y dirección; el cambio de correo exige confirmar la contraseña actual. |
| HU-20 | Activación y desactivación de médicos. |

## Investigación adicional

CitaMed incorpora dos tecnologías no desarrolladas directamente en clase, ninguna de las cuales forma parte de `Tienda_j`, la referencia técnica del curso.

**Spring Mail.** Notifica automáticamente al paciente cuando su cita es confirmada, rechazada, cancelada o reprogramada. Cada intento de envío (exitoso o fallido) se registra en la tabla `notificacion`, visible para el administrador en `/admin/notificaciones`. El correo forma parte real del ciclo de vida de la cita y su historial es auditable desde la aplicación.

**OpenPDF (`com.github.librepdf:openpdf`).** Genera en el servidor un documento PDF descargable del reporte de citas por rango de fechas (`GET /admin/reportes/pdf`), con el detalle de paciente, médico, fecha, hora, estado y motivo de cada cita del rango consultado. El botón "Exportar a PDF" está disponible en `/admin/reportes` junto al filtro de fechas ya existente (HU-14), y respeta el idioma de la sesión activa.

## Internacionalización

La aplicación soporta español e inglés mediante `messages_es.properties` y `messages_en.properties`, con un selector de idioma disponible en la pantalla de login y en el panel de cada rol (`?lang=es` / `?lang=en`).

## Acuerdo de trabajo

- `main` representa la versión estable.
- Cada funcionalidad se desarrolla en una rama `feature/nombre-funcionalidad`.
- Las correcciones se realizan en ramas `fix/descripcion-correccion`.
- Todo cambio hacia `main` requiere un pull request revisado por otro integrante.

## Estado general

- Avance 1: planteamiento, backlog, prototipo y modelo preliminar completados.
- Avance 2: aplicación funcional, persistencia, navegación por roles y vistas Bootstrap implementadas.
- Entrega final: Spring Security, historias de usuario al 100%, 8va tabla, Spring Mail e internacionalización funcional completados.

## Licencia

Licencia MIT, ver [LICENSE](LICENSE).
