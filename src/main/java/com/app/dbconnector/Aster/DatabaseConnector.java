package com.app.dbconnector.Aster;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnector {
    private static final String URL = "jdbc:mysql://localhost:3306/aster_guardias"; 
    private static final String USER = "root"; 
    private static final String PASSWORD = "";  

    public static Connection connect() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }
}
