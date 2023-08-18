package jdbc;

import lombok.extern.slf4j.Slf4j;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;

@Slf4j
public class CustomConnector {

    public Connection getConnection(String driver, String url) {
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url);
            logConnectionStatus(connection);
            return connection;

        } catch (Exception e) {
            log.info("Error: " + e.getMessage());
            return null;
        }

    }

    public Connection getConnection(String driver, String url, String user, String password) throws SQLException{
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            logConnectionStatus(connection);
            return connection;

        } catch (Exception e) {
            log.info("Error: " + e.getMessage());
            return null;
        }
    }


    private void logConnectionStatus(Connection connection){
        String status = (Objects.nonNull(connection)) ? "Connected to the database."
            : "Failed to connect to database.";
        log.info(status);
    }
}
