package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SelectSpecTest {

    @Test
    void getTable() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);

        // When
        final Table result = selectSpec.getTable();

        // Then
        assertSame(table, result);
    }

    @Test
    void getTable_null() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();

        // When/Then
        assertThrows(IllegalStateException.class, () -> selectSpec.getTable());
    }

    @Test
    void setColumns() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        selectSpec.setColumns(List.of(column));
        final List<Column> result = selectSpec.getColumns();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(column, result.getFirst());
    }

    @Test
    void addColumns() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");

        // When
        selectSpec.addColumns(List.of(column));
        final List<Column> result = selectSpec.getColumns();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(column, result.getFirst());
    }

    @Test
    void getJoins() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final JoinSpec joinSpec = selectSpec.newJoinSpec("TEST_SCHEMA", "TEST_TABLE");

        // When
        selectSpec.setJoins(List.of(joinSpec));
        final List<JoinSpec> result = selectSpec.getJoins();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(joinSpec, result.getFirst());
    }

    @Test
    void newJoinSpec() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));

        // When
        final JoinSpec result = selectSpec.newJoinSpec("TEST_SCHEMA", "TEST_TABLE2");

        // Then
        assertNotNull(result);
        assertNotNull(selectSpec.getJoins());
        assertEquals(1, selectSpec.getJoins().size());
        assertSame(result, selectSpec.getJoins().getFirst());
    }

    @Test
    void newJoinSpec_noSchema() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));

        // When
        final JoinSpec result = selectSpec.newJoinSpec("TEST_TABLE2");

        // Then
        assertNotNull(result);
        assertNotNull(selectSpec.getJoins());
        assertEquals(1, selectSpec.getJoins().size());
        assertSame(result, selectSpec.getJoins().getFirst());
    }

    @Test
    void setWhereConditions() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);

        // When
        selectSpec.setWhereConditions(List.of(conditionSpec));
        final List<ConditionSpec> result = selectSpec.getWhereConditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
    }

    @Test
    void newWhereCondition() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);

        // When
        final ConditionSpec result = selectSpec.newWhereCondition(column);

        // Then
        assertNotNull(result);
        assertNotNull(selectSpec.getWhereConditions());
        assertEquals(1, selectSpec.getWhereConditions().size());
        assertSame(result, selectSpec.getWhereConditions().getFirst());
    }

    @Test
    void setOrderBys() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        selectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});

        // When
        selectSpec.setOrderBys(List.of(orderBySpec));
        final List<OrderBySpec> result = selectSpec.getOrderBys();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(orderBySpec, result.getFirst());
    }

    @Test
    void newOrderBy() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        selectSpec.setWhereConditions(List.of(conditionSpec));

        // When
        final OrderBySpec result = selectSpec.newOrderBy("TEST_COLUMN");

        // Then
        assertNotNull(result);
        assertNotNull(selectSpec.getOrderBys());
        assertEquals(1, selectSpec.getOrderBys().size());
        assertSame(result, selectSpec.getOrderBys().getFirst());
    }

    @Test
    void setLimit() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        selectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        selectSpec.setOrderBys(List.of(orderBySpec));
        final LimitSpec limitSpec = new LimitSpec();
        limitSpec.setOffset(100);
        limitSpec.setLimit(200);

        // When
        selectSpec.setLimit(limitSpec);
        final LimitSpec result = selectSpec.getLimit();

        // Then
        assertSame(limitSpec, result);
    }

    @Test
    void ensureLimit() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        selectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        selectSpec.setOrderBys(List.of(orderBySpec));

        // When
        final LimitSpec result = selectSpec.ensureLimit();

        // Then
        assertNotNull(result);
    }

    @Test
    void setDtoAlias() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        selectSpec.setDtoAlias(TestDto.class, "TEST_ALIAS");

        // When
        final String result = selectSpec.getDtoAlias(TestDto.class);

        // Then
        assertEquals("TEST_ALIAS", result);
    }

    @Test
    void getDtoAlias_null() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();

        // When
        final String result = selectSpec.getDtoAlias(TestDto.class);

        // Then
        assertNull(result);
    }

    @Test
    void toSelect() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);
        final Column column = new Column(table, "TEST_COLUMN");
        selectSpec.setColumns(List.of(column));
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditionSpec.setOperator(Operator.LTE);
        conditionSpec.setValue(123);
        selectSpec.setWhereConditions(List.of(conditionSpec));
        final OrderBySpec orderBySpec = new OrderBySpec(new String[]{"TEST_COLUMN"});
        selectSpec.setOrderBys(List.of(orderBySpec));
        final LimitSpec limitSpec = new LimitSpec();
        limitSpec.setOffset(100);
        limitSpec.setLimit(200);
        selectSpec.setLimit(limitSpec);

        // When
        final Select result = selectSpec.toSelect();

        // Then
        assertNotNull(result);
        assertEquals(table, result.table());
        assertNotNull(result.columns());
        assertEquals(1, result.columns().size());
        assertEquals(column, result.columns().getFirst());
    }

    @Test
    void toSelect_columnsNotSet() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();
        final Table table = new Table("TEST_CATALOG", "TEST_SCHEMA", "TEST_TABLE");
        selectSpec.setTable(table);

        // When
        final Select result = selectSpec.toSelect();

        // Then
        assertNotNull(result);
        assertEquals(table, result.table());
        assertNotNull(result.columns());
        assertTrue(result.columns().isEmpty());
    }

    @Test
    void toSelect_tableNotSet() {
        // Given
        final SelectSpec selectSpec = new SelectSpec();

        // When/Then
        assertThrows(IllegalStateException.class, selectSpec::toSelect);
    }

    private static class TestDto {
    }
}