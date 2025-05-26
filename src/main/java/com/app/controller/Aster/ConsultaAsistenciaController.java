package com.app.controller.Aster;

import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

public class ConsultaAsistenciaController {

    @FXML private ComboBox<String> comboDocentes;
    @FXML private DatePicker datePicker;
    @FXML private Button btnBuscar;
    @FXML private TableView<String> tableResultados;
    @FXML private TableColumn<String, String> colResultado;

    public void setRolUsuario(String rol) {
        if (!"admin".equalsIgnoreCase(rol)) {
            Alertas.showErrorAlert("No tienes permisos para acceder a este modulo.");
            desactivarModulo();
        }
    }

    @FXML
    public void initialize() {
        cargarDocentes();
        colResultado.setCellValueFactory(data -> new javafx.beans.property.SimpleStringProperty(data.getValue()));
    }

    private void desactivarModulo() {
        comboDocentes.setDisable(true);
        datePicker.setDisable(true);
        btnBuscar.setDisable(true);
    }

    private void cargarDocentes() {
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT id_docent, nom FROM docent";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();

            ObservableList<String> docentes = FXCollections.observableArrayList();
            while (rs.next()) {
                docentes.add(rs.getString("nom") + " - " + rs.getString("id_docent"));
            }
            comboDocentes.setItems(docentes);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void buscarAsistencia() {
        String seleccionado = comboDocentes.getValue();
        LocalDate fecha = datePicker.getValue();

        if (fecha == null) {
            Alertas.showWarningAlert("Selecciona una fecha para continuar.");
            return;
        }

        ObservableList<String> resultados = FXCollections.observableArrayList();
        String diaLetra = convertirADiaLetra(fecha);

        try (Connection conn = DatabaseConnector.connect()) {
            PreparedStatement stmt;

            if (seleccionado == null) {
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
                stmt.setString(1, diaLetra);
                stmt.setDate(2, Date.valueOf(fecha));
            } else {
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
                stmt.setString(3, diaLetra);
                stmt.setString(4, idDocent);
                stmt.setDate(5, Date.valueOf(fecha));
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
                resultados.add(info);
            }

            tableResultados.setItems(resultados);
            
            if (resultados.isEmpty()) {
                Alertas.showInfoAlert("No se han encontrado horas de asistencia para este dia.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar las fechas.");
        }
    }

    private String convertirADiaLetra(LocalDate data) {
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
    	
    	 /* Limpia el ComboBox */
        comboDocentes.getSelectionModel().clearSelection();
        
        /* Limpia el DatePicker */
        datePicker.setValue(null);
        
        /* Limpia la tabla de resultados */
        tableResultados.getItems().clear();
    }

}