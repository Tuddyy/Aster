module ASTER {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires transitive javafx.graphics;
    
    // Abre todos los paquetes necesarios
    opens controller to javafx.fxml, javafx.graphics;
    opens application to javafx.fxml, javafx.graphics;
    
    // Exporta los paquetes principales
    exports controller;
    exports application;
}