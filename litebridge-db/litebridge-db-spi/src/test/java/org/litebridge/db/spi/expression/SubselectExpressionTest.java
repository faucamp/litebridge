package org.litebridge.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubselectExpressionTest {

    @Test
    void select_returnsWrappedSelect() {
        // Given
        final Select select = new Select(new Table("T"), Collections.emptyList());
        final SubselectExpression subselectExpr = new SubselectExpression(select) {
            @Override
            public String toSql(final Operation operation, final ConnectionProvider connectionProvider) {
                return "(SELECT 1)";
            }
        };

        // When & Then
        assertEquals(select, subselectExpr.select());
    }
}
