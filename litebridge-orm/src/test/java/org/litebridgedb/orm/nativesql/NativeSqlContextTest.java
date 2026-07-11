package org.litebridgedb.orm.nativesql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.litebridgedb.db.spi.update.UpdateResult;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class NativeSqlContextTest {

    private TransactionalDatabaseProvider databaseProvider;
    private TransactionManager transactionManager;
    private NativeSqlContext context;

    @BeforeEach
    void setUp() {
        databaseProvider = mock(TransactionalDatabaseProvider.class);
        transactionManager = mock(TransactionManager.class);
        when(databaseProvider.transactionManager()).thenReturn(transactionManager);
        context = new NativeSqlContext(databaseProvider);
    }

    @Test
    void query_positionalParametersArray() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = ?";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.nativeSqlQuery(eq(sql), any(), eq(transactionManager))).thenReturn(expectedRows);

        // When
        final List<Row> results = context.query(sql, "Smith");

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_positionalParametersList() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = ?";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.nativeSqlQuery(eq(sql), any(), eq(transactionManager))).thenReturn(expectedRows);

        // When
        final List<Row> results = context.query(sql, List.of("Smith"));

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_namedParameters() throws SQLException {
        // Given
        final String sql = "SELECT * FROM LB.PERSON WHERE SURNAME = :name";
        final List<Row> expectedRows = Collections.emptyList();
        when(databaseProvider.nativeSqlQuery(eq("SELECT * FROM LB.PERSON WHERE SURNAME = ?"), any(), eq(transactionManager))).thenReturn(expectedRows);

        // When
        final List<Row> results = context.query(sql, Map.of("name", "Smith"));

        // Then
        assertNotNull(results);
        assertEquals(expectedRows, results);
    }

    @Test
    void query_exception() throws SQLException {
        // Given
        final String sql = "SELECT * FROM INVALID";
        when(databaseProvider.nativeSqlQuery(eq(sql), any(), eq(transactionManager))).thenThrow(new SQLException("Syntax error"));

        // When / Then
        assertThrows(IllegalStateException.class, () -> context.query(sql));
    }

    @Test
    void execute_positionalParametersArray() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = ? WHERE PERSON_ID = ?";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.nativeSqlUpdate(eq(sql), any(), eq(transactionManager))).thenReturn(expectedResult);

        // When
        final UpdateResult result = context.execute(sql, "Smith", 1);

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_positionalParametersList() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = ? WHERE PERSON_ID = ?";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.nativeSqlUpdate(eq(sql), any(), eq(transactionManager))).thenReturn(expectedResult);

        // When
        final UpdateResult result = context.execute(sql, List.of("Smith", 1));

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_namedParameters() throws SQLException {
        // Given
        final String sql = "UPDATE LB.PERSON SET SURNAME = :name WHERE PERSON_ID = :id";
        final UpdateResult expectedResult = mock(UpdateResult.class);
        when(databaseProvider.nativeSqlUpdate(eq("UPDATE LB.PERSON SET SURNAME = ? WHERE PERSON_ID = ?"), any(), eq(transactionManager))).thenReturn(expectedResult);

        // When
        final UpdateResult result = context.execute(sql, Map.of("name", "Smith", "id", 1));

        // Then
        assertNotNull(result);
        assertEquals(expectedResult, result);
    }

    @Test
    void execute_exception() throws SQLException {
        // Given
        final String sql = "UPDATE INVALID SET NAME = 'Test'";
        when(databaseProvider.nativeSqlUpdate(eq(sql), any(), eq(transactionManager))).thenThrow(new SQLException("Table not found"));

        // When / Then
        assertThrows(IllegalStateException.class, () -> context.execute(sql));
    }
}
