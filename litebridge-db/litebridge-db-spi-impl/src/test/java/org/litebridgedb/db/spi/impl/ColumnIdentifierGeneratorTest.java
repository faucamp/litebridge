package org.litebridgedb.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class ColumnIdentifierGeneratorTest {

    private final ColumnIdentifierGenerator generator = new ColumnIdentifierGenerator();

    @Test
    void createColumnIdentifier_withoutAlias() throws Exception {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");

        // When
        final String result = generator.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withAlias_notIncluded() throws Exception {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN", "col_alias");

        // When
        final String result = generator.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withTableAlias() throws Exception {
        // Given
        final Column column = new Column(new Table("TEST_TABLE"), "TEST_COLUMN");
        column.table().setAlias("t1");

        // When
        final String result = generator.createColumnIdentifier(column, false, mock(Select.class));

        // Then
        assertEquals("t1.TEST_COLUMN", result);
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
    void quoteIdentifier_null() {
        // Given
        final String identifier = null;

        // When
        final String result = generator.quoteIdentifier(identifier);

        // Then
        assertNull(result);
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
}