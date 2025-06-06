package com.app.controller.Aster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;
import java.sql.*;
import java.time.LocalDate;
import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;
import com.app.model.Aster.Guardia;

public class ConsultaGuardiasController {

    @FXML private DatePicker datePicker;
    @FXML private TableView<Guardia> tableView;
    @FXML private TableColumn<Guardia, String> colDocente;
    @FXML private TableColumn<Guardia, String> colGrupo;
    @FXML private TableColumn<Guardia, String> colMateria;
    @FXML private TableColumn<Guardia, String> colAula;
    @FXML private TableColumn<Guardia, String> colHora;
    @FXML private TableColumn<Guardia, Void> colAccion;

    private String idDocenteActual;

    public void setUsuario(String idDocent) {
        this.idDocenteActual = idDocent;
    }

    @FXML
    void buscarGuardias() {
        LocalDate fechaSeleccionada = datePicker.getValue();
        if (fechaSeleccionada == null) {
            Alertas.showWarningAlert("Selecciona una fecha para continuar.");
            return;
        }

        ObservableList<Guardia> guardias = FXCollections.observableArrayList();

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = """
                SELECT 
                    ha.id_horari,
                    hg.grup,
                    hg.contingut,
                    hg.aula,
                    hg.hora_desde,
                    hg.hora_fins,
                    d.nom,
                    d.id_docent,
                    ha.id_absent
                FROM docents_absents ha
                JOIN horari_grup hg ON ha.id_horari = hg.id_horari
                JOIN docent d ON ha.id_docent = d.id_docent
                WHERE ha.data_absencia = ?
                  AND ha.id_horari NOT IN (
                      SELECT id_horari FROM guardies WHERE data = ?
                  )
            """;

            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setDate(1, Date.valueOf(fechaSeleccionada));
            stmt.setDate(2, Date.valueOf(fechaSeleccionada));

            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int idHorari = rs.getInt("id_horari");
                String grup = rs.getString("grup");
                String materia = rs.getString("contingut");
                String aula = rs.getString("aula");
                String hora = rs.getString("hora_desde") + " - " + rs.getString("hora_fins");
                String profeAbsent = rs.getString("nom");
                String idDocentAbsent = rs.getString("id_docent");
                int idAbsent = rs.getInt("id_absent");

                guardias.add(new Guardia(idHorari, grup, materia, aula, hora, profeAbsent, idDocentAbsent, idAbsent));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar las guardias.");
        }

        tableView.setItems(guardias);
    }

    @FXML
    public void initialize() {
        colDocente.setCellValueFactory(new PropertyValueFactory<>("docentAbsent"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grup"));
        colMateria.setCellValueFactory(new PropertyValueFactory<>("materia"));
        colAula.setCellValueFactory(new PropertyValueFactory<>("aula"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));

        colAccion.setCellFactory(getBotonesAccion());
    }

    @SuppressWarnings("unused")
    private Callback<TableColumn<Guardia, Void>, TableCell<Guardia, Void>> getBotonesAccion() {
        return param -> new TableCell<>() {
            private final Button btn = new Button("Hacer Guardia");

            {
                btn.setOnAction(event -> {
                    Guardia g = getTableView().getItems().get(getIndex());
                    registrarGuardia(g);
                });
                btn.setStyle("-fx-background-color: #6d6da0; -fx-text-fill: white;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        };
    }

    private void registrarGuardia(Guardia g) {
        try (Connection conn = DatabaseConnector.connect()) {
            String obtenerNom = "SELECT nom FROM docent WHERE id_docent = ?";
            PreparedStatement stmtNom = conn.prepareStatement(obtenerNom);
            stmtNom.setString(1, idDocenteActual);
            ResultSet rsNom = stmtNom.executeQuery();
            String nomDocenteGuardia = "";
            if (rsNom.next()) {
                nomDocenteGuardia = rsNom.getString("nom");
            }

            String sql = """
                INSERT INTO guardies
                  (id_docent_guardia, nom_docent_guardia, id_horari, id_absent, data,
                   hora_desde, hora_fins, grup, contingut, aula)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            int idx = 1;
            stmt.setString(idx++, idDocenteActual);
            stmt.setString(idx++, nomDocenteGuardia);
            stmt.setInt(idx++, g.getIdHorari());
            stmt.setInt(idx++, g.getIdAbsent());
            stmt.setDate(idx++, Date.valueOf(datePicker.getValue()));
            stmt.setString(idx++, g.getHoraDesde());
            stmt.setString(idx++, g.getHoraFins());
            stmt.setString(idx++, g.getGrup());
            stmt.setString(idx++, g.getMateria());
            stmt.setString(idx++, g.getAula());

            stmt.executeUpdate();

            Alertas.showSuccessAlert("Guardia registrada correctament.");
            buscarGuardias();
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al registrar la guàrdia.");
        }
    }
    
    @FXML
    private void limpiar() {
        datePicker.setValue(null);     /* Limpia el DatePicker */
        tableView.getItems().clear();  /* Limpia los resultados de la tabla */
    }

}