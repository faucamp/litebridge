package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.alias.AliasedTable;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.SelectColumn;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createLiteralExpression;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelectSqlGeneratorTest {

    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private SelectSqlGenerator selectSqlGenerator;

    @BeforeEach
    void beforeEach() {
        final LabelGenerator labelGenerator = new LabelGenerator();
        final MathOperationGenerator mathOperationGenerator = new MathOperationGenerator(labelGenerator);
        selectSqlGenerator = new SelectSqlGenerator(labelGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    @Test
    void createJoin_withNoAlias() throws Exception {
        // Given
        final ColumnExpression columnExpression = createSelectColumn();
        final Table table = columnExpression.column().table();

        final LogicCondition condition = new LogicCondition(columnExpression, Operator.EQ, createLiteralExpression("testValue"));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition));

        final Join join = new Join(Join.JoinType.INNER, table, conditionGroup);

        // When
        final String result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN TEST_SCHEMA.TEST_TABLE ON TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createJoin_multipleConditions() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG.TEST_SCHEMA.TEST_TABLE");
        final AliasedTable aliasedTable = new AliasedTable("t2", table);
        final Column column1 = createTestColumn("TEST_PK", table);
        final Column column2 = createTestColumn("TEST_COLUMN", table);
        final ColumnExpression columnExression1 = createSelectColumn(column1, null, "t2");
        final ColumnExpression columnExression2 = createSelectColumn(column2, null, "t2");

        final LogicCondition condition1 = new LogicCondition(columnExression1, Operator.EQ, createLiteralExpression("value1"));
        final LogicCondition condition2 = new LogicCondition(LogicOperator.AND, new Condition(columnExression2, Operator.NEQ, createLiteralExpression("value2")));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition1, condition2));

        final Join join = new Join(Join.JoinType.INNER, aliasedTable, conditionGroup);

        // When
        final String result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN TEST_SCHEMA.TEST_TABLE AS \"t2\" ON \"t2\".TEST_PK = ? AND \"t2\".TEST_COLUMN <> ?", result);
    }

    @Test
    void appendLimitClause_bothLimitAndOffset() {
        // Given
        final Limit limit = new Limit(100, 50);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100 OFFSET 50", sql.toString());
    }

    @Test
    void appendLimitClause_onlyLimit() {
        // Given
        final Limit limit = new Limit(100, null);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100", sql.toString());
    }

    @Test
    void appendLimitClause_onlyOffset() {
        // Given
        final Limit limit = new Limit(null, 50);
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST OFFSET 50", sql.toString());
    }

    @Test
    void generateSql_complex() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final AliasedTable aliasedTable = new AliasedTable("t1", table);
        final Column col1 = new Column(table, "COL1");
        final Column col2 = new Column(table, "COL2");
        final SelectColumn selectCol1 = createSelectColumn(col1, null, "t1");
        final SelectColumn selectCol2 = createSelectColumn(col2, null, "t1");

        final Table joinTable = new Table("JOIN_TABLE");
        final AliasedTable aliasedJoinTable = new AliasedTable("j1", joinTable);
        final Column joinCol = new Column(joinTable, "JCOL");
        final SelectColumn selectJoinCol = createSelectColumn(joinCol, null, "j1");
        final LogicCondition condition = new LogicCondition(selectJoinCol, Operator.EQ, createLiteralExpression("val"));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition), Collections.emptyList());
        final Join join = new Join(Join.JoinType.INNER, aliasedJoinTable, conditionGroup);

        final List<LogicCondition> whereConditions = List.of(new LogicCondition(selectCol2, Operator.GT, createLiteralExpression(10)));
        final ConditionGroup where = new ConditionGroup(whereConditions);

        final List<OrderBy> orderBy = List.of(new OrderBy(selectCol1, false));
        final Limit limit = new Limit(10, 5);

        final List<SelectExpression> groupBy = List.of(selectCol1);
        final List<LogicCondition> havingConditions = List.of(new LogicCondition(selectCol1, Operator.NEQ, createLiteralExpression("foo")));
        final ConditionGroup having = new ConditionGroup(havingConditions);

        final Select select = new Select(
                aliasedTable,
                new ArrayList<>(List.of(selectCol1, mock(SelectExpression.class))),
                new ArrayList<>(List.of(join)),
                where,
                groupBy,
                having,
                new ArrayList<>(orderBy),
                limit);

        // Mock the non-AliasedColumnExpression
        when(select.expressions().get(1).toSql(any(Operation.class), any(ClauseType.class))).thenReturn("<CUSTOM GENERATED SQL>");

        // When
        final String result = selectSqlGenerator.generateSql(select, mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT \"t1\".COL1, <CUSTOM GENERATED SQL> FROM TEST_TABLE AS \"t1\" JOIN JOIN_TABLE AS \"j1\" ON \"j1\".JCOL = ? WHERE \"t1\".COL2 > ? GROUP BY \"t1\".COL1 HAVING \"t1\".COL1 <> ? ORDER BY \"t1\".COL1 DESC LIMIT 10 OFFSET 5", result);
        //assertEquals("SELECT     t1.COL1, <CUSTOM GENERATED SQL> FROM TEST_TABLE AS     t1 JOIN JOIN_TABLE AS j1     ON j1    .JCOL = ? WHERE t1.COL2 > ? GROUP BY t1.COL1 HAVING t1.COL1 <> ? ORDER BY t1.COL1 DESC LIMIT 10 OFFSET 5", result);
    }

    @Test
    void generateSql_emptyExpressions() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Select select = new Select(
                table,
                Collections.emptyList(),
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                null,
                Collections.emptyList(),
                null);

        // When
        final String result = selectSqlGenerator.generateSql(select, mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT * FROM TEST_TABLE", result);
    }

    @Test
    void createJoin_using() {
        // Given
        final Table table = new Table("JOIN_TABLE");
        final Column column = new Column(VirtualTable.anonymous(), "COL1");
        final SelectColumn selectColumn = createSelectColumn(column);
        final List<LogicCondition> conditions = List.of(new LogicCondition(selectColumn, Operator.USING, selectColumn));
        final Join join = new Join(Join.JoinType.INNER, table, new ConditionGroup(conditions));

        // When
        final String result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN JOIN_TABLE USING (COL1)", result);
    }
}