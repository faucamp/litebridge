package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConnectionProviderExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.litebridge.db.spi.impl.sql.TestUtil.createLiteralExpression;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumn;
import static org.litebridge.db.spi.impl.sql.TestUtil.createSelectColumnWithTableAlias;
import static org.litebridge.db.spi.impl.sql.TestUtil.createTestColumn;
import static org.mockito.ArgumentMatchers.any;
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
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.EQ, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_isNull() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.IS_NULL);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NULL", result);
    }

    @Test
    void createCondition_isNotNull() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.IS_NOT_NULL);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertNotNull(result);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NOT NULL", result);
    }

    @Test
    void createCondition_using() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn(new Column(VirtualTable.anonymous(), "TEST_COLUMN"));
        final Condition condition = new Condition(column, Operator.USING, column);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("USING (TEST_COLUMN)", result);
    }

    @Test
    void createCondition_withTableAlias() throws Exception {
        // Given
        final ColumnExpression columnExpression = createSelectColumnWithTableAlias("t1");
        final Condition condition = new Condition(columnExpression, Operator.EQ, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("\"t1\".TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_withOperatorGT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.GT, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN > ?", result);
    }

    @Test
    void createCondition_withOperatorGTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.GTE, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN >= ?", result);
    }

    @Test
    void createCondition_withOperatorLT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.LT, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN < ?", result);
    }

    @Test
    void createCondition_withOperatorLTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.LTE, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN <= ?", result);
    }

    @Test
    void createCondition_withOperatorIn() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.IN, createLiteralExpression(List.of("value1", "value2")));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (?, ?)", result);
    }

    @Test
    void createCondition_withOperatorNotIn() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.NOT_IN, createLiteralExpression(List.of("value1", "value2")));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN NOT IN (?, ?)", result);
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
        final ColumnExpression column = createSelectColumn();
        final SubselectExpression subselectExpression = mock(SubselectExpression.class);
        final Condition condition = new Condition(column, Operator.IN, subselectExpression);
        final String subselectSql = "SELECT ID FROM OTHER";

        when(subselectExpression.toSql(any(Operation.class), any(ConnectionProvider.class))).thenReturn(subselectSql);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (SELECT ID FROM OTHER)", result);
    }

    @Test
    void createCondition_selectReference() {
        // Given
        final ColumnExpression column = createSelectColumnWithTableAlias("ref");
        final Condition condition = new Condition(column, Operator.EQ, column);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("\"ref\".TEST_COLUMN = \"ref\".TEST_COLUMN", result);
    }

    @Test
    void createCondition_nonColumnLhs() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        when(lhs.toSql(any(Operation.class), any(ClauseType.class))).thenReturn("1");
        final Condition condition = new Condition(lhs, Operator.EQ, createLiteralExpression("val"));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("1 = ?", result);
    }

    @Test
    void createCondition_nullRhs() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.EQ, createLiteralExpression(null));

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_using_exception() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        final Condition condition = new Condition(lhs, Operator.USING, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class)));
    }

    @Test
    void appendConditionsAndSubgroups_nested() {
        // Given
        final StringBuilder sql = new StringBuilder();
        final ColumnExpression col1 = createSelectColumn();
        final LogicCondition cond1 = new LogicCondition(col1, Operator.EQ, createLiteralExpression("v1"));

        final ConditionGroup subGroup = new ConditionGroup(List.of(new LogicCondition(col1, Operator.NEQ, createLiteralExpression("v2"))));
        final LogicConditionGroup logicSubGroup = new LogicConditionGroup(LogicOperator.AND, subGroup);

        final ConditionGroup root = new ConditionGroup(
                List.of(cond1),
                List.of(logicSubGroup)
        );

        // When
        sqlGenerator.appendConditionsAndSubgroups(sql, root, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ? AND (TEST_TABLE.TEST_COLUMN <> ?)", sql.toString());
    }

    @Test
    void createCondition_in_connectionProviderExpression() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final ConnectionProviderExpression cpe = mock(ConnectionProviderExpression.class);
        final Condition condition = new Condition(column, Operator.IN, cpe);
        final String fragment = "SUB_SQL";

        when(cpe.toSql(any(Operation.class), any(ConnectionProvider.class))).thenReturn(fragment);

        // When
        final String result = sqlGenerator.createCondition(condition, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (SUB_SQL)", result);
    }

    private class TestSqlGenerator extends AbstractSqlGenerator {
        public TestSqlGenerator(final TypeConverter typeConverter) {
            super(new LabelGenerator(), new MathOperationGenerator(new LabelGenerator()), (table, connectionProvider) -> tableMetaData);
        }
    }
}