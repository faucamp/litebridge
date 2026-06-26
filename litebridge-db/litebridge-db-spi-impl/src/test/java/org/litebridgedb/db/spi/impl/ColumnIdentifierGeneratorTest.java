package org.litebridgedb.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class ColumnIdentifierGeneratorTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();

    @Test
    void createSelectColumnIdentifier_withoutAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");

        // When
        final String result = generator.createSelectColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withAlias_notIncluded() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "col_alias");

        // When
        final String result = generator.createSelectColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withAlias_included() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "col_alias");

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN AS col_alias", result);
    }

    @Test
    void createSelectColumnIdentifier_withBlankAlias_included() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", " ");

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withTableAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        column.table().setAlias("t1");

        // When
        final String result = generator.createSelectColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("t1.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withReservedTableNameColumnNameAndAlias_quotesIdentifiers() {
        // Given
        final Column column = new Column(new Table("TABLE"), "ORDER", "GROUP");

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, mock(Select.class));

        // Then
        assertEquals("\"TABLE\".\"ORDER\" AS \"GROUP\"", result);
    }

    @Test
    void createSelectColumnIdentifier_withReservedTableAlias_quotesTableAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        column.table().setAlias("TABLE");

        // When
        final String result = generator.createSelectColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("\"TABLE\".TEST_COLUMN", result);
    }

    @Test
    void createColumnReference_withoutAlias_usesColumnName() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");

        // When
        final String result = generator.createColumnReference(column);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createColumnReference_withAlias_usesAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "col_alias");

        // When
        final String result = generator.createColumnReference(column);

        // Then
        assertEquals("col_alias", result);
    }

    @Test
    void createColumnReference_withoutAliasAndReservedColumnName_quotesColumnName() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "ORDER");

        // When
        final String result = generator.createColumnReference(column);

        // Then
        assertEquals("\"ORDER\"", result);
    }

    @Test
    void createColumnReference_withReservedAlias_quotesAlias() {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "GROUP");

        // When
        final String result = generator.createColumnReference(column);

        // Then
        assertEquals("\"GROUP\"", result);
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
    void createAlias() {
        // Given
        final String alias = "my_alias";

        // When
        final String result = generator.createAlias(alias);

        // Then
        assertEquals("AS my_alias", result);
    }

    @Test
    void createAlias_withReservedKeyword_quotesAlias() {
        // Given
        final String alias = "TABLE";

        // When
        final String result = generator.createAlias(alias);

        // Then
        assertEquals("AS \"TABLE\"", result);
    }
}