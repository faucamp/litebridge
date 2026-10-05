package org.litebridge.orm.nativesql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NativeSqlContextTest {

    private TransactionalDatabaseProvider databaseProvider;
    private TransactionManager transactionManager;
    private NativeSqlContext nativeSqlContext;

    @BeforeEach
    void setUp() {
        databaseProvider = mock(TransactionalDatabaseProvider.class);
        transactionManager = mock(TransactionManager.class);
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        nativeSqlContext = new NativeSqlContext(databaseProvider);
    }

    @Test
    void query_positionalParametersArray() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = ?";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.executeQuery(any(PreparedSql.class), any(ConnectionProvider.class))).thenReturn(expectedRows);

        // When
        final List<Row> results = nativeSqlContext.query(sql, "Smith");

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_positionalParametersList() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = ?";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.executeQuery(any(PreparedSql.class), any(ConnectionProvider.class))).thenReturn(expectedRows);

        // When
        final List<Row> results = nativeSqlContext.query(sql, List.of("Smith"));

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_namedParameters() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = :name";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.executeQuery(any(PreparedSql.class), any(ConnectionProvider.class))).thenReturn(expectedRows);
        // When
        final List<Row> results = nativeSqlContext.query(sql, Map.of("name", "Smith"));

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_exception() throws SQLException {
        // Given
        final String sql = "SELECT * FROM INVALID";
        when(databaseProvider.executeQuery(any(PreparedSql.class), any(ConnectionProvider.class))).thenThrow(new SQLException("Syntax error"));

        // When / Then
        assertThrows(IllegalStateException.class, () -> nativeSqlContext.query(sql));
    }

    @Test
    void execute_positionalParametersArray() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = ? WHERE PERSON_ID = ?";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), any(), any(ConnectionProvider.class))).thenReturn(expectedResult);

        // When
        final UpdateResult result = nativeSqlContext.execute(sql, "Smith", 1);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_positionalParametersList() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = ? WHERE PERSON_ID = ?";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), any(), any(ConnectionProvider.class))).thenReturn(expectedResult);

        // When
        final UpdateResult result = nativeSqlContext.execute(sql, List.of("Smith", 1));

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_namedParameters() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = :name WHERE PERSON_ID = :id";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.executeUpdate(any(PreparedSql.class), any(), any(ConnectionProvider.class))).thenReturn(expectedResult);

        // When
        final UpdateResult result = nativeSqlContext.execute(sql, Map.of("name", "Smith", "id", 1));

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        final String sql = "UPDATE INVALID SET NAME = 'Test'";
        when(databaseProvider.executeUpdate(any(PreparedSql.class), any(), any(ConnectionProvider.class))).thenThrow(new SQLException("Table not found"));

        // When / Then
        assertThrows(IllegalStateException.class, () -> nativeSqlContext.execute(sql));
    }
}
