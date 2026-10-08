package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.update.Delete;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class SelectExpressionTest {

    @Test
    void defaultToSql_delegatesWithNullParent() {
        // Given
        final Operation operation = new Delete(new Table("T"), new ConditionGroup(Collections.emptyList()));
        final SelectExpression expression = (op, clause, parent) -> {
            assertNull(parent);
            return "SQL_FOR_" + clause.name();
        };

        // When
        final String result = expression.toSql(operation, ClauseType.WHERE);

        // Then
        assertEquals("SQL_FOR_WHERE", result);
    }
}
