package jdbc;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;


public class CustomConnector {
    private final Logger LOGGER = Logger.getLogger("CustomConnector");

    public Connection getConnection(String driver, String url) {
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url);
            logConnectionStatus(connection);
            return connection;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection getConnection(String driver, String url, String user, String password) throws SQLException{
        try{
            Class.forName(driver);
            Connection connection = DriverManager.getConnection(url, user, password);
            logConnectionStatus(connection);
            return connection;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    private void logConnectionStatus(Connection connection){
        String status = (Objects.nonNull(connection)) ? "Connected to the database."
                : "Failed to connect to database.";
        LOGGER.info(status);
    }
}
