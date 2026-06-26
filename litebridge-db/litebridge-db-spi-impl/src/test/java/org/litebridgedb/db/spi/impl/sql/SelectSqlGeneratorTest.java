package org.litebridgedb.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Limit;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.litebridgedb.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
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

        final Condition condition = new Condition(columnExpression, Operator.EQ, "testValue");

        final Join join = new Join(table, List.of(condition));

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

        final Condition condition1 = new Condition(columnExression1, Operator.EQ, "value1");
        final Condition condition2 = new Condition(columnExression2, Operator.NEQ, "value2");

        final TableMetaData tableMetaData = mock(TableMetaData.class);
        when(ensureTableMetaData.apply(eq(table), any(ConnectionProvider.class))).thenReturn(tableMetaData);

        final ColumnMetaData pkColumnMetaData = mock(ColumnMetaData.class);
        when(pkColumnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_PK")).thenReturn(pkColumnMetaData);

        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);

        when(typeConverter.convert(anyString(), eq(Types.VARCHAR))).then(i -> i.getArgument(0));

        final Join join = new Join(table, List.of(condition1, condition2));

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
    void appendLimitClause_neitherLimitNorOffset() {
        // Given
        final Limit limit = new Limit(Optional.empty(), Optional.empty());
        final StringBuilder sql = new StringBuilder("SELECT * FROM TEST");

        // When
        selectSqlGenerator.appendLimitClause(limit, sql);

        // Then
        assertEquals("SELECT * FROM TEST", sql.toString());
    }
}