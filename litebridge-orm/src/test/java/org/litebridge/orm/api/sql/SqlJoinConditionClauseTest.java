package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.mock;

class SqlJoinConditionClauseTest {

    @Test
    void constructor() {
        // Given
        final ConditionSpec conditionSpec = mock(ConditionSpec.class);

        // When
        final SqlJoinConditionClause result = new SqlJoinConditionClause(conditionSpec, mock(LitebridgeContext.class), LogicOperator.NOOP, new org.litebridge.orm.expression.select.SelectColumnSpec(mock(org.litebridge.db.spi.Column.class)), null, n -> mock(SqlJoinConditionClauseTerminal.class));

        // Then
        assertNotNull(result);
    }
}