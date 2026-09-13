package org.litebridge.orm.tx;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LitebridgeDriverManagerDataSourceTest {

    @AfterEach
    void tearDown() {
        DriverManager.setLogWriter(null);
        DriverManager.setLoginTimeout(0);
    }

    @Test
    void getConnection() throws SQLException {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:getConnection;DB_CLOSE_DELAY=-1",
                "sa",
                ""
        );

        // When
        final Connection connection = dataSource.getConnection();

        // Then
        assertNotNull(connection);
        assertFalse(connection.isClosed());

        connection.close();
    }

    @Test
    void getConnection_usernamePassword() throws SQLException {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:getConnectionUsernamePassword;DB_CLOSE_DELAY=-1",
                "unused",
                "unused"
        );

        // When
        final Connection connection = dataSource.getConnection("sa", "");

        // Then
        assertNotNull(connection);
        assertFalse(connection.isClosed());

        connection.close();
    }

    @Test
    void getLogWriter() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:getLogWriter",
                "sa",
                ""
        );
        final PrintWriter printWriter = new PrintWriter(new StringWriter());

        DriverManager.setLogWriter(printWriter);

        // When
        final PrintWriter result = dataSource.getLogWriter();

        // Then
        assertSame(printWriter, result);
    }

    @Test
    void setLogWriter() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:setLogWriter",
                "sa",
                ""
        );
        final PrintWriter printWriter = new PrintWriter(new StringWriter());

        // When
        dataSource.setLogWriter(printWriter);

        // Then
        assertSame(printWriter, DriverManager.getLogWriter());
    }

    @Test
    void setLoginTimeout() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:setLoginTimeout",
                "sa",
                ""
        );

        // When
        dataSource.setLoginTimeout(10);

        // Then
        assertEquals(10, DriverManager.getLoginTimeout());
    }

    @Test
    void getLoginTimeout() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:getLoginTimeout",
                "sa",
                ""
        );

        DriverManager.setLoginTimeout(15);

        // When
        final int result = dataSource.getLoginTimeout();

        // Then
        assertEquals(15, result);
    }

    @Test
    void getParentLogger() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:getParentLogger",
                "sa",
                ""
        );

        // When
        final Logger result = dataSource.getParentLogger();

        // Then
        assertEquals(Logger.getLogger(Logger.GLOBAL_LOGGER_NAME), result);
    }

    @Test
    void unwrap() throws SQLException {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:unwrap",
                "sa",
                ""
        );

        // When
        final LitebridgeDriverManagerDataSource result = dataSource.unwrap(LitebridgeDriverManagerDataSource.class);

        // Then
        assertSame(dataSource, result);
    }

    @Test
    void unwrap_dataSource() throws SQLException {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:unwrapDataSource",
                "sa",
                ""
        );

        // When
        final DataSource result = dataSource.unwrap(DataSource.class);

        // Then
        assertSame(dataSource, result);
    }

    @Test
    void unwrap_throwsSQLException() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:unwrapThrowsSQLException",
                "sa",
                ""
        );

        // When
        final SQLException result = assertThrows(SQLException.class, () -> dataSource.unwrap(Connection.class));

        // Then
        assertEquals("Cannot unwrap to java.sql.Connection", result.getMessage());
    }

    @Test
    void isWrapperFor_true() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:isWrapperForTrue",
                "sa",
                ""
        );

        // When
        final boolean result = dataSource.isWrapperFor(LitebridgeDriverManagerDataSource.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isWrapperFor_dataSource_true() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:isWrapperForDataSourceTrue",
                "sa",
                ""
        );

        // When
        final boolean result = dataSource.isWrapperFor(DataSource.class);

        // Then
        assertTrue(result);
    }

    @Test
    void isWrapperFor_false() {
        // Given
        final LitebridgeDriverManagerDataSource dataSource = new LitebridgeDriverManagerDataSource(
                "jdbc:h2:mem:isWrapperForFalse",
                "sa",
                ""
        );

        // When
        final boolean result = dataSource.isWrapperFor(Connection.class);

        // Then
        assertFalse(result);
    }
}