package org.litebridgedb.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ColumnIdentifierGeneratorTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();

    @Test
    void createSelectColumn_withoutAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "col_alias");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN AS col_alias", result);
    }

    @Test
    void createSelectColumn_withTableAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        column.table().setAlias("t1");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("t1.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withReservedTableNameColumnNameAndAlias_quotesIdentifiers() {
        // Given
        final Column column = new Column(new Table("TABLE"), "ORDER", "GROUP");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("\"TABLE\".\"ORDER\" AS \"GROUP\"", result);
    }

    @Test
    void createSelectColumn_withReservedTableAlias_quotesTableAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        column.table().setAlias("TABLE");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("\"TABLE\".TEST_COLUMN", result);
    }

    @Test
    void quoteIdentifier_reservedKeyword() {
        // Given
        final String identifier = "TABLE";

        // When
        final String result = generator.quoteIdentifier(identifier);

        // Then
        assertEquals("\"TABLE\"", result);
    }

    @Test
    void quoteIdentifier_notNeeded() {
        // Given
        final String identifier = "TEST";

        // When
        final String result = generator.quoteIdentifier(identifier);

        // Then
        assertEquals("TEST", result);
    }

    @Test
    void createAliasDeclaration() {
        // Given
        final String alias = "my_alias";

        // When
        final String result = generator.createAliasDeclaration(alias);

        // Then
        assertEquals("AS my_alias", result);
    }

    @Test
    void createAlias_withReservedKeyword_quotesAliasDeclaration() {
        // Given
        final String alias = "TABLE";

        // When
        final String result = generator.createAliasDeclaration(alias);

        // Then
        assertEquals("AS \"TABLE\"", result);
    }
}