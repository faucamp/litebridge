package org.litebridge.orm.api.select.model;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Operator;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

class JoinSpecTest {

    @Test
    void table() {
        // Given
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");

        // When
        final Table table = joinSpec.table();

        // Then
        assertNotNull(table);
        assertEquals("TEST_SCHEMA", table.schema());
        assertEquals("TEST_TABLE", table.name());
    }

    @Test
    void newCondition() {
        // Given
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");
        final Table table = joinSpec.table();

        // When
        final ConditionSpec conditionSpec = joinSpec.newCondition(new Column(table, "TEST_COLUMN"));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);
        final List<ConditionSpec> result = joinSpec.conditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
    }

    @Test
    void using() {
        // Given
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");
        final Table table = joinSpec.table();

        // When
        final ConditionSpec conditionSpec = joinSpec.using("TEST_COLUMN");
        final List<ConditionSpec> result = joinSpec.conditions();

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertSame(conditionSpec, result.getFirst());
    }

    @Test
    void toJoin() {
        // Given
        final JoinSpec joinSpec = new JoinSpec("TEST_SCHEMA", "TEST_TABLE");
        final Table table = joinSpec.table();
        final ConditionSpec conditionSpec = joinSpec.newCondition(new Column(table, "TEST_COLUMN"));
        conditionSpec.setOperator(Operator.LT);
        conditionSpec.setValue(123);

        // When
        final Join result = joinSpec.toJoin();

        // Then
        assertNotNull(result);
        assertSame(table, result.table());
        assertNotNull(result.conditions());
        assertEquals(1, result.conditions().size());
        assertEquals(conditionSpec.toCondition(), result.conditions().getFirst());
    }
}