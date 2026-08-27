//package org.litebridge.db.oracle;
//
//import org.junit.jupiter.api.Test;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Operation;
//import org.litebridge.db.spi.Table;
//import org.litebridge.db.spi.expression.ClauseType;
//import org.litebridge.db.spi.expression.ColumnExpression;
//import org.litebridge.db.spi.expression.SelectExpression;
//import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
//import org.litebridge.db.spi.impl.function.SelectColumn;
//import org.litebridge.db.spi.query.Condition;
//import org.litebridge.db.spi.query.ConditionGroup;
//import org.litebridge.db.spi.query.Join;
//import org.litebridge.db.spi.query.LogicCondition;
//import org.litebridge.db.spi.query.LogicConditionGroup;
//import org.litebridge.db.spi.query.LogicOperator;
//import org.litebridge.db.spi.query.Operator;
//import org.litebridge.db.spi.query.Select;
//import org.litebridge.db.spi.update.Delete;
//import org.litebridge.db.spi.update.Insert;
//import org.litebridge.db.spi.update.Update;
//
//import java.util.Collections;
//import java.util.List;
//import java.util.Optional;
//
//import static org.junit.jupiter.api.Assertions.assertEquals;
//import static org.mockito.Mockito.mock;
//import static org.mockito.Mockito.when;
//
//class OracleColumnIdentifierGeneratorTest {
//
//    private final OracleColumnIdentifierGenerator generator = new OracleColumnIdentifierGenerator();
//
//    @Test
//    void createSelectColumn_withoutSelect_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Operation operation = new Insert(table, Collections.emptyList(), Collections.emptyList(), false);
//
//        // When
//        final String result = generator.createSelectColumn(column, operation, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createColumnRef_withoutSelect_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Operation operation = new Delete(table, new ConditionGroup(Collections.emptyList()));
//
//        // When
//        final String result = generator.createColumnRef(column, operation, ClauseType.WHERE);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createColumnRef_withSelect_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Select select = mock(Select.class);
//        when(select.joins()).thenReturn(Collections.emptyList());
//
//        // When
//        final String result = generator.createColumnRef(column, select, ClauseType.WHERE);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withEmptyJoins_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withJoinWithoutUsing_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(column, generator), Operator.EQ, "TEST_VALUE"));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, new ColumnIdentifierGenerator())),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUsingForDifferentSelectColumn_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        // When
//        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUsingForSameSelectColumnButUnrelatedTable_usesDefaultTableQualifier() {
//        // Given
//        final Table unrelatedTable = new Table("UNRELATED_TABLE", null);
//        final Column column = new Column(unrelatedTable, "TEST_COLUMN");
//
//        // When
//        final String result = generator.createSelectColumn(column, mock(Select.class), ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("UNRELATED_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUsingForSameSelectColumnAndTable_doesNotUseTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(column, generator), Operator.USING, null));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUsingForSameSelectColumnButFromOtherSideOfJoin_doesNotUseTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table joinedTable = new Table("JOINED_TABLE", null);
//        final Column column = new Column(joinedTable, "TEST_COLUMN");
//        final Column tableColumn = new Column(table, "TEST_COLUMN");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(tableColumn, generator), Operator.USING, null));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withMultipleJoins_hitsSecondJoin() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table joinedTable1 = new Table("JOINED_TABLE1", null);
//        final Table joinedTable2 = new Table("JOINED_TABLE2", null);
//        final Column column = new Column(joinedTable2, "TEST_COLUMN");
//        final Column tableColumn = new Column(table, "TEST_COLUMN");
//
//        final List<LogicCondition> join1Conditions = List.of(new LogicCondition(new SelectColumn(tableColumn, generator), Operator.EQ, "OTHER"));
//        final List<LogicCondition> join2Conditions = List.of(new LogicCondition(new SelectColumn(tableColumn, generator), Operator.USING, null));
//
//        final Join join1 = new Join(joinedTable1, new ConditionGroup(join1Conditions));
//        final Join join2 = new Join(joinedTable2, new ConditionGroup(join2Conditions));
//
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join1, join2),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_joinUsing_omitTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", "T");
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Column columnWithAlias = new Column(table, "TEST_COLUMN", "C");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(columnWithAlias, generator), Operator.USING, null));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUsingForSameSelectColumnButFromOtherSideOfJoinTable_doesNotUseTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table joinedTable = new Table("JOINED_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Column joinColumn = new Column(joinedTable, "TEST_COLUMN");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(joinColumn, generator), Operator.USING, null));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withIncludeAlias_returnsColumnWithAlias() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN", "MY_ALIAS");
//        final Column tableColumn = new Column(table, "TEST_COLUMN");
//        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(tableColumn, generator), Operator.USING, null));
//        final Join join = new Join(table, new ConditionGroup(conditions));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN MY_ALIAS", result);
//    }
//
//    @Test
//    void createAlias_validAliasDeclaration() {
//        // Given
//        final String alias = "MY_ALIAS";
//
//        // When
//        String result = generator.createAliasDeclaration(alias);
//
//        // Then
//        assertEquals("MY_ALIAS", result);
//    }
//
//    @Test
//    void createSelectColumn_withJoinConditionsSizeNotOneAndSubgroupsNotEmpty_continuesLoop() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final List<LogicCondition> logicConditions = List.of(
//                new LogicCondition(mock(SelectExpression.class), Operator.EQ, "V1"),
//                new LogicCondition(mock(SelectExpression.class), Operator.EQ, "V2")
//        );
//        final List<LogicConditionGroup> subgroups = List.of(
//                new LogicConditionGroup(LogicOperator.AND, new ConditionGroup(Collections.emptyList()))
//        );
//        final ConditionGroup conditionGroup = new ConditionGroup(logicConditions, subgroups);
//        final Join join = new Join(table, conditionGroup);
//
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withNonColumnExpression_continuesLoop() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final SelectExpression nonColumnExpression = mock(SelectExpression.class);
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(nonColumnExpression, Operator.USING, null));
//
//        final ConditionGroup conditions = new ConditionGroup(List.of(logicCondition));
//        final Join join = new Join(table, conditions);
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnNameButDifferentTable_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table otherTable = new Table("OTHER_TABLE", null);
//        final Table unrelatedTable = new Table("UNRELATED_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final Column otherColumn = new Column(otherTable, "TEST_COLUMN");
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(otherColumn);
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(unrelatedTable, new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                unrelatedTable,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        // OracleColumnIdentifierGenerator.java:72: columnExpression.column().name().equals(column.name())
//        // If names match, it sets applyTableQualifier = false.
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withDifferentColumnName_usesDefaultTableQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Column otherColumn = new Column(table, "OTHER_COLUMN");
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(otherColumn);
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(table, new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnExactlyOnSelectTable_removesQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        // equalsColumnOnlyIgnoreAlias(column) will be true if it's the same column object or same name/alias.
//        // Actually I'll use a real SelectColumn which implements ColumnExpression.
//        final SelectExpression colExpr = new SelectColumn(column, generator);
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(colExpr, Operator.USING, null));
//
//        final Join join = new Join(new Table("OTHER", null), new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createColumnRef_withSelectAndJoinUsing_removesQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Column otherColumn = new Column(new Table("OTHER", null), "TEST_COLUMN");
//
//        final LogicCondition logicCondition = new LogicCondition(new SelectColumn(otherColumn, generator), Operator.USING, null);
//
//        final Join join = new Join(new Table("OTHER", null), new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createColumnRef(column, select, ClauseType.WHERE);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withColumnMatchingJoinedTable_removesQualifier() {
//        // Given
//        final Table selectTab = new Table("SELECT_TAB", null);
//        final Table joinTab = new Table("JOIN_TAB", null);
//        final Column column = new Column(joinTab, "TEST_COLUMN");
//
//        final LogicCondition logicCondition = new LogicCondition(new SelectColumn(column, generator), Operator.USING, null);
//
//        final Join join = new Join(joinTab, new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                selectTab,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withNonSelectClauseType_doesNotAddAlias() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN", "MY_ALIAS");
//        final Select select = new Select(table, Collections.emptyList(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.WHERE, true);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withJoinConditionsSizeOneAndSubgroupsNotEmpty_doesNotContinue() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final LogicCondition logicCondition = new LogicCondition(mock(SelectExpression.class), Operator.EQ, "V1");
//        final List<LogicConditionGroup> subgroups = List.of(
//                new LogicConditionGroup(LogicOperator.AND, new ConditionGroup(Collections.emptyList()))
//        );
//        final ConditionGroup conditionGroup = new ConditionGroup(List.of(logicCondition), subgroups);
//        final Join join = new Join(table, conditionGroup);
//
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withJoinConditionsSizeZeroAndSubgroupsNotEmpty_doesNotContinue() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final List<LogicConditionGroup> subgroups = List.of(
//                new LogicConditionGroup(LogicOperator.AND, new ConditionGroup(Collections.emptyList()))
//        );
//        final ConditionGroup conditionGroup = new ConditionGroup(Collections.emptyList(), subgroups);
//        final Join join = new Join(table, conditionGroup);
//
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnNameInUsing_removesQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(new Column(new Table("OTHER", null), "TEST_COLUMN"));
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(new Table("OTHER", null), new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        // Match by name only (line 72)
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnExactlyOnJoinTable_removesQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table joinTable = new Table("JOIN_TABLE", null);
//        final Column column = new Column(joinTable, "TEST_COLUMN");
//
//        final Column mockColumn = mock(Column.class);
//        when(mockColumn.name()).thenReturn("DIFFERENT_NAME");
//        when(mockColumn.equalsColumnOnlyIgnoreAlias(column)).thenReturn(true);
//        when(mockColumn.table()).thenReturn(joinTable);
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(mockColumn);
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(joinTable, new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnExactlyOnSelectTableAndMockNameMismatch_removesQualifier() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final Column mockColumn = mock(Column.class);
//        when(mockColumn.name()).thenReturn("DIFFERENT_NAME");
//        when(mockColumn.equalsColumnOnlyIgnoreAlias(column)).thenReturn(true);
//        when(mockColumn.table()).thenReturn(table);
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(mockColumn);
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(new Table("OTHER", null), new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        // Line 72 false, line 74 true, line 75 select.table matches
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMultipleJoins_removesQualifierIfAnyMatches() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        final LogicCondition cond1 = new LogicCondition(mock(SelectExpression.class), Operator.EQ, "V1");
//        final Join join1 = new Join(new Table("OTHER1", null), new ConditionGroup(List.of(cond1)));
//
//        final Column otherColumn = new Column(new Table("OTHER2", null), "TEST_COLUMN");
//        final LogicCondition cond2 = new LogicCondition(new SelectColumn(otherColumn, generator), Operator.USING, null);
//        final Join join2 = new Join(new Table("OTHER2", null), new ConditionGroup(List.of(cond2)));
//
//        final Select select = new Select(
//                table,
//                List.of(new SelectColumn(column, generator)),
//                List.of(join1, join2),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_COLUMN", result);
//    }
//
//    @Test
//    void shouldApplyTableQualifier_withMatchingColumnNameButNoTableMatch_returnsTrue() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Table unrelatedTable = new Table("UNRELATED", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//
//        // We want line 72 false, so names must NOT match.
//        // We want line 74 true, so we use mocked column.
//        // We want line 75 false, so neither select.table nor join.table match.
//
//        final Column mockColumn = mock(Column.class);
//        when(mockColumn.name()).thenReturn("DIFFERENT_NAME");
//        when(mockColumn.equalsColumnOnlyIgnoreAlias(column)).thenReturn(true);
//        when(mockColumn.table()).thenReturn(unrelatedTable);
//
//        final ColumnExpression columnExpression = mock(ColumnExpression.class);
//        when(columnExpression.column()).thenReturn(mockColumn);
//
//        final LogicCondition logicCondition = new LogicCondition(LogicOperator.NOOP, new Condition(columnExpression, Operator.USING, null));
//
//        final Join join = new Join(new Table("OTHER", null), new ConditionGroup(List.of(logicCondition)));
//        final Select select = new Select(
//                new Table("ANOTHER", null),
//                List.of(new SelectColumn(column, generator)),
//                List.of(join),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty(),
//                Collections.emptyList(),
//                Optional.empty());
//
//        // When
//        final String result = generator.createSelectColumn(column, select, ClauseType.SELECT, false);
//
//        // Then
//        // Should keep qualifier
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createSelectColumn_withUpdateOperation_usesSuper() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN");
//        final Update update = new Update(table, Collections.emptyList(), new ConditionGroup(Collections.emptyList()));
//
//        // When
//        final String result = generator.createSelectColumn(column, update, ClauseType.SELECT, false);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//
//    @Test
//    void createColumnRef_withAliasedColumnAndNonWhereClause_returnsAlias() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN", "MY_ALIAS");
//        final Operation operation = new Select(table, Collections.emptyList(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty());
//
//        // When
//        final String result = generator.createColumnRef(column, operation, ClauseType.SELECT);
//
//        // Then
//        assertEquals("MY_ALIAS", result);
//    }
//
//    @Test
//    void createColumnRef_withAliasedColumnAndWhereClause_returnsQualifiedName() {
//        // Given
//        final Table table = new Table("TEST_TABLE", null);
//        final Column column = new Column(table, "TEST_COLUMN", "MY_ALIAS");
//        final Operation operation = new Select(table, Collections.emptyList(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty(), Collections.emptyList(), Optional.empty());
//
//        // When
//        final String result = generator.createColumnRef(column, operation, ClauseType.WHERE);
//
//        // Then
//        assertEquals("TEST_TABLE.TEST_COLUMN", result);
//    }
//}