package lk.ijse.dep10.app.db;

import javafx.scene.control.Alert;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class DBConnection {
    private static DBConnection dbConnection;
    private final Connection connection;

    private DBConnection() {
        Properties configProperties = new Properties();
        File configFile = new File("application.properties");

        try {
            try {
                FileReader fr = new FileReader(configFile);
                try {
                    configProperties.load(fr);
                    String host = configProperties.getProperty("dep10.sas.host", "localhost");
                    String port = configProperties.getProperty("dep10.sas.port", "3306");
                    String dataBase = configProperties.getProperty("dep10.sas.database", "dep10_studentRegister");
                    String user = configProperties.getProperty("dep10.sas.user", "root");
                    String password = configProperties.getProperty("dep10.sas.password", "");

                    String quaeryString="jdbc:mysql://dep10.lk:3306/dep10_studentRegister?createDatabaseIfNotExist=true&allowMultiQueries=true";
                } catch (IOException e) {
                    new Alert(Alert.AlertType.ERROR,"Failed to read Configuration file").showAndWait();
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            } catch (FileNotFoundException e) {
                new Alert(Alert.AlertType.ERROR,"Configuration file is not found").showAndWait();
                e.printStackTrace();
                throw new RuntimeException(e);
            }

            connection = DriverManager.getConnection("jdbc:mysql://dep10.lk:3306/dep10_studentRegister?createDatabaseIfNotExist=true&allowMultiQueries=true", "root", "root");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static DBConnection getInstance() {
        return dbConnection==null?dbConnection=new DBConnection():dbConnection;
    }

    public Connection getConnection() {
        return connection;
    }
}
