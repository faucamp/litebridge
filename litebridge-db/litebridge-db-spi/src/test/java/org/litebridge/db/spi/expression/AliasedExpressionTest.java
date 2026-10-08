package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Operation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;

class AliasedExpressionTest {

    @Test
    void defaultTableAlias_returnsNull() {
        // Given
        final AliasedExpression expression = new AliasedExpression() {
            @Override
            public String alias() {
                return "aliasName";
            }

            @Override
            public String toSql(final Operation operation, final ClauseType clause, final DelegateExpression parent) {
                return "expr AS aliasName";
            }
        };

        // When & Then
        assertEquals("aliasName", expression.alias());
        assertNull(expression.tableAlias());
    }
}
