package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OracleDatabaseProviderTest {

    @Test
    void createColumnIdentifier_withoutSelect_usesDefaultTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        final String result = provider.createColumnIdentifier(column, true, null);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withEmptyJoins_usesDefaultTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Select select = new Select(
                table,
                List.of(column),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withJoinWithoutUsing_usesDefaultTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(column, Operator.EQ, "TEST_VALUE")));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForDifferentColumn_usesDefaultTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Column otherColumn = new Column(table, "OTHER_COLUMN");
        final Join join = new Join(table, List.of(new Condition(otherColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForSameColumnFromJoinSideAndSelectedColumnFromSelectTable_omitsTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Column joinColumn = new Column(joinedTable, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(joinColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForSameColumnFromSelectSideAndSelectedColumnFromJoinTable_omitsTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Column column = new Column(joinedTable, "TEST_COLUMN");
        final Column selectColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(selectColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForSameColumnButUnrelatedTable_usesDefaultTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Table unrelatedTable = new Table("UNRELATED_TABLE", null);
        final Column column = new Column(unrelatedTable, "TEST_COLUMN");
        final Column selectColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(selectColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("UNRELATED_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withMatchingUsing_omitsTableQualifier() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(column, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withMatchingUsingAndAlias_omitsTableQualifierAndIncludesAlias() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN").as("TEST_ALIAS");
        final Join join = new Join(table, List.of(new Condition(column, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_COLUMN TEST_ALIAS", result);
    }

    @Test
    void createColumnIdentifier_withMatchingUsingAndAliasExcluded_omitsTableQualifierAndAlias() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN").as("TEST_ALIAS");
        final Join join = new Join(table, List.of(new Condition(column, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, false, select);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withMatchingUsingAndBlankAlias_omitsTableQualifierAndAlias() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN").as(" ");
        final Join join = new Join(table, List.of(new Condition(column, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(column),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = provider.createColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSequenceNextValueForDirectInsert_validSequenceName() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final String sequenceName = "MY_SEQUENCE";

        // When
        String result = provider.createSequenceNextValueForDirectInsert(sequenceName);

        // Then
        assertEquals("MY_SEQUENCE.NEXTVAL", result);
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
    void getLogger() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();

        // When
        var result = provider.getLogger();

        // Then
        assertNotNull(result);
    }
}