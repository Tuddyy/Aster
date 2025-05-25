![Image](https://github.com/user-attachments/assets/5ae293d3-f729-43b8-87ef-67b4e4dbb179)
# ASTER
**Gestión moderna de asistencia y guardias para centros educativos**

---

## 🧠 Acerca del proyecto

**Aster** es una aplicación de escritorio multiplataforma (Java + JavaFX) diseñada para facilitar la **gestión de asistencia del profesorado** y **la organización de guardias en centros educativos.** Surge como una solución adaptada específicamente al entorno escolar, teniendo en cuenta necesidades reales como la codocencia, las franjas horarias y la asignación rápida de sustituciones ante ausencias imprevistas.


### ⚡ Características principales

- Registro digital de entrada y salida
- Alta y consulta de ausencias
- Generación de informes (diario/semanal/trimestral)
- Asignación de guardias
- Vista de guardias realizadas
- Roles diferenciados: docente vs administrador

---

## 🛠️ Tecnologías utilizadas

- `Java`
- `JavaFX` + `FXML` + `CSS`
- `MySQL` con `phpMyAdmin` (vía XAMPP)
- `Spring Tools for Eclipse` + `SceneBuilder`

---

## 🚀 Instalación

### ⚙️ Requisitos previos

- Java 11 o superior
- XAMPP con MySQL y phpMyAdmin
- Sistema operativo compatible (Windows / Linux / MacOS)
- Eclipse + JavaFX configurado

### 💾 Pasos

1. Clona el repositorio
   ```bash
   git clone https://github.com/Tuddyy/Aster.git
2. Abre el proyecto en Spring Tools for Eclipse
   - Ve a `File > Open Projects from File System...`
   - Navega hasta la carpeta del proyecto clonado y ábrela.

3. Importa la base de datos `aster_guardias.sql` en PhpMyAdmin
   - Inicia XAMPP, dale start en los modulos **Apache** y **MySQL** y luego dale click a **Admin** en el modulo de **MySQL**.
   - Crea una nueba base de datos
   - Usa la opción **Importar** y selecciona el archivo `aster_guardias.sql`

4. Configura tu archivo de conexión **(si aplica)**
   - Asegúrate de que el archivo Java que conecta a MySQL tenga estos datos:
     ```bash
     String url = "jdbc:mysql://localhost:3306/aster";
     String user = "root";
     String password = "";
5. Ejecuta la clase principal desde Spring Tools
   - Busca el archivo `Main.java` en el paquete `com.app.main.Aster` situado en `src/main/java`
   - Haz click derecho `> Run As > Java Application`
