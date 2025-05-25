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
import java.io.IOException;
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
    @FXML private Tab tabAbsencies;
    @FXML private Tab tabGuardies;
    @FXML private Tab tabInformeFaltas;
    @FXML private Tab tabConsultaGuardiesRealitzades;
    
    private String idDocent;
    private String rolUsuario;
    
    public void initialize() {
        // Configurar listener para cargar pestañas cuando se seleccionan
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

    public void setUsuario(String idDocent, String rol) {
        this.idDocent = idDocent;
        this.rolUsuario = rol;
        
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(
                 "SELECT usuario FROM login WHERE id_docent = ?")) {
            
            stmt.setString(1, idDocent);
            var rs = stmt.executeQuery();
            
            if (rs.next()) {
                String dni = rs.getString("usuario");
                bienvenidaText.setText("Bienvenido, " + dni + ("admin".equalsIgnoreCase(rol) ? " (Administrador)" : ""));
                
                // Configurar pestañas según permisos
                configurarPestanasPorRol(rol);
                
                // Cargar la pestaña inicial
                cargarPestanaInicial();
            }
        } catch (Exception e) {
            e.printStackTrace();
            bienvenidaText.setText("Bienvenido");
        }
    }

    private void configurarPestanasPorRol(String rol) {
        if (!"admin".equalsIgnoreCase(rol)) {
            // Para usuarios no administradores
            tabPane.getTabs().removeAll(tabAsistencia, tabAbsencies, tabInformeFaltas);
            tabPane.getSelectionModel().select(tabGuardies);
        } else {
            // Para administradores
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
            } else if (pestana == tabAbsencies) {
                cargarControladorAbsencies();
            } else if (pestana == tabGuardies) {
                cargarControladorGuardies();
            } else if (pestana == tabInformeFaltas) {
                cargarControladorInformeFaltas();
            } else if (pestana == tabConsultaGuardiesRealitzades) {
                cargarControladorGuardiesRealitzades();
            }
        } catch (Exception e) {
            e.printStackTrace();
            pestana.setDisable(true);
        }
    }

    private void cargarControladorAsistencia() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaAssistencia.fxml"));
        Parent content = loader.load();
        ConsultaAssistenciaController controller = loader.getController();
        controller.setRolUsuari(rolUsuario);
        tabAsistencia.setContent(content);
    }

    private void cargarControladorAbsencies() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/IntroduirAbsencies.fxml"));
        Parent content = loader.load();
        tabAbsencies.setContent(content);
    }

    private void cargarControladorGuardies() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaGuardies.fxml"));
        Parent content = loader.load();
        ConsultaGuardiesController ctrl = loader.getController();
        ctrl.setUsuario(idDocent); 
        tabGuardies.setContent(content);
    }

    private void cargarControladorInformeFaltas() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/InformeFaltas.fxml"));
        Parent content = loader.load();
        tabInformeFaltas.setContent(content);
    }

    private void cargarControladorGuardiesRealitzades() throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/ConsultaGuardiesRealitzades.fxml"));
        Parent content = loader.load();
        ConsultaGuardiesRealitzadesController ctrl = loader.getController();
        ctrl.setUsuario(idDocent);
        tabConsultaGuardiesRealitzades.setContent(content);
    }

    @FXML
    void iniciarJornada() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "INSERT INTO registro_jornada (id_docent, fecha, hora_entrada) VALUES (?, ?, ?)";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idDocent);
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
            stmt.setString(2, idDocent);
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
            out.println(tipo + " | Usuario: " + idDocent + " | Fecha y hora: " + ahora);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}