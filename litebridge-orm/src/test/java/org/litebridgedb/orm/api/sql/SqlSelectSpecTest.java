package org.litebridgedb.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.expression.SqlFunctionRegistry;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.JoinSpec;
import org.litebridgedb.orm.api.select.model.LimitSpec;
import org.litebridgedb.orm.api.select.model.OrderBySpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.TestColumnExpression;
import org.litebridgedb.orm.expression.TestColumnExpressionFactory;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SqlSelectSpecTest {

    @Test
    void getTable() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);

        // When
        final Table result = sqlSelectSpec.getTable();

        // Then
        assertSame(table, result);
    }

    @Test
    void getTable_null() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));

        // When/Then
        assertThrows(IllegalStateException.class, () -> sqlSelectSpec.getTable());
    }

    @Test
    void setExpressions() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final List<ExpressionSpec> result = sqlSelectSpec.getExpressions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(SelectColumnSpec.class, result.getFirst());
        assertSame(column, ((SelectColumnSpec) result.getFirst()).column());
    }

    @Test
    void addExpressions() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        sqlSelectSpec.addExpressions(List.of(new SelectColumnSpec(column)));
        final List<ExpressionSpec> result = sqlSelectSpec.getExpressions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertInstanceOf(SelectColumnSpec.class, result.getFirst());
        assertSame(column, ((SelectColumnSpec) result.getFirst()).column());
    }

    @Test
    void getJoins() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final JoinSpec joinSpec = sqlSelectSpec.newJoinSpec("TEST_SCHEMA.TEST_TABLE");

        // When
        sqlSelectSpec.setJoins(List.of(joinSpec));
        final List<JoinSpec> result = sqlSelectSpec.getJoins();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(joinSpec, result.getFirst());
    }

    @Test
    void newJoinSpec() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));

        // When
        final JoinSpec result = sqlSelectSpec.newJoinSpec("TEST_SCHEMA.TEST_TABLE2");

        // Then
        assertNotNull(result);
        assertNotNull(sqlSelectSpec.getJoins());
        assertEquals(1, sqlSelectSpec.getJoins().size());
        assertSame(result, sqlSelectSpec.getJoins().getFirst());
    }

    @Test
    void newJoinSpec_noSchema() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));

        // When
        final JoinSpec result = sqlSelectSpec.newJoinSpec("TEST_TABLE2");

        // Then
        assertNotNull(result);
        assertNotNull(sqlSelectSpec.getJoins());
        assertEquals(1, sqlSelectSpec.getJoins().size());
        assertSame(result, sqlSelectSpec.getJoins().getFirst());
    }

    @Test
    void setWhereConditions() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);

        // When
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));
        final List<ConditionSpec> result = sqlSelectSpec.getWhereConditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
    }

    @Test
    void newWhereCondition() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);

        // When
        final ConditionSpec result = sqlSelectSpec.newWhereCondition(column);

        // Then
        assertNotNull(result);
        assertNotNull(sqlSelectSpec.getWhereConditions());
        assertEquals(1, sqlSelectSpec.getWhereConditions().size());
        assertSame(result, sqlSelectSpec.getWhereConditions().getFirst());
    }

    @Test
    void setOrderBys() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});

        // When
        sqlSelectSpec.setOrderBys(List.of(orderBySpec));
        final List<OrderBySpec> result = sqlSelectSpec.getOrderBys();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(orderBySpec, result.getFirst());
    }

    @Test
    void newOrderBy() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));

        // When
        final OrderBySpec result = sqlSelectSpec.newOrderBy("TEST_COLUMN");

        // Then
        assertNotNull(result);
        assertNotNull(sqlSelectSpec.getOrderBys());
        assertEquals(1, sqlSelectSpec.getOrderBys().size());
        assertSame(result, sqlSelectSpec.getOrderBys().getFirst());
    }

    @Test
    void setLimit() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        sqlSelectSpec.setOrderBys(List.of(orderBySpec));
        final LimitSpec limitSpec = new LimitSpec();
        limitSpec.setOffset(100);
        limitSpec.setLimit(200);

        // When
        sqlSelectSpec.setLimit(limitSpec);
        final LimitSpec result = sqlSelectSpec.getLimit();

        // Then
        assertSame(limitSpec, result);
    }

    @Test
    void ensureLimit() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        sqlSelectSpec.setOrderBys(List.of(orderBySpec));

        // When
        final LimitSpec result = sqlSelectSpec.ensureLimit();

        // Then
        assertNotNull(result);
    }

    @Test
    void setDtoAlias() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        sqlSelectSpec.setDtoAlias(TestDto.class, "TEST_ALIAS");

        // When
        final String result = sqlSelectSpec.getDtoAlias(TestDto.class);

        // Then
        assertEquals("TEST_ALIAS", result);
    }

    @Test
    void getDtoAlias_null() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));

        // When
        final String result = sqlSelectSpec.getDtoAlias(TestDto.class);

        // Then
        assertNull(result);
    }

    @Test
    void toSelect() {
        // Given
        final SqlFunctionRegistry sqlFunctionRegistry = mock(SqlFunctionRegistry.class);
        when(sqlFunctionRegistry.selectColumnFactory()).thenReturn(new TestColumnExpressionFactory());
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(sqlFunctionRegistry);
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        sqlSelectSpec.setExpressions(List.of(new SelectColumnSpec(column)));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        sqlSelectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        sqlSelectSpec.setOrderBys(List.of(orderBySpec));
        final LimitSpec limitSpec = new LimitSpec();
        limitSpec.setOffset(100);
        limitSpec.setLimit(200);
        sqlSelectSpec.setLimit(limitSpec);

        // When
        final Select result = sqlSelectSpec.toSelect();

        // Then
        assertNotNull(result);
        assertEquals(table, result.table());
        assertNotNull(result.expressions());
        assertEquals(1, result.expressions().size());
        assertInstanceOf(TestColumnExpression.class, result.expressions().getFirst());
        assertEquals(column, ((TestColumnExpression) result.expressions().getFirst()).column());
    }

    @Test
    void toSelect_expressionsNotSet() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        sqlSelectSpec.setTable(table);

        // When
        final Select result = sqlSelectSpec.toSelect();

        // Then
        assertNotNull(result);
        assertEquals(table, result.table());
        assertNotNull(result.expressions());
        assertTrue(result.expressions().isEmpty());
    }

    @Test
    void toSelect_tableNotSet() {
        // Given
        final SqlSelectSpec sqlSelectSpec = new SqlSelectSpec(mock(SqlFunctionRegistry.class));

        // When/Then
        assertThrows(IllegalStateException.class, sqlSelectSpec::toSelect);
    }

    private static class TestDto {
    }
}