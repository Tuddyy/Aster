package com.app.controller.Aster;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import java.sql.*;
import java.sql.Date;
import java.time.LocalDate;
import java.util.*;

import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;

public class ConsultaGuardiesRealitzadesController {

    @FXML private DatePicker fechaPicker;
    @FXML private ComboBox<String> horaCombo;
    @FXML private Button buscarBtn;
    @FXML private ListView<String> tablaGuardies;
    
    private String idDocent;
    
    public void setUsuario(String idDocent) {
        this.idDocent = idDocent;
    }

    private Map<String, String> horaTextoToHoraDesdeMap = new HashMap<>();

    @FXML
    public void initialize() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT DISTINCT hora_desde, hora_fins FROM horari_grup ORDER BY hora_desde";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> horas = FXCollections.observableArrayList();

            while (rs.next()) {
                String desde = rs.getString("hora_desde");
                String fins = rs.getString("hora_fins");
                String texto = desde + " – " + fins;

                horas.add(texto);
                horaTextoToHoraDesdeMap.put(texto, desde);
            }

            horaCombo.setItems(horas);

        } catch (SQLException e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar las horas disponibles.");
        }
    }

    @FXML
    void buscarGuardies() {
        LocalDate fecha = fechaPicker.getValue();
        String seleccionado = horaCombo.getValue();

        if (fecha == null || seleccionado == null) {
            Alertas.showWarningAlert("Por favor, selecciona una fecha y hora.");
            return;
        }

        String horaDesde = horaTextoToHoraDesdeMap.get(seleccionado);
        if (horaDesde == null) {
            Alertas.showErrorAlert("Hora no válida.");
            return;
        }
        
        try (Connection conn = DatabaseConnector.connect()) {
            String sql =
                "SELECT id_docent_guardia, nom_docent_guardia, grup, contingut, aula " +
                "FROM guardies " +
                "WHERE data = ? AND hora_desde = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setDate(1, Date.valueOf(fecha));
            pst.setString(2, horaDesde);

            ResultSet rs = pst.executeQuery();
            ObservableList<String> guardias = FXCollections.observableArrayList();

            while (rs.next()) {
                String dni = rs.getString("id_docent_guardia");
                String docente = rs.getString("nom_docent_guardia");
                String grupo = rs.getString("grup");
                String materia = rs.getString("contingut");
                String aula = rs.getString("aula");

                guardias.add(
                    "DNI: " + dni +
                    ", Docente: " + docente +
                    ", Grupo: " + grupo +
                    ", Materia: " + materia +
                    ", Aula: " + aula
                );
            }

            if (guardias.isEmpty()) {
                Alertas.showInfoAlert("No hay guardias realizadas para esa fecha y hora.");
            }

            tablaGuardies.setItems(guardias);

        } catch (SQLException e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al consultar las guardias.");
        }
    }
    
    @FXML
    void limpiarCampos() {
        // Limpiar los campos de entrada
        fechaPicker.setValue(null);
        horaCombo.getSelectionModel().clearSelection();

        // Limpiar la lista de resultados
        tablaGuardies.setItems(FXCollections.observableArrayList());
    }

}