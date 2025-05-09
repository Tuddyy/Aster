# Aster
# Sistema de gestión de asistencia y guardias para instituciones educativas, desarrollado en JavaFX.

Aplicación de escritorio diseñada para centros educativos que permite gestionar la asistencia del profesorado y las guardias ante ausencias. Esta herramienta busca facilitar el cumplimiento de la jornada laboral y la cobertura de clases por parte del profesorado de guardia.

## Funcionalidades principales

- Control de permisos según el tipo de usuario (administrador o general).
- Gestión y almacenamiento de guardias y faltas realizadas.
- Generación de informes sobre faltas y guardias.

## Módulos de la aplicación

- **Login:** Inicio de sesión con DNI y contraseña.
- **Módulo Principal:** Acceso al resto de módulos y botones para iniciar/finalizar jornada.
- **Asistencia:** Consulta de asistencia por docente, fecha o mes (solo administradores).
- **Profesorat absent:** Alta de ausencias en la base de datos (solo administradores).
- **Informes:** Informe de faltas por día, semana, mes, trimestre, curso o docente (solo administradores).
- **Consulta de absències:** Para que el profesorado de guardia consulte ausencias y cubra clases.
- **Consulta de guàrdies realitzades:** Muestra quién cubrió qué guardia, con fecha y hora.

## Tecnologías utilizadas

- Java
- JavaFX
- SceneBuilder
- FXML
- MySQL
- JDBC

## Instalación

1. Clona este repositorio:
   ```bash
   git clone https://github.com/Tuddyy/Aster.git
