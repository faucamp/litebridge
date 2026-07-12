package org.litebridge.orm.api.sql;

import org.junit.jupiter.api.Test;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.api.select.model.ConditionSpec;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;

class SqlJoinConditionClauseTest {

    @Test
    void constructor() {
        // Given
        final ConditionSpec conditionSpec = mock(ConditionSpec.class);
        final SqlJoinConditionClauseTerminal conditionTerminal = mock(SqlJoinConditionClauseTerminal.class);

        // When
        final SqlJoinConditionClause result = new SqlJoinConditionClause(conditionSpec, conditionTerminal, mock(LitebridgeContext.class));

        // Then
        assertNotNull(result);
    }
}