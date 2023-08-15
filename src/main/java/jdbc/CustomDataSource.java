package jdbc;

import javax.sql.DataSource;
import lombok.Getter;
import lombok.Setter;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Objects;
import java.util.Properties;
import java.util.logging.Logger;

@Getter
@Setter
public class CustomDataSource implements DataSource {


    private final Logger LOGGER = Logger.getLogger("CustomDataSource");
    private final String driver;
    private final String url;
    private final String name;
    private final String password;
    private final String NOT_IMPLEMENTED_MSG = "Not implemented";
    private final CustomConnector customConnector = new CustomConnector();

    private static volatile CustomDataSource instance;


    private CustomDataSource(String driver, String url, String password, String name) {
        this.driver = driver;
        this.url = url;
        this.password = password;
        this.name = name;
    }


    public static CustomDataSource getInstance() {
        synchronized(CustomDataSource.class) {
            if (Objects.isNull(instance)) {
                PropertyFileLoader propertyFileLoader = new PropertyFileLoader("app.properties");
                Properties properties = propertyFileLoader.getProperties();
                instance = new CustomDataSource(
                        properties.getProperty("postgres.driver"),
                        properties.getProperty("postgres.url"),
                        properties.getProperty("postgres.password"),
                        properties.getProperty("postgres.name")
                );
            }
            return instance;
        }
    }

    @Override
    public Connection getConnection() throws SQLException {
        return customConnector.getConnection(url, name, password);
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        return customConnector.getConnection(this.url, username, password);
    }


    // ... rest of the methods ...
    @Override
    public PrintWriter getLogWriter() throws SQLException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        throw new UnsupportedOperationException(NOT_IMPLEMENTED_MSG);
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        throw new UnsupportedOperationException("Not implemented");
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return LOGGER;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        if (iface.isInstance(this)) {
            return iface.cast(this);
        }
        throw new SQLException("DataSource is not a wrapper for " + iface);
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return iface.isInstance(this);
    }
}
