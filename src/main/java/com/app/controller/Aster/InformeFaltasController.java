package com.app.controller.Aster;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.*;
import javafx.stage.FileChooser;
import java.io.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;

public class InformeFaltasController {

    @FXML private ComboBox<String> periodoCombo;
    @FXML private ComboBox<String> docenteCombo;
    @FXML private TextArea resultadosTextArea;

    @FXML
    public void initialize() {
        periodoCombo.setItems(FXCollections.observableArrayList("Resumen automático de ausencias"));
        periodoCombo.getSelectionModel().selectFirst();
        periodoCombo.setDisable(true);
        cargarDocentes();
    }

    private void cargarDocentes() {
        ObservableList<String> docentes = FXCollections.observableArrayList("Todos los docentes");
        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement("SELECT id_docent, nom FROM docent ORDER BY nom");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                docentes.add(rs.getString("id_docent") + " - " + rs.getString("nom"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al cargar los docentes.");
        }
        docenteCombo.setItems(docentes);
        docenteCombo.getSelectionModel().selectFirst();
    }

    @FXML
    void generarInforme() {
        String docenteSel = docenteCombo.getValue();
        try {
            List<Ausencia> lista = fetchAusencias(docenteSel);
            String informe = buildInforme(lista, docenteSel);
            resultadosTextArea.setText(informe);
            guardarInformeEnArchivo(informe);
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al generar el informe: " + e.getMessage());
        }
    }

    private List<Ausencia> fetchAusencias(String docenteSel) throws SQLException {
        List<Ausencia> list = new ArrayList<>();
        String base = """
            SELECT da.data_absencia, d.id_docent, d.nom, hg.dia_setmana,
                   hg.hora_desde, hg.hora_fins, hg.contingut, hg.grup, da.motiu
              FROM docents_absents da
              JOIN docent d ON da.id_docent = d.id_docent
              JOIN horari_grup hg ON da.id_horari = hg.id_horari
        """;
        if (!docenteSel.equals("Todos los docentes")) {
            base += " WHERE d.id_docent = ?";
        }
        base += " ORDER BY da.data_absencia, hg.hora_desde";

        try (Connection conn = DatabaseConnector.connect();
             PreparedStatement stmt = conn.prepareStatement(base)) {
            if (!docenteSel.equals("Todos los docentes")) {
                String idDoc = docenteSel.split(" - ")[0];
                stmt.setString(1, idDoc);
            }
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                list.add(new Ausencia(
                    rs.getDate("data_absencia").toLocalDate(),
                    rs.getString("id_docent"),
                    rs.getString("nom"),
                    rs.getString("dia_setmana"),
                    rs.getString("hora_desde"),
                    rs.getString("hora_fins"),
                    rs.getString("contingut"),
                    rs.getString("grup"),
                    rs.getString("motiu")
                ));
            }
        }
        return list;
    }
    
    /**
     * Informe de ausencias a partir de una lista de objetos Ausencia y el docente seleccionado.
     *
     * @param list Lista de ausencias registradas
     * @param docenteSel Nombre del docente seleccionado (puede ser "Todos los docentes")
     * @return Un String con el informe formateado
     */
     private String buildInforme(List<Ausencia> list, String docenteSel) {
    	 
    	/* Formateadores de fecha: uno para día/mes/año y otro para nombre del mes + año */
        DateTimeFormatter df = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter mf = DateTimeFormatter.ofPattern("MMMM yyyy", new Locale("es"));
        StringBuilder out = new StringBuilder("INFORME DE AUSENCIAS\n===============================\n");
        
        /* Si se seleccionó un docente específico, se añade su nombre al informe */
        if (!docenteSel.equals("Todos los docentes")) {
            out.append("Docente: ").append(docenteSel.split(" - ")[1]).append("\n");
        }
        
        if (list.isEmpty()) {
            out.append("\nNo se encontraron ausencias registradas.\n");
            return out.toString();
        }
        
        /* Cálculo del primer y último día de ausencias */
        LocalDate primero = list.get(0).fecha, ultimo = primero;
        for (Ausencia a : list) {
            if (a.fecha.isBefore(primero)) primero = a.fecha;
            if (a.fecha.isAfter(ultimo)) ultimo = a.fecha;
        }

        /* Cálculo del número total de días entre la primera y última ausencia */
        long diasTot = ChronoUnit.DAYS.between(primero, ultimo);  // Sin el +1
        long semanas = diasTot / 7;  // Número de semanas completas
        long dias = diasTot % 7;    // Días restantes

        /* Agrega el resumen del periodo al informe */
        out.append("\nPeriodo de ausencias: ")
           .append(semanas > 0 ? semanas + (semanas == 1 ? " semana " : " semanas ") : "")
           .append(dias > 0 ? dias + (dias == 1 ? " día" : " días") : "")
           .append("\nDesde: ").append(primero.format(df))
           .append(" - Hasta: ").append(ultimo.format(df))
           .append("\nTotal días con ausencias: ").append(list.size()).append("\n\n");
        
        /* Detalle de ausencias por mes, fecha y profesor */
        String curMes="", curFecha="", curProf="";
        out.append("DETALLE DE AUSENCIAS:\n");
        
        for (Ausencia a : list) {
            String mes = a.fecha.format(mf).toUpperCase(); /* Ej: "MAYO 2025" */
            String fecha = a.fecha.format(df);             /* Ej: "25/05/2025" */
            
            /* Si cambia el mes, se imprime un nuevo encabezado mensual */
            if (!mes.equals(curMes)) {
                out.append("\n").append(mes).append("\n----------\n");
                curMes = mes; curFecha=""; curProf="";
            }
            
            /* Si cambia la fecha, se imprime encabezado con el día y fecha */
            if (!fecha.equals(curFecha)) {
                out.append("\n").append(convertirDia(a.dia)).append(", ").append(fecha).append("\n");
                curFecha=fecha; curProf="";
            }
            
            /* Si cambia el profesor, se imprime su nombre */
            if (!a.nom.equals(curProf)) {
                out.append("  ").append(a.nom).append(":\n");
                curProf=a.nom;
            }
            
            /* Detalle de la ausencia: hora, grupo, contenido y motivo */
            out.append("    - ").append(a.horaDesde).append(" a ").append(a.horaFins)
               .append(": ").append(a.contenido).append(" (").append(a.grup).append(")\n")
               .append("      Motivo: ").append(a.motiu).append("\n");
        }
        
        return out.toString();
    }

    /* Guardar el informe en el archivo .txt */
    private void guardarInformeEnArchivo(String contenido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar informe de ausencias");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("TXT", "*.txt"));
        String idDoc = docenteCombo.getValue().split(" - ")[0];
        chooser.setInitialFileName("Informe_" + idDoc + "_" + LocalDate.now() + ".txt");
        File f = chooser.showSaveDialog(resultadosTextArea.getScene().getWindow());
        if (f != null) {
            try (PrintWriter w = new PrintWriter(f, "UTF-8")) {
                w.write(contenido);
                Alertas.showSuccessAlert("Informe guardado en:\n" + f.getAbsolutePath());
            } catch (Exception e) {
                e.printStackTrace();
                Alertas.showErrorAlert("Error al guardar archivo: " + e.getMessage());
            }
        }
    }

    private String convertirDia(String letra) {
        return switch (letra) {
            case "L" -> "Lunes"; case "M" -> "Martes"; case "X" -> "Miércoles";
            case "J" -> "Jueves"; case "V" -> "Viernes"; default -> "";
        };
    }

    private static class Ausencia { LocalDate fecha; String idDocent, nom, dia, horaDesde, horaFins, contenido, grup, motiu;             /* Atributos de la ausencia */
                         Ausencia(LocalDate f, String id, String n, String d,String h1, String h2, String c, String g, String m) {       /* Constructor de ausencia */
                         this.fecha=f; this.idDocent=id; this.nom=n; this.dia=d; this.horaDesde=h1; this.horaFins=h2; this.contenido=c;
                         this.grup=g; this.motiu=m;
        }
    }
}