package com.app.controller.Aster;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.TabPane;
import javafx.scene.control.Tab;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;

public class AplicacionController {

    @FXML private Text bienvenidaText;
    @FXML private TabPane tabPane;
    @FXML private Tab tabAsistencia;
    @FXML private Tab tabAusencias;
    @FXML private Tab tabGuardias;
    @FXML private Tab tabInformeFaltas;
    @FXML private Tab tabConsultaGuardiasRealizadas;
    
    private String idDocente;
    private String rolUsuario;
    
    public void initialize() {
        /* Listener para cargar las pestañas cuando se seleccionan */
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null && newTab.getContent() == null) {
                cargarPestana(newTab);
            }
        });

        Platform.runLater(() -> {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            stage.setMinWidth(905);  
            stage.setMinHeight(700);
            stage.setWidth(905);
            stage.setHeight(700);
        });
    }

    /**
    * Establece el usuario actual que ha iniciado sesión, configurando el rol,
    * el mensaje de bienvenida y las pestañas visibles según su permiso.
    *
    * @param idDocent ID del docente que ha iniciado sesión
    * @param rol Rol del usuario (por ejemplo, "Administrador" o "Usuario")
    */
    public void setUsuario(String idDocent, String rol) {
        this.idDocente = idDocent;
        this.rolUsuario = rol;
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT usuario FROM login WHERE id_docent = ?")) {
            
            stmt.setString(1, idDocent);
            var rs = stmt.executeQuery();
            
            if (rs.next()) {
                String dni = rs.getString("usuario");
                bienvenidaText.setText("Bienvenido, " + dni + ("admin".equalsIgnoreCase(rol) ? " (Administrador)" : ""));
                
                configurarPestanasPorRol(rol);
                cargarPestanaInicial();
            }
        } catch (Exception e) {
            e.printStackTrace();
            bienvenidaText.setText("Bienvenido");
        }
    }

    private void configurarPestanasPorRol(String rol) {
        if (!"admin".equalsIgnoreCase(rol)) {
        	
            /* Para usuarios no administradores */
            tabPane.getTabs().removeAll(tabAsistencia, tabAusencias, tabInformeFaltas);
            tabPane.getSelectionModel().select(tabGuardias);
        } else {
        	
            /* Para administradores */
            tabPane.getSelectionModel().select(tabAsistencia);
        }
    }

    private void cargarPestanaInicial() {
        Tab selectedTab = tabPane.getSelectionModel().getSelectedItem();
        if (selectedTab != null) {
            cargarPestana(selectedTab);
        }
    }
    
    private void cargarPestana(Tab pestana) {
        try {
            if (pestana == tabAsistencia) {
                cargarControladorAsistencia();
            } else if (pestana == tabAusencias) {
                cargarControladorAusencias();
            } else if (pestana == tabGuardias) {
                cargarControladorGuardias();
            } else if (pestana == tabInformeFaltas) {
                cargarControladorInformeFaltas();
            } else if (pestana == tabConsultaGuardiasRealizadas) {
                cargarControladorGuardiasRealizadas();
            }
        } catch (Exception e) {
            e.printStackTrace();
            pestana.setDisable(true);
        }
    }

    private void cargarControladorAsistencia() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaAsistencia.fxml"));
        Parent content = loader.load();
        ConsultaAsistenciaController controller = loader.getController();
        controller.setRolUsuario(rolUsuario);
        tabAsistencia.setContent(content);
    }

    private void cargarControladorAusencias() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/IntroducirAusencias.fxml"));
        Parent content = loader.load();
        tabAusencias.setContent(content);
    }

    private void cargarControladorGuardias() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaGuardias.fxml"));
        Parent content = loader.load();
        ConsultaGuardiasController ctrl = loader.getController();
        ctrl.setUsuario(idDocente); 
        tabGuardias.setContent(content);
    }

    private void cargarControladorInformeFaltas() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InformeFaltas.fxml"));
        Parent content = loader.load();
        tabInformeFaltas.setContent(content);
    }

    private void cargarControladorGuardiasRealizadas() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaGuardiasRealizadas.fxml"));
        Parent content = loader.load();
        ConsultaGuardiasRealizadasController ctrl = loader.getController();
        ctrl.setUsuario(idDocente);
        tabConsultaGuardiasRealizadas.setContent(content);
    }

    @FXML
    void iniciarJornada() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "INSERT INTO registro_jornada (id_docent, fecha, hora_entrada) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idDocente);
            stmt.setDate(2, java.sql.Date.valueOf(LocalDate.now()));
            stmt.setTimestamp(3, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.executeUpdate();

            registrarEnLog("INICIO");
            Alertas.showSuccessAlert("Jornada iniciada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al iniciar la jornada.");
        }
    }

    @FXML
    void finalizarJornada() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "UPDATE registro_jornada SET hora_salida = ? WHERE id_docent = ? AND fecha = ? AND hora_salida IS NULL";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setTimestamp(1, java.sql.Timestamp.valueOf(LocalDateTime.now()));
            stmt.setString(2, idDocente);
            stmt.setDate(3, java.sql.Date.valueOf(LocalDate.now()));
            stmt.executeUpdate();

            registrarEnLog("FINAL");
            Alertas.showSuccessAlert("Jornada finalizada correctamente.");
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al finalizar la jornada.");
        }
    }

    @FXML
    void cerrarSesion() {
        try {
            Stage stage = (Stage) tabPane.getScene().getWindow();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Login.fxml"));
            Parent root = loader.load();
            Scene scene = new Scene(root);
            scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
            stage.setScene(scene);
            stage.setWidth(700);  
            stage.setHeight(500); 
            stage.setMinWidth(700);  
            stage.setMinHeight(500);
            stage.show();
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cerrar sesión.");
        }
    }

    private void registrarEnLog(String tipo) {
        try (PrintWriter out = new PrintWriter(new FileWriter("registro_log.txt", true))) {
            String ahora = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            out.println(tipo + " | Usuario: " + idDocente + " | Fecha y hora: " + ahora);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}