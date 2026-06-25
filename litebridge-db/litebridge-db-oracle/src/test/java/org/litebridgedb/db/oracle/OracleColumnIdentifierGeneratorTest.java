package org.litebridgedb.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleColumnIdentifierGeneratorTest {

    private final OracleColumnIdentifierGenerator generator = new OracleColumnIdentifierGenerator();

    @Test
    void createSelectColumnIdentifier_withoutSelect_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, null);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withEmptyJoins_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumnIdentifier_withJoinWithoutUsing_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(column, Operator.EQ, "TEST_VALUE")));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForDifferentSelectColumn_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Column otherColumn = new Column(table, "OTHER_COLUMN");
        final Join join = new Join(table, List.of(new Condition(otherColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, select);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createColumnIdentifier_withUsingForSameSelectColumnButUnrelatedTable_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Table unrelatedTable = new Table("UNRELATED_TABLE", null);
        final Column column = new Column(unrelatedTable, "TEST_COLUMN");
        final Column selectColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(selectColumn, Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumnIdentifier(column, true, select);

        // Then
        assertEquals("UNRELATED_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createAlias_validAlias() {
        // Given
        final String alias = "MY_ALIAS";

        // When
        String result = generator.createAlias(alias);

        // Then
        assertEquals("MY_ALIAS", result);
    }
}