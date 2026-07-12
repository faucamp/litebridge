package org.litebridge.orm.tx;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

/**
 * A simple {@link DataSource} implementation that utilises {@link DriverManager} to create connections.
 * It provides basic functionality for connection management by encapsulating a URL,
 * username, and password to connect to a database.
 * <p>
 * This class implements the {@link javax.sql.DataSource} interface and delegates connection
 * retrieval to the {@link java.sql.DriverManager} class, making it suitable for lightweight
 * database connection management.
 * <p>
 * Thread safety is not guaranteed. Ensure proper synchronisation for concurrent access.
 */
public class LitebridgeDriverManagerDataSource implements DataSource {

    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructs a new {@code LitebridgeDriverManagerDataSource} with the given URL, username, and password.
     *
     * @param url      the database URL.
     * @param username the database username.
     * @param password the database password.
     */
    public LitebridgeDriverManagerDataSource(final String url, final String username, final String password) {
        this.url = url;
        this.username = username;
        this.password = password;
    }

    @Override
    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public Connection getConnection(final String username, final String password) throws SQLException {
        return DriverManager.getConnection(url, username, password);
    }

    @Override
    public PrintWriter getLogWriter() {
        return DriverManager.getLogWriter();
    }

    @Override
    public void setLogWriter(final PrintWriter out) {
        DriverManager.setLogWriter(out);
    }

    @Override
    public void setLoginTimeout(final int seconds) {
        DriverManager.setLoginTimeout(seconds);
    }

    @Override
    public int getLoginTimeout() {
        return DriverManager.getLoginTimeout();
    }

    @Override
    public Logger getParentLogger() {
        return Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T unwrap(final Class<T> iface) throws SQLException {
        if (isWrapperFor(iface)) {
            return (T) this;
        }

        throw new SQLException("Cannot unwrap to " + iface.getName());
    }

    @Override
    public boolean isWrapperFor(final Class<?> iface) {
        return iface.isInstance(this);
    }
}
