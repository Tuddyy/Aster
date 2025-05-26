package com.app.controller.Aster;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Parent;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.app.alertas.Aster.Alertas;
import com.app.dbconnector.Aster.DatabaseConnector;

public class LoginController {
    
    @FXML private TextField DniField;
    @FXML private PasswordField PasswordField;
    @FXML private ImageView logoImage;
    @FXML private ImageView usuarioImage;
    @FXML private ImageView passwordImage;
    @FXML private ImageView usuariosImage;
    
    @FXML
    public void initialize() {
    	
        /* Carga todas las imágenes */
        logoImage.setImage(loadImage("/imagenes/Logo.png"));
        usuarioImage.setImage(loadImage("/imagenes/usuario.png"));
        passwordImage.setImage(loadImage("/imagenes/contraseña.png"));
        usuariosImage.setImage(loadImage("/imagenes/usuarios.png"));
    }
    
    private Image loadImage(String path) {
        try {
            return new Image(getClass().getResourceAsStream(path));
        } catch (Exception e) {
            System.err.println("Error al cargar imagen: " + path);
            return null;
        }
    }
    
    @FXML
    void handleLogin(ActionEvent event) {
        String user = DniField.getText();
        String pass = PasswordField.getText();

        if (user.isBlank() || pass.isBlank()) {
            Alertas.showWarningAlert("Por favor, completa ambos campos.");
            return;
        }

        try (Connection conn = DatabaseConnector.connect()) {
            String sql = "SELECT * FROM login WHERE id_docent = ? AND contraseña = ?";
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setString(1, user);
            stmt.setString(2, pass);

            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String rol = rs.getString("rol");
                
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/Aplicacion.fxml"));
                Parent root = loader.load();

                AplicacionController controller = loader.getController();
                controller.setUsuario(user, rol);

                Scene mainScene = new Scene(root);
                mainScene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());

                Stage stage = new Stage();
                stage.setScene(mainScene);
                stage.setTitle("Aster");
                stage.getIcons().add(loadImage("/imagenes/icon.png"));
                stage.show();

                ((Stage) DniField.getScene().getWindow()).close();
            } else {
                Alertas.showErrorAlert("DNI o contraseña incorrectos.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Alertas.showErrorAlert("Error al conectar con la base de datos.");
        }
    }
}