package jdbc;



import org.postgresql.Driver;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.logging.Logger;


public class CustomConnector {
    Logger logger = Logger.getLogger("CustomConnector");

    public Connection getConnection(String url) {
        try{
            Connection connection = DriverManager.getConnection(url);
            logConnectionStatus(connection);
            return connection;

        } catch (SQLException se) {
            se.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public Connection getConnection(String url, String user, String password) {
        try{
            Connection connection = DriverManager.getConnection(url, user, password);
            logConnectionStatus(connection);
            return connection;

        } catch (SQLException se) {
            se.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private void logConnectionStatus(Connection connection){
        logger.info(
            (Objects.nonNull(connection)) ? "Connected to the database."
                    : "Failed to connect to database."
        );
    }
}
