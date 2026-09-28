package org.litebridge.db.spi.expression;

import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.tx.ConnectionProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ConnectionProviderExpressionTest {

    @Test
    void toSql() {
        // Given
        final ConnectionProviderExpression connectionProviderExpression = new TestConnectionProviderExpression();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> connectionProviderExpression.toSql(mock(Select.class), ClauseType.SELECT));
    }

    @Test
    void testToSql_connectionProvider() {
        // Given
        final ConnectionProviderExpression connectionProviderExpression = new TestConnectionProviderExpression();

        // When
        final String result = connectionProviderExpression.toSql(mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT * FROM TABLE", result);
    }

    private static class TestConnectionProviderExpression implements ConnectionProviderExpression {

        @Override
        public String toSql(final @NonNull Operation operation, final @NonNull ConnectionProvider connectionProvider) {
            return "SELECT * FROM TABLE";
        }
    }
}