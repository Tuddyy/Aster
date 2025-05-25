package com.app.controller.Aster;

import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class ConsultaAssistenciaController {

    @FXML private ComboBox<String> comboDocents;
    @FXML private DatePicker datePicker;
    @FXML private Button btnBuscar;
    @FXML private TableView<String> tableResultats;
    @FXML private TableColumn<String, String> colResultat;

    public void setRolUsuari(String rol) {
        if (!"admin".equalsIgnoreCase(rol)) {
            Alertas.showErrorAlert("No tens permisos per accedir a aquest mòdul.");
            desactivarMòdul();
        }
    }

    @FXML
    public void initialize() {
        carregarDocents();
        colResultat.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
    }

    private void desactivarMòdul() {
        comboDocents.setDisable(true);
        datePicker.setDisable(true);
        btnBuscar.setDisable(true);
    }

    private void carregarDocents() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT id_docent, nom FROM docent";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> docents = FXCollections.observableArrayList();
            while (rs.next()) {
                docents.add(rs.getString("nom") + " - " + rs.getString("id_docent"));
            }
            comboDocents.setItems(docents);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void buscarAssistencia() {
        String seleccionado = comboDocents.getValue();
        LocalDate data = datePicker.getValue();

        if (data == null) {
            Alertas.showWarningAlert("Selecciona una data per continuar.");
            return;
        }

        ObservableList<String> resultats = FXCollections.observableArrayList();
        String diaLletra = convertirADiaLletra(data);

        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement stmt;

            if (seleccionado == null) {
                // Consulta para admin (todos los docentes)
                stmt = conn.prepareStatement("""
                    SELECT d.nom AS nom_docent, hg.hora_desde, hg.hora_fins
                    FROM docent d
                    JOIN horari_grup hg ON (d.id_docent = hg.id_docent1 OR d.id_docent = hg.id_docent2)
                    WHERE hg.dia_setmana = ?
                    AND NOT EXISTS (
                        SELECT 1 FROM docents_absents da 
                        WHERE da.id_docent = d.id_docent 
                        AND da.data_absencia = ?
                        AND da.id_horari = hg.id_horari
                    )
                    ORDER BY d.nom, hg.hora_desde
                """);
                stmt.setString(1, diaLletra);
                stmt.setDate(2, Date.valueOf(data));
            } else {
                // Consulta para un docente específico
                String idDocent = seleccionado.split(" - ")[1];
                stmt = conn.prepareStatement("""
                    SELECT hg.hora_desde, hg.hora_fins, hg.contingut, hg.grup
                    FROM horari_grup hg
                    WHERE (hg.id_docent1 = ? OR hg.id_docent2 = ?)
                    AND hg.dia_setmana = ?
                    AND NOT EXISTS (
                        SELECT 1 FROM docents_absents da 
                        WHERE da.id_docent = ?
                        AND da.data_absencia = ?
                        AND da.id_horari = hg.id_horari
                    )
                    ORDER BY hg.hora_desde
                """);
                stmt.setString(1, idDocent);
                stmt.setString(2, idDocent);
                stmt.setString(3, diaLletra);
                stmt.setString(4, idDocent);
                stmt.setDate(5, Date.valueOf(data));
            }

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                String info;
                if (seleccionado == null) {
                    info = rs.getString("nom_docent") + ": " +
                           rs.getString("hora_desde") + " - " +
                           rs.getString("hora_fins");
                } else {
                    info = rs.getString("hora_desde") + " - " +
                           rs.getString("hora_fins") + " (" +
                           rs.getString("contingut") + " " +
                           rs.getString("grup") + ")";
                }
                resultats.add(info);
            }

            tableResultats.setItems(resultats);
            
            if (resultats.isEmpty()) {
                Alertas.showInfoAlert("No s'han trobat hores de assistència per aquest dia.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al carregar les dades.");
        }
    }

    private String convertirADiaLletra(LocalDate data) {
        return switch (data.getDayOfWeek()) {
            case MONDAY -> "L";
            case TUESDAY -> "M";
            case WEDNESDAY -> "X";
            case THURSDAY -> "J";
            case FRIDAY -> "V";
            default -> "";
        };
    }
    
    @FXML
    void limpiarCampos() {
        // Limpiar ComboBox
        comboDocents.getSelectionModel().clearSelection();
        
        // Limpiar DatePicker
        datePicker.setValue(null);
        
        // Limpiar la tabla de resultados
        tableResultats.getItems().clear();
    }

}