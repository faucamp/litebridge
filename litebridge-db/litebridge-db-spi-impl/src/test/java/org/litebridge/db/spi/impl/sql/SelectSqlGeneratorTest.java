package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.function.SelectColumn;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.OrderBy;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SelectSqlGeneratorTest {

    @Mock
    private TypeConverter typeConverter;
    @Mock
    private BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData;
    private SelectSqlGenerator selectSqlGenerator;

    @BeforeEach
    void beforeEach() {
        selectSqlGenerator = new SelectSqlGenerator(typeConverter, new ColumnIdentifierGenerator(), ensureTableMetaData);
    }

    @Test
    void createJoin_withNoAlias() throws Exception {
        // Given
        final Column column = createTestColumn();
        final Table table = column.table();

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(ensureTableMetaData.apply(eq(table), any(ConnectionProvider.class))).thenReturn(tableMetaData);
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");
        final ColumnExpression columnExpression = new SelectColumn(column, selectSqlGenerator.columnIdentifierGenerator);

        final LogicCondition condition = new LogicCondition(columnExpression, Operator.EQ, "testValue");
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition));

        final Join join = new Join(table, conditionGroup);

        // When
        final PreparedSql result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN TEST_SCHEMA.TEST_TABLE ON TEST_TABLE.TEST_COLUMN = ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createJoin_multipleConditions() throws Exception {
        // Given
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE", "t2");
        final Column column1 = createTestColumn("TEST_PK", table);
        final Column column2 = createTestColumn("TEST_COLUMN", table);
        final ColumnExpression columnExression1 = new SelectColumn(column1, selectSqlGenerator.columnIdentifierGenerator);
        final ColumnExpression columnExression2 = new SelectColumn(column2, selectSqlGenerator.columnIdentifierGenerator);

        final LogicCondition condition1 = new LogicCondition(columnExression1, Operator.EQ, "value1");
        final LogicCondition condition2 = new LogicCondition(LogicOperator.AND, new Condition(columnExression2, Operator.NEQ, "value2"));
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition1, condition2));

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(ensureTableMetaData.apply(eq(table), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        final ColumnMetaData pkColumnMetaData = mock(ColumnMetaData.class);
        when(pkColumnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_PK")).thenReturn(pkColumnMetaData);

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);

        when(typeConverter.convert(anyString(), eq(Types.VARCHAR))).then(i -> i.getArgument(0));

        final Join join = new Join(table, conditionGroup);

        // When
        final PreparedSql result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN TEST_SCHEMA.TEST_TABLE AS t2 ON t2.TEST_PK = ? AND t2.TEST_COLUMN <> ?", result.sql());
        assertEquals(2, result.bindValues().size());
        assertEquals("value1", result.bindValues().get(0).value());
        assertEquals("value2", result.bindValues().get(1).value());
    }

    @Test
    void appendLimitClause_bothLimitAndOffset() {
        // Given
        final Limit limit = new Limit(Optional.of(100), Optional.of(50));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100 OFFSET 50", sql.toString());
    }

    @Test
    void appendLimitClause_onlyLimit() {
        // Given
        final Limit limit = new Limit(Optional.of(100), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST LIMIT 100", sql.toString());
    }

    @Test
    void appendLimitClause_onlyOffset() {
        // Given
        final Limit limit = new Limit(Optional.empty(), Optional.of(50));
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST OFFSET 50", sql.toString());
    }

    @Test
    void prepareSql_complex() {
        // Given
        final Table table = new Table("TEST_TABLE", "t1");
        final Column col1 = new Column(table, "COL1");
        final Column col2 = new Column(table, "COL2");

        final SelectColumn selectCol1 = new SelectColumn(col1, selectSqlGenerator.columnIdentifierGenerator);

        final Table joinTable = new Table("JOIN_TABLE", "j1");
        final Column joinCol = new Column(joinTable, "JCOL");
        final LogicCondition condition = new LogicCondition(new SelectColumn(joinCol, selectSqlGenerator.columnIdentifierGenerator), Operator.EQ, "val");
        final ConditionGroup conditionGroup = new ConditionGroup(List.of(condition), Collections.emptyList());
        final Join join = new Join(joinTable, conditionGroup);

        final List<LogicCondition> whereConditions = List.of(new LogicCondition(new SelectColumn(col2, selectSqlGenerator.columnIdentifierGenerator), Operator.GT, 10));
        final ConditionGroup where = new ConditionGroup(whereConditions);

        final List<OrderBy> orderBy = List.of(new OrderBy(selectCol1, false));
        final Limit limit = new Limit(Optional.of(10), Optional.of(5));

        final List<SelectExpression> groupBy = List.of(selectCol1);
        final List<LogicCondition> havingConditions = List.of(new LogicCondition(new SelectColumn(col1, selectSqlGenerator.columnIdentifierGenerator), Operator.NEQ, "foo"));
        final ConditionGroup having = new ConditionGroup(havingConditions);

        final Select select = new Select(
                table,
                new ArrayList<>(List.of(selectCol1, mock(SelectExpression.class))), // Test non-AliasedColumnExpression
                new ArrayList<>(List.of(join)),
                Optional.of(where),
                groupBy,
                Optional.of(having),
                new ArrayList<>(orderBy),
                Optional.of(limit)
        );

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(ensureTableMetaData.apply(any(), any())).thenReturn(tableMetaData);
        final ColumnMetaData cmd = mock(ColumnMetaData.class);
        when(cmd.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column(anyString())).thenReturn(cmd);
        when(typeConverter.convert(any(), anyInt())).then(i -> i.getArgument(0));
        // when(typeConverter.getDbDataType(any())).thenReturn(Types.INTEGER);

        // Mock the non-AliasedColumnExpression
        when(select.expressions().get(1).toSql(any(Operation.class), any(ClauseType.class))).thenReturn("1");

        // When
        final PreparedSql result = selectSqlGenerator.prepareSql(select, mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT t1.COL1, 1 FROM TEST_TABLE AS t1 JOIN JOIN_TABLE AS j1 ON j1.JCOL = ? WHERE t1.COL2 > ? GROUP BY t1.COL1 HAVING t1.COL1 <> ? ORDER BY t1.COL1 DESC LIMIT 10 OFFSET 5", result.sql());
        assertEquals(3, result.bindValues().size());
    }

    @Test
    void prepareSql_emptyExpressions() {
        // Given
        final Table table = new Table("TEST_TABLE");
        final Select select = new Select(
                table,
                Collections.emptyList(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty(),
                Collections.emptyList(),
                Optional.empty()
        );

        // When
        final PreparedSql result = selectSqlGenerator.prepareSql(select, mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT * FROM TEST_TABLE", result.sql());
    }

    @Test
    void createJoin_using() {
        // Given
        final Table table = new Table("JOIN_TABLE");
        final Column column = new Column(table, "COL1");
        final List<LogicCondition> conditions = List.of(new LogicCondition(new SelectColumn(column, selectSqlGenerator.columnIdentifierGenerator), Operator.USING, null));
        final Join join = new Join(table, new ConditionGroup(conditions));

        // When
        final PreparedSql result = selectSqlGenerator.createJoin(join, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals(" JOIN JOIN_TABLE USING (COL1)", result.sql());
    }
}