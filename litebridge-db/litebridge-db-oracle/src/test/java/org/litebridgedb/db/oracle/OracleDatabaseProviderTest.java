package org.litebridgedb.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Limit;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleDatabaseProviderTest {

    @Test
    void getSequenceColumnValueGenerator() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

        // When
        final SequenceColumnValueGenerator result = oracleDatabaseProvider.getSequenceColumnValueGenerator("test_sequence");

        // Then
        assertInstanceOf(OracleSequenceColumnValueGenerator.class, result);
    }

    @Test
    void createAlias_validAlias() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final String alias = "MY_ALIAS";

        // When
        String result = provider.createAlias(alias);

        // Then
        assertEquals("MY_ALIAS", result);
    }

    @Test
    void appendLimitClause_withOffsetAndLimit() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(Optional.of(10), Optional.of(5));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        provider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE OFFSET 5 ROWS FETCH FIRST 10 ROWS ONLY", sql.toString());
    }

    @Test
    void appendLimitClause_withOffsetOnly() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(Optional.empty(), Optional.of(5));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        provider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE OFFSET 5 ROWS", sql.toString());
    }

    @Test
    void appendLimitClause_withLimitOnly() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(Optional.of(10), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        provider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE FETCH FIRST 10 ROWS ONLY", sql.toString());
    }

    @Test
    void appendLimitClause_withoutOffsetAndLimit() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Limit limit = new Limit(Optional.empty(), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST_TABLE");

        // When
        provider.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST_TABLE", sql.toString());
    }

    @Test
    void extractGeneratedKeys_withGeneratedKeys() throws SQLException {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final ColumnMetaData otherIdColumn = new ColumnMetaData(table, "OTHER_ID", false, Types.INTEGER);
        final TableMetaData tableMetaData = new TableMetaData(table,
                List.of("ID", "OTHER_ID"),
                List.of(idColumn, otherIdColumn));

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(true);
        when(resultSet.getObject(1)).thenReturn(1);
        when(resultSet.getObject(2)).thenReturn(2);

        // When
        Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(tableMetaData, preparedStatement);

        // Then
        assertEquals(2, result.size());
        assertEquals(1, result.get(idColumn));
        assertEquals(2, result.get(otherIdColumn));
        verify(resultSet, times(1)).close();
    }

    @Test
    void extractGeneratedKeys_withoutGeneratedKeys() throws SQLException {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final PreparedStatement preparedStatement = mock(PreparedStatement.class);
        final ResultSet resultSet = mock(ResultSet.class);
        final Table table = new Table("TEST_TABLE", null);
        final ColumnMetaData idColumn = new ColumnMetaData(table, "ID", false, Types.INTEGER);
        final TableMetaData tableMetaData = new TableMetaData(table, List.of("ID"), List.of(idColumn));

        when(preparedStatement.getGeneratedKeys()).thenReturn(resultSet);
        when(resultSet.next()).thenReturn(false);

        // When
        Map<ColumnMetaData, Object> result = provider.extractGeneratedKeys(tableMetaData, preparedStatement);

        // Then
        assertTrue(result.isEmpty());
        verify(resultSet, times(1)).close();
    }

    @Test
    void createColumnIdentifierGenerator() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        final ColumnIdentifierGenerator result = provider.createColumnIdentifierGenerator();

        // Then
        assertInstanceOf(OracleColumnIdentifierGenerator.class, result);
    }

    @Test
    void getLogger() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        var result = provider.getLogger();

        // Then
        assertNotNull(result);
    }
}