package com.app.alertas.Aster;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.scene.Node;

public class Alertas {

    public static void showSuccessAlert(String message) {
        showStyledAlert("Éxito", message, AlertType.INFORMATION, "success");
    }

    public static void showErrorAlert(String message) {
        showStyledAlert("Error", message, AlertType.ERROR, "error");
    }

    public static void showInfoAlert(String message) {
        showStyledAlert("Información", message, AlertType.INFORMATION, "info");
    }

    public static void showWarningAlert(String message) {
        showStyledAlert("Advertencia", message, AlertType.WARNING, "warning");
    }

    private static void showStyledAlert(String title, String message, AlertType type, String styleClass) {
        Alert alert = new Alert(type);
        alert.setTitle("Aster");
        alert.setHeaderText(title);
        alert.setContentText(message);
        
        
        // Estilos CSS
        alert.getDialogPane().getStylesheets().add(
            Alertas.class.getResource("/css/application.css").toExternalForm()
        );
        alert.getDialogPane().getStyleClass().add(styleClass);
        
        // Icono
        try {
            Stage stage = (Stage) alert.getDialogPane().getScene().getWindow();
            stage.getIcons().add(new Image(Alertas.class.getResourceAsStream("/imagenes/icon.png")));
        } catch (Exception e) {
            System.err.println("Error al cargar el icono de la alerta");
        }
        
        alert.show();
    }
}