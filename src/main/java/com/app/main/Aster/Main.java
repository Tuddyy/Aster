package com.app.main.Aster;

import javafx.application.Application;
import javafx.stage.Stage;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.fxml.FXMLLoader;

public class Main extends Application {
	@Override
	public void start(Stage primaryStage) {
	    try {
	        Parent root = FXMLLoader.load(getClass().getResource("/fxml/Login.fxml"));
	        Scene scene = new Scene(root);
	        primaryStage.setWidth(700);
	        primaryStage.setHeight(500);
	        primaryStage.setMinWidth(700);  
	        primaryStage.setMinHeight(500); 
	        scene.getStylesheets().add(getClass().getResource("/css/application.css").toExternalForm());
	        primaryStage.setScene(scene);
	        primaryStage.setTitle("Login");
	        primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/imagenes/icon.png")));
	        primaryStage.show();
	        
	    } catch(Exception e) {
	        e.printStackTrace();
	    }
	}
	
	public static void main(String[] args) {
		launch(args);
	}
}
