package org.litebridge.db.spi.impl.sql;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.AliasedExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConnectionProviderExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.generator.ColumnValueGenerator;
import org.litebridge.db.spi.impl.expression.LiteralExpressionImpl;
import org.litebridge.db.spi.math.MathOperator;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.Values;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.db.spi.update.UpdateColumn;
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
import static org.mockito.ArgumentMatchers.eq;
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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("USING (TEST_COLUMN)", result);
    }

    @Test
    void createCondition_withTableAlias() throws Exception {
        // Given
        final ColumnExpression columnExpression = createSelectColumnWithTableAlias("t1");
        final Condition condition = new Condition(columnExpression, Operator.EQ, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("\"t1\".TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_withOperatorGT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.GT, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN > ?", result);
    }

    @Test
    void createCondition_withOperatorGTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.GTE, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN >= ?", result);
    }

    @Test
    void createCondition_withOperatorLT() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.LT, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN < ?", result);
    }

    @Test
    void createCondition_withOperatorLTE() throws Exception {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.LTE, createLiteralExpression("testValue"));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN <= ?", result);
    }

    @Test
    void createCondition_withOperatorIn() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.IN, createLiteralExpression(List.of("value1", "value2")));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (?, ?)", result);
    }

    @Test
    void createCondition_withOperatorNotIn() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.NOT_IN, createLiteralExpression(List.of("value1", "value2")));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN NOT IN (?, ?)", result);
    }

    @Test
    void appendTable() throws Exception {
        // Given
        final StringBuilder sql = new StringBuilder("SELECT * FROM ");
        final Table table = new Table("TEST_SCHEMA.TEST_TABLE");

        // When
        sqlGenerator.appendTable(sql, table);

        // Then
        assertEquals("SELECT * FROM TEST_SCHEMA.TEST_TABLE", sql.toString());
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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (SELECT ID FROM OTHER)", result);
    }

    @Test
    void createCondition_selectReference() {
        // Given
        final ColumnExpression column = createSelectColumnWithTableAlias("ref");
        final Condition condition = new Condition(column, Operator.EQ, column);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("1 = ?", result);
    }

    @Test
    void createCondition_nullRhs() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.EQ, createLiteralExpression(null));

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void createCondition_using_exception() {
        // Given
        final SelectExpression lhs = mock(SelectExpression.class);
        final Condition condition = new Condition(lhs, Operator.USING, null);

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class)));
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
        sqlGenerator.appendConditionsAndSubgroups(sql, root, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

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
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IN (SUB_SQL)", result);
    }

    @Test
    void createCondition_exists_withNullLhs() {
        // Given
        final SubselectExpression subselectExpression = mock(SubselectExpression.class);
        when(subselectExpression.toSql(any(Operation.class), any(ConnectionProvider.class))).thenReturn("SELECT 1 FROM DUAL");
        final Condition condition = new Condition(null, Operator.EXISTS, subselectExpression);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("EXISTS (SELECT 1 FROM DUAL)", result);
    }

    @Test
    void createCondition_nullLhs_nonExists_throwsException() {
        // Given
        final Condition condition = new Condition(null, Operator.EQ, createLiteralExpression("val"));

        // When & Then
        assertThrows(IllegalArgumentException.class,
                () -> sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class)));
    }

    @Test
    void createCondition_isNull_and_isNotNull() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition isNullCond = new Condition(column, Operator.IS_NULL, null);
        final Condition isNotNullCond = new Condition(column, Operator.IS_NOT_NULL, null);

        // When
        final String nullResult = sqlGenerator.createCondition(isNullCond, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));
        final String notNullResult = sqlGenerator.createCondition(isNotNullCond, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN IS NULL", nullResult);
        assertEquals("TEST_TABLE.TEST_COLUMN IS NOT NULL", notNullResult);
    }

    @Test
    void createCondition_using_withValidRhs() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final Condition condition = new Condition(column, Operator.USING, column);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.JOIN, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("USING (TEST_TABLE.TEST_COLUMN)", result);
    }

    @Test
    void createCondition_rhsAliasedExpression() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final AliasedExpression aliased = mock(AliasedExpression.class);
        when(aliased.toSql(any(), eq(ClauseType.JOIN))).thenReturn("\"alias_col\"");
        final Condition condition = new Condition(column, Operator.EQ, aliased);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = \"alias_col\"", result);
    }

    @Test
    void createCondition_rhsGenericSelectExpression() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final SelectExpression genericExpr = mock(SelectExpression.class);
        final Condition condition = new Condition(column, Operator.EQ, genericExpr);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ?", result);
    }

    @Test
    void appendConditionsAndSubgroups_withOrOperator() {
        // Given
        final StringBuilder sql = new StringBuilder();
        final ColumnExpression col = createSelectColumn();
        final LogicCondition cond1 = new LogicCondition(col, Operator.EQ, createLiteralExpression("v1"));
        final LogicCondition cond2 = new LogicCondition(LogicOperator.OR, new Condition(col, Operator.EQ, createLiteralExpression("v2")));

        final ConditionGroup subGroup = new ConditionGroup(List.of(cond1));
        final LogicConditionGroup logicSubGroup = new LogicConditionGroup(LogicOperator.OR, subGroup);

        final ConditionGroup root = new ConditionGroup(
                List.of(cond1, cond2),
                List.of(logicSubGroup)
        );

        // When
        sqlGenerator.appendConditionsAndSubgroups(sql, root, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ? OR TEST_TABLE.TEST_COLUMN = ? OR (TEST_TABLE.TEST_COLUMN = ?)", sql.toString());
    }

    @Test
    void appendValues_multipleRowsAndColumns() {
        // Given
        final StringBuilder sql = new StringBuilder();
        final LabelGenerator lg = new LabelGenerator();
        final Values values = new Values(List.of(
                new LiteralExpressionImpl(1, "A", lg),
                new LiteralExpressionImpl(2, "B", lg)
        ), "src");

        // When
        sqlGenerator.appendValues(sql, values, mock(Select.class));

        // Then
        assertEquals("(VALUES (?, ?)) AS \"src\" (\"A\", \"B\")", sql.toString());
    }

    @Test
    void getColumnValueFragment_variations() {
        // Given
        final ColumnValueGenerator generator = () -> "GEN_SQL()";

        final UpdateColumn colWithGen = new UpdateColumn("id", generator, null);
        final UpdateColumn colWithMath = new UpdateColumn("count", null, MathOperator.ADD);
        final UpdateColumn colDefault = new UpdateColumn("name");

        // When & Then
        assertEquals("GEN_SQL()", sqlGenerator.getColumnValueFragment(colWithGen));
        assertEquals("count + ?", sqlGenerator.getColumnValueFragment(colWithMath));
        assertEquals("?", sqlGenerator.getColumnValueFragment(colDefault));
    }

    @Test
    void createCondition_notIn_plainSelectExpression() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final ColumnExpression rhsColumn = createSelectColumn();
        final Condition condition = new Condition(column, Operator.NOT_IN, rhsColumn);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN NOT IN (TEST_TABLE.TEST_COLUMN)", result);
    }

    @Test
    void createCondition_exists_plainSelectExpression() {
        // Given
        final ColumnExpression rhsColumn = createSelectColumn();
        final Condition condition = new Condition(null, Operator.EXISTS, rhsColumn);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("EXISTS (TEST_TABLE.TEST_COLUMN)", result);
    }

    @Test
    void createCondition_eq_subselectExpression() {
        // Given
        final ColumnExpression column = createSelectColumn();
        final SubselectExpression subselectExpression = mock(SubselectExpression.class);
        when(subselectExpression.toSql(any(Operation.class), any(ConnectionProvider.class))).thenReturn("SELECT 1");
        final Condition condition = new Condition(column, Operator.EQ, subselectExpression);

        // When
        final String result = sqlGenerator.createCondition(condition, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = (SELECT 1)", result);
    }

    @Test
    void mapOperator_allCases() {
        assertEquals("=", sqlGenerator.mapOperator(Operator.EQ));
        assertEquals(">", sqlGenerator.mapOperator(Operator.GT));
        assertEquals(">=", sqlGenerator.mapOperator(Operator.GTE));
        assertEquals("<", sqlGenerator.mapOperator(Operator.LT));
        assertEquals("<=", sqlGenerator.mapOperator(Operator.LTE));
        assertEquals("<>", sqlGenerator.mapOperator(Operator.NEQ));
        assertEquals("LIKE", sqlGenerator.mapOperator(Operator.LIKE));
        assertEquals("IN", sqlGenerator.mapOperator(Operator.IN));
        assertEquals("NOT IN", sqlGenerator.mapOperator(Operator.NOT_IN));
        assertEquals("IS NULL", sqlGenerator.mapOperator(Operator.IS_NULL));
        assertEquals("IS NOT NULL", sqlGenerator.mapOperator(Operator.IS_NOT_NULL));
        assertEquals("USING", sqlGenerator.mapOperator(Operator.USING));
        assertEquals("EXISTS", sqlGenerator.mapOperator(Operator.EXISTS));
    }

    @Test
    void appendConditionsAndSubgroups_subgroupNoopLogicOperator() {
        // Given
        final StringBuilder sql = new StringBuilder();
        final ColumnExpression col = createSelectColumn();
        final LogicCondition cond = new LogicCondition(col, Operator.EQ, createLiteralExpression("v1"));

        final ConditionGroup subGroup = new ConditionGroup(List.of(cond));
        final LogicConditionGroup logicSubGroup = new LogicConditionGroup(LogicOperator.NOOP, subGroup);

        final ConditionGroup root = new ConditionGroup(
                List.of(cond),
                List.of(logicSubGroup)
        );

        // When
        sqlGenerator.appendConditionsAndSubgroups(sql, root, ClauseType.WHERE, mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("TEST_TABLE.TEST_COLUMN = ? (TEST_TABLE.TEST_COLUMN = ?)", sql.toString());
    }

    @Test
    void appendTable_virtualTable() {
        // Given
        final StringBuilder sql = new StringBuilder();
        final VirtualTable virtualTable = new VirtualTable("V_TABLE");

        // When
        sqlGenerator.appendTable(sql, virtualTable);

        // Then
        assertEquals("V_TABLE", sql.toString());
    }

    private class TestSqlGenerator extends AbstractSqlGenerator {
        public TestSqlGenerator(final TypeConverter typeConverter) {
            super(new LabelGenerator(), new MathOperationGenerator(new LabelGenerator()), (table, connectionProvider) -> tableMetaData);
        }
    }
}