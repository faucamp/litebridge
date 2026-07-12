package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.expression.ColumnExpressionSpec;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class SqlJoinClauseTest {

    @Test
    void constructorCreatesClause() {
        // Given
        final SqlJoinSpec joinSpec = new SqlJoinSpec(new Table("joined_table", null), mock(SelectExpressionMapper.class));
        final SqlSelector delegate = mock(SqlSelector.class);

        // When
        final SqlJoinClause result = new SqlJoinClause(joinSpec, delegate);

        // Then
        assertNotNull(result);
        assertTrue(joinSpec.currentConditionGroupSpec().conditions().isEmpty());
    }

    @Test
    void onAddsConditionForJoinTableColumnAndReturnsConditionClause() {
        // Given
        final Table joinTable = new Table("joined_table", null);
        final SqlJoinSpec joinSpec = new SqlJoinSpec(joinTable, mock(SelectExpressionMapper.class));
        final SqlSelector delegate = mock(SqlSelector.class);
        final SqlJoinClause clause = new SqlJoinClause(joinSpec, delegate);

        // When
        final SqlJoinConditionClause result = clause.on("joined_id");

        // Then
        assertNotNull(result);
        assertEquals(1, joinSpec.currentConditionGroupSpec().conditions().size());

        final ConditionSpec condition = joinSpec.currentConditionGroupSpec().conditions().getFirst().conditionSpec();
        final Column column = ((ColumnExpressionSpec) condition.getLhs()).getColumn();
        assertEquals(joinTable, column.table());
        assertEquals("joined_id", column.name());
        assertNull(condition.getOperator());
        assertNull(condition.getValue());
    }

    @Test
    void onConditionWithNonNullValueSetsOperatorAndValue() {
        // Given
        final Table joinTable = new Table("joined_table", null);
        final SqlJoinSpec joinSpec = new SqlJoinSpec(joinTable, mock(SelectExpressionMapper.class));
        final SqlSelector delegate = mock(SqlSelector.class);
        final SqlJoinClause clause = new SqlJoinClause(joinSpec, delegate);

        // When
        final SqlJoinConditionClauseTerminal terminal = clause.on("joined_id").eq("root_id");

        // Then
        assertNotNull(terminal);
        assertEquals(1, joinSpec.currentConditionGroupSpec().conditions().size());

        final ConditionSpec condition = joinSpec.currentConditionGroupSpec().conditions().getFirst().conditionSpec();
        final Column column = ((ColumnExpressionSpec) condition.getLhs()).getColumn();
        assertEquals(joinTable, column.table());
        assertEquals("joined_id", column.name());
        assertEquals(Operator.EQ, condition.getOperator());
        assertEquals("root_id", condition.getValue());
    }

    @Test
    void onConditionWithNullEqValueTranslatesOperatorToIsNull() {
        // Given
        final Table joinTable = new Table("joined_table", null);
        final SqlJoinSpec joinSpec = new SqlJoinSpec(joinTable, mock(SelectExpressionMapper.class));
        final SqlSelector delegate = mock(SqlSelector.class);
        final SqlJoinClause clause = new SqlJoinClause(joinSpec, delegate);

        // When
        final SqlJoinConditionClauseTerminal terminal = clause.on("optional_id").eq((Object) null);

        // Then
        assertNotNull(terminal);
        assertEquals(1, joinSpec.currentConditionGroupSpec().conditions().size());

        final ConditionSpec condition = joinSpec.currentConditionGroupSpec().conditions().getFirst().conditionSpec();
        final Column column = ((ColumnExpressionSpec) condition.getLhs()).getColumn();
        assertEquals(joinTable, column.table());
        assertEquals("optional_id", column.name());
        assertEquals(Operator.IS_NULL, condition.getOperator());
        assertNull(condition.getValue());
    }

    @Test
    void usingAddsUsingConditionAndReturnsTerminalClause() {
        // Given
        final Table joinTable = new Table("joined_table", null);
        final SqlJoinSpec joinSpec = new SqlJoinSpec(joinTable, mock(SelectExpressionMapper.class));
        final SqlSelector delegate = mock(SqlSelector.class);
        final SqlJoinClause clause = new SqlJoinClause(joinSpec, delegate);

        // When
        final SqlJoinConditionClauseTerminal result = clause.using("shared_id");

        // Then
        assertNotNull(result);
        assertEquals(1, joinSpec.currentConditionGroupSpec().conditions().size());

        final ConditionSpec condition = joinSpec.currentConditionGroupSpec().conditions().getFirst().conditionSpec();
        final Column column = ((ColumnExpressionSpec) condition.getLhs()).getColumn();
        assertEquals(joinTable, column.table());
        assertEquals("shared_id", column.name());
        assertEquals(Operator.USING, condition.getOperator());
        assertNull(condition.getValue());
    }
}