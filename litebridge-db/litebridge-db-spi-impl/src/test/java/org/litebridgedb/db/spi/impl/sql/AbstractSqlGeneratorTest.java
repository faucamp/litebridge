package org.litebridgedb.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.TableMetaData;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.LiteralExpression;
import org.litebridgedb.db.spi.expression.SelectExpression;
import org.litebridgedb.db.spi.expression.SelectReference;
import org.litebridgedb.db.spi.expression.SubselectExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.function.SelectColumn;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.sql.BindValue;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;
import org.litebridgedb.db.spi.tx.TransactionManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Types;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.litebridgedb.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridgedb.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AbstractSqlGeneratorTest {

    @Mock
    private TableMetaData tableMetaData;
    @Mock
    private TypeConverter typeConverter;
    private AbstractSqlGenerator sqlGenerator;

    @BeforeEach
    void beforeEach() {
        sqlGenerator = new TestSqlGenerator(typeConverter);
    }

    @Test
    void mapOperator_eq() {
        // Given
        final Operator operator = Operator.EQ;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("=", result);
    }

    @Test
    void mapOperator_neq() {
        // Given
        final Operator operator = Operator.NEQ;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("<>", result);
    }

    @Test
    void mapOperator_gt() {
        // Given
        final Operator operator = Operator.GT;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals(">", result);
    }

    @Test
    void mapOperator_gte() {
        // Given
        final Operator operator = Operator.GTE;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals(">=", result);
    }

    @Test
    void mapOperator_lt() {
        // Given
        final Operator operator = Operator.LT;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("<", result);
    }

    @Test
    void mapOperator_lte() {
        // Given
        final Operator operator = Operator.LTE;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("<=", result);
    }

    @Test
    void mapOperator_in() {
        // Given
        final Operator operator = Operator.IN;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("IN", result);
    }

    @Test
    void mapOperator_notIn() {
        // Given
        final Operator operator = Operator.NOT_IN;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("NOT IN", result);
    }

    @Test
    void mapOperator_isNull() {
        // Given
        final Operator operator = Operator.IS_NULL;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("IS NULL", result);
    }

    @Test
    void mapOperator_isNotNull() {
        // Given
        final Operator operator = Operator.IS_NOT_NULL;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("IS NOT NULL", result);
    }

    @Test
    void mapOperator_using() {
        // Given
        final Operator operator = Operator.USING;

        // When
        final String result = sqlGenerator.mapOperator(operator);

        // Then
        assertEquals("USING", result);
    }

    @Test
    void createCondition() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.EQ, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_isNull() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.IS_NULL);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NULL", result.sql());
        assertTrue(result.bindValues().isEmpty());
    }

    @Test
    void createCondition_isNotNull() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.IS_NOT_NULL);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NOT NULL", result.sql());
        assertTrue(result.bindValues().isEmpty());
    }

    @Test
    void createCondition_using() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.USING, null);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("USING (TEST_COLUMN)", result.sql());
        assertTrue(result.bindValues().isEmpty());
    }

    @Test
    void createCondition_withTableAlias() throws Exception {
        // Given
        final Column column = createTestColumn();
        column.table().setAlias("t1");
        final ColumnExpression columnExpression = new SelectColumn(column, sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(columnExpression, Operator.EQ, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("t1.TEST_COLUMN = ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_withOperatorGT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.GT, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN > ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_withOperatorGTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.GTE, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN >= ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_withOperatorLT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.LT, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN < ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_withOperatorLTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.LTE, "testValue");
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert("testValue", Types.VARCHAR)).thenReturn("testValue");

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN <= ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("testValue", result.bindValues().getFirst().value());
    }

    @Test
    void createCondition_withOperatorIn() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.IN, List.of("value1", "value2"));
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert(any(), anyInt())).then(i -> i.getArgument(0));

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (?, ?)", result.sql());
        assertEquals(2, result.bindValues().size());
        assertEquals("value1", result.bindValues().getFirst().value());
        assertEquals("value2", result.bindValues().get(1).value());
    }

    @Test
    void createCondition_withOperatorNotIn() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.NOT_IN, List.of("value1", "value2"));
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert(any(), anyInt())).then(i -> i.getArgument(0));

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN NOT IN (?, ?)", result.sql());
        assertEquals(2, result.bindValues().size());
        assertEquals("value1", result.bindValues().getFirst().value());
        assertEquals("value2", result.bindValues().get(1).value());
    }

    @Test
    void appendTable_withSchemaAndName() throws Exception {
        // Given
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");

        // When
        sqlGenerator.appendTable(sql, "TEST_SCHEMA", "TEST_TABLE");

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", sql.toString());
    }

    @Test
    void appendTable_withoutSchema() throws Exception {
        // Given
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");

        // When
        sqlGenerator.appendTable(sql, "", "TEST_TABLE");

        // Then
        assertEquals("SELECT * FROM TEST_TABLE", sql.toString());
    }

    @Test
    void ensureColumnMetaData() throws Exception {
        // Given
        final Column column = createTestColumn();
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.name()).thenReturn(column.name());
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);

        // When
        final ColumnMetaData result = sqlGenerator.ensureColumnMetaData(column, mock(TransactionManager.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_COLUMN", result.name());
    }

    @Test
    void createCondition_subselect() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Select subselect = mock(Select.class);
        final SubselectExpression subselectExpression = mock(SubselectExpression.class);
        final Condition condition = new Condition(column, Operator.IN, subselectExpression);
        final PreparedSql subselectSql = new PreparedSql("SELECT ID FROM OTHER", List.of(new BindValue(1, Types.INTEGER)));

        when(subselectExpression.toSql(any(), any())).thenReturn(subselectSql);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (SELECT ID FROM OTHER)", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals(1, result.bindValues().get(0).value());
    }

    @Test
    void createCondition_selectReference() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Column referencedColumn = createTestColumn();
        referencedColumn.table().setAlias("ref");
        final SelectReference selectReference = mock(SelectReference.class);
        when(selectReference.column()).thenReturn(referencedColumn);
        final Condition condition = new Condition(column, Operator.EQ, selectReference);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ref.TEST_COLUMN", result.sql());
        assertTrue(result.bindValues().isEmpty());
    }

    @Test
    void createCondition_nonColumnLhs() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        when(lhs.toSql(any())).thenReturn("1");
        final Condition condition = new Condition(lhs, Operator.EQ, "val");

        when(typeConverter.getSqlDataType(any())).thenReturn(Types.VARCHAR);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("1 = ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals("val", result.bindValues().get(0).value());
    }

    @Test
    void createCondition_nullRhs() {
        // Given
        final ColumnExpression column = createSelectColumn(sqlGenerator.columnIdentifierGenerator);
        final Condition condition = new Condition(column, Operator.EQ, new LiteralExpression(null));
        final ColumnMetaData columnMetaData = mock(ColumnMetaData.class);
        when(columnMetaData.getDataType()).thenReturn(Types.VARCHAR);
        when(tableMetaData.column("TEST_COLUMN")).thenReturn(columnMetaData);
        when(typeConverter.convert(null, Types.VARCHAR)).thenReturn(null);

        // When
        final PreparedSql result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result.sql());
        assertEquals(1, result.bindValues().size());
        assertEquals(null, result.bindValues().get(0).value());
        assertEquals(Types.VARCHAR, result.bindValues().get(0).sqlDataType());
    }

    @Test
    void getExpressionValue_unsupported() {
        // Given
        final SelectExpression unsupported = mock(SelectExpression.class);

        // When & Then
        try {
            sqlGenerator.getExpressionValue(unsupported);
        } catch (UnsupportedOperationException e) {
            assertTrue(e.getMessage().contains("Unsupported select expression"));
        }
    }

    @Test
    void createCondition_using_exception() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        final Condition condition = new Condition(lhs, Operator.USING, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class)));
    }

    private class TestSqlGenerator extends AbstractSqlGenerator {
        public TestSqlGenerator(final TypeConverter typeConverter) {
            super(typeConverter, new ColumnIdentifierGenerator(), (table, connectionProvider) -> tableMetaData);
        }
    }
}