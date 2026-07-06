package org.litebridgedb.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.ClauseType;
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
import static org.mockito.Mockito.mock;

class OracleColumnIdentifierGeneratorTest {

    private final OracleColumnIdentifierGenerator generator = new OracleColumnIdentifierGenerator();

    @Test
    void createSelectColumn_withoutSelect_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withEmptyJoins_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withJoinWithoutUsing_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(new SelectColumn(column, generator), Operator.EQ, "TEST_VALUE")));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withUsingForDifferentSelectColumn_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Column otherColumn = new Column(table, "OTHER_COLUMN");
        final Join join = new Join(table, List.of(new Condition(new SelectColumn(otherColumn, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withUsingForSameSelectColumnButUnrelatedTable_usesDefaultTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Table unrelatedTable = new Table("UNRELATED_TABLE", null);
        final Column column = new Column(unrelatedTable, "TEST_COLUMN");
        final Column selectColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(new SelectColumn(selectColumn, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);

        // Then
        assertEquals("UNRELATED_TABLE.TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withUsingForSameSelectColumnAndTable_doesNotUseTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(new SelectColumn(column, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withUsingForSameSelectColumnButFromOtherSideOfJoin_doesNotUseTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Column column = new Column(joinedTable, "TEST_COLUMN");
        final Column tableColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(new SelectColumn(tableColumn, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withMultipleJoins_hitsSecondJoin() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable1 = new Table("JOINED_TABLE1", null);
        final Table joinedTable2 = new Table("JOINED_TABLE2", null);
        final Column column = new Column(joinedTable2, "TEST_COLUMN");
        final Column tableColumn = new Column(table, "TEST_COLUMN");

        final Join join1 = new Join(joinedTable1, List.of(new Condition(new SelectColumn(tableColumn, generator), Operator.EQ, "OTHER")));
        final Join join2 = new Join(joinedTable2, List.of(new Condition(new SelectColumn(tableColumn, generator), Operator.USING, null)));

        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join1, join2),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_joinUsing_omitTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", "T");
        final Column column = new Column(table, "TEST_COLUMN");
        final Column columnWithAlias = new Column(table, "TEST_COLUMN", "C");
        final Join join = new Join(table, List.of(new Condition(new SelectColumn(columnWithAlias, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withUsingForSameSelectColumnButFromOtherSideOfJoinTable_doesNotUseTableQualifier() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Table joinedTable = new Table("JOINED_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN");
        final Column joinColumn = new Column(joinedTable, "TEST_COLUMN");
        final Join join = new Join(joinedTable, List.of(new Condition(new SelectColumn(joinColumn, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN", result);
    }

    @Test
    void createSelectColumn_withIncludeAlias_returnsColumnWithAlias() {
        // Given
        final Table table = new Table("TEST_TABLE", null);
        final Column column = new Column(table, "TEST_COLUMN", "MY_ALIAS");
        final Column tableColumn = new Column(table, "TEST_COLUMN");
        final Join join = new Join(table, List.of(new Condition(new SelectColumn(tableColumn, generator), Operator.USING, null)));
        final Select select = new Select(
                table,
                List.of(new SelectColumn(column, generator)),
                List.of(join),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty());

        // When
        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);

        // Then
        assertEquals("TEST_COLUMN MY_ALIAS", result);
    }

    @Test
    void createAlias_validAliasDeclaration() {
        // Given
        final String alias = "MY_ALIAS";

        // When
        String result = generator.createAliasDeclaration(alias);

        // Then
        assertEquals("MY_ALIAS", result);
    }
}