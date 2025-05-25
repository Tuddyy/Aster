package com.app.controller.Aster;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import java.sql.*;
import java.time.LocalDate;

import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;

public class IntroduirAbsenciesController {

    @FXML private ComboBox<String> comboDocents;
    @FXML private DatePicker datePicker;
    @FXML private TextField motiuField;
    @FXML private ListView<String> listViewHores;

    private final ObservableList<String> horesList = FXCollections.observableArrayList();
    private final ObservableList<Integer> horesIds = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        cargarDocents(); // Cargar docentes al inicio
        listViewHores.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // Manejamos selección personalizada de índices
        ObservableList<Integer> selectedIndices = FXCollections.observableArrayList();
        listViewHores.getProperties().put("customSelectedIndices", selectedIndices);

        // Pintamos de rojo las celdas seleccionadas
        listViewHores.setCellFactory(event -> {
            ListCell<String> cell = new ListCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setStyle("");
                    } else {
                        setText(item);
                        if (selectedIndices.contains(getIndex())) {
                            setStyle("-fx-background-color: #ffcccc;");
                        } else {
                            setStyle("");
                        }
                    }
                }
            };

            cell.setOnMousePressed(e -> {
                if (!cell.isEmpty()) {
                    int index = cell.getIndex();
                    if (selectedIndices.contains(index)) {
                        selectedIndices.remove((Integer) index);
                    } else {
                        selectedIndices.add(index);
                    }
                    listViewHores.getSelectionModel().clearSelection(); // evitar selección por defecto
                    listViewHores.refresh(); // aplicar estilo visual
                }
            });

            return cell;
        });

        // 🔁 Añadimos listeners para actualizar las horas automáticamente
        comboDocents.setOnAction(e -> cargarHores());
        datePicker.setOnAction(e -> cargarHores());
    }

    private void cargarDocents() {
        ObservableList<String> docents = FXCollections.observableArrayList();
        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT id_docent, nom FROM docent";
            PreparedStatement stmt = conn.prepareStatement(sql);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                docents.add(rs.getString("nom") + " - " + rs.getString("id_docent"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar los docentes.");
        }
        comboDocents.setItems(docents);
    }

    private void cargarHores() {
        horesList.clear();
        horesIds.clear();

        // ⚠️ Limpiar selección personalizada al recargar
        @SuppressWarnings("unchecked")
        ObservableList<Integer> selectedIndices = (ObservableList<Integer>) listViewHores.getProperties().get("customSelectedIndices");
        if (selectedIndices != null) {
            selectedIndices.clear();
        }

        String seleccionado = comboDocents.getValue();
        LocalDate fecha = datePicker.getValue();
        if (seleccionado == null || fecha == null) return;

        String idDocent = seleccionado.split(" - ")[1];
        String diaLletra = convertirADiaLletra(fecha);

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = """
                SELECT id_horari, hora_desde, hora_fins, grup, contingut
                FROM horari_grup
                WHERE id_docent1 = ? AND dia_setmana = ?
            """;
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, idDocent);
            stmt.setString(2, diaLletra);
            ResultSet rs = stmt.executeQuery();

            while (rs.next()) {
                int idHorari = rs.getInt("id_horari");
                String entrada = rs.getString("hora_desde");
                String sortida = rs.getString("hora_fins");
                String grup = rs.getString("grup");
                String materia = rs.getString("contingut");

                horesList.add(entrada + " - " + sortida + " (" + materia + " " + grup + ")");
                horesIds.add(idHorari);
            }

            listViewHores.setItems(horesList);
            listViewHores.refresh(); //  refrescar para aplicar estilos tras limpieza
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar las horas.");
        }
    }

    @FXML
    void guardarAbsencia() {
        String seleccionado = comboDocents.getValue();
        if (seleccionado == null || datePicker.getValue() == null || motiuField.getText().isBlank()) {
            Alertas.showWarningAlert("Completa todos los campos.");
            return;
        }

        String idDocent = seleccionado.split(" - ")[1];
        LocalDate fecha = datePicker.getValue();
        String motiu = motiuField.getText();
        @SuppressWarnings("unchecked")
        ObservableList<Integer> seleccionats = (ObservableList<Integer>) listViewHores.getProperties().get("customSelectedIndices");

        if (seleccionats.isEmpty()) {
            Alertas.showWarningAlert("Selecciona al menos una hora de ausencia.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            // Primero eliminamos cualquier ausencia previa para este docente en esta fecha
            String sqlDelete = "DELETE FROM docents_absents WHERE id_docent = ? AND data_absencia = ?";
            PreparedStatement stmtDelete = conn.prepareStatement(sqlDelete);
            stmtDelete.setString(1, idDocent);
            stmtDelete.setDate(2, Date.valueOf(fecha));
            stmtDelete.executeUpdate();

            // Luego insertamos solo las horas seleccionadas como ausencias
            String sqlInsert = "INSERT INTO docents_absents (id_docent, data_absencia, motiu, id_horari) VALUES (?, ?, ?, ?)";
            PreparedStatement stmtInsert = conn.prepareStatement(sqlInsert);

            for (Integer index : seleccionats) {
                if (index >= 0 && index < horesIds.size()) {
                    stmtInsert.setString(1, idDocent);
                    stmtInsert.setDate(2, Date.valueOf(fecha));
                    stmtInsert.setString(3, motiu);
                    stmtInsert.setInt(4, horesIds.get(index));
                    stmtInsert.addBatch();
                }
            }

            stmtInsert.executeBatch();
            Alertas.showSuccessAlert("Absència registrada correctament.");
            listViewHores.getSelectionModel().clearSelection();
            cargarHores(); // refrescar la lista
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al guardar la absència.");
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
        // Limpiar campos de entrada
        comboDocents.getSelectionModel().clearSelection();
        datePicker.setValue(null);
        motiuField.clear();
        listViewHores.getSelectionModel().clearSelection(); // Limpiar selección en la lista
    }

}
