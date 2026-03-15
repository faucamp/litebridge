//package org.litebridge.orm.tx;
//
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//
//import java.io.PrintWriter;
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import static org.junit.jupiter.api.Assertions.assertFalse;
//import static org.junit.jupiter.api.Assertions.assertSame;
//import static org.junit.jupiter.api.Assertions.assertThrows;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class SingleConnectionDataSourceTest {
//
//    private final String url = "jdbc:h2:mem:lb";
//    private final String username = "testUser";
//    private final String password = "testPassword";
//
//    private SingleConnectionDataSource dataSource;
//
//    @BeforeEach
//    void beforeEach() {
//        dataSource = new SingleConnectionDataSource(url, username, password);
//    }
//
//    @Test
//    void getConnectionWithUsernameAndPassword() {
//        // When
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, () -> dataSource.getConnection("user", "password"));
//    }
//
//    @Test
//    void getLogWriter() {
//        // Given
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, dataSource::getLogWriter);
//    }
//
//    @Test
//    void setLogWriter() {
//        // Given
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, () -> dataSource.setLogWriter(mock(PrintWriter.class)));
//    }
//
//    @Test
//    void setLoginTimeout() {
//        // Given
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, () -> dataSource.setLoginTimeout(10));
//    }
//
//    @Test
//    void getLoginTimeout() {
//        // Given
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, dataSource::getLoginTimeout);
//    }
//
//    @Test
//    void getParentLogger() {
//        // Given
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(mock(Connection.class));
//
//        // Then
//        assertThrows(UnsupportedOperationException.class, dataSource::getParentLogger);
//    }
//
//    @Test
//    void unwrap() throws SQLException {
//        // Given
//        final Connection connection = mock(Connection.class);
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection);
//
//        when(connection.unwrap(Connection.class)).thenReturn(connection);
//
//        // When
//        final Connection result = dataSource.unwrap(Connection.class);
//
//        // Then
//        assertSame(connection, result);
//    }
//
//    @Test
//    void unwrapThrowsSQLException() throws SQLException {
//        // Given
//        final Connection connection = mock(Connection.class);
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection);
//        final SQLException sqlException = new SQLException("Test exception");
//
//        when(connection.unwrap(Connection.class)).thenThrow(sqlException);
//
//        // Then
//        assertSame(sqlException, assertThrows(SQLException.class, () -> dataSource.unwrap(Connection.class)));
//    }
//
//    @Test
//    void isWrapperFor() throws SQLException {
//        // Given
//        final Connection connection = mock(Connection.class);
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection);
//
//        when(connection.isWrapperFor(Connection.class)).thenReturn(false);
//
//        // When
//        final boolean result = dataSource.isWrapperFor(Connection.class);
//
//        // Then
//        assertFalse(result);
//    }
//
//    @Test
//    void isWrapperForThrowsSQLException() throws SQLException {
//        // Given
//        final Connection connection = mock(Connection.class);
//        final SingleConnectionDataSource dataSource = new SingleConnectionDataSource(connection);
//        final SQLException sqlException = new SQLException("Test exception");
//
//        when(connection.isWrapperFor(Connection.class)).thenThrow(sqlException);
//
//        // Then
//        assertSame(sqlException, assertThrows(SQLException.class, () -> dataSource.isWrapperFor(Connection.class)));
//    }
//}