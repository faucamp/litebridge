package org.litebridgedb.db.spi.expression;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.query.Select;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ConnectionProviderExpressionTest {

    @Test
    void toSql() {
        // Given
        final ConnectionProviderExpression connectionProviderExpression = new TestConnectionProviderExpression();

        // When/Then
        assertThrows(UnsupportedOperationException.class, () -> connectionProviderExpression.toSql(mock(Select.class)));
    }

    @Test
    void testToSql_connectionProvider() {
        // Given
        final ConnectionProviderExpression connectionProviderExpression = new TestConnectionProviderExpression();

        // When
        final PreparedSql result = connectionProviderExpression.toSql(mock(Select.class), mock(ConnectionProvider.class));

        // Then
        assertEquals("SELECT * FROM TABLE", result.sql());
    }

    private static class TestConnectionProviderExpression implements ConnectionProviderExpression {

        @Override
        public PreparedSql toSql(final Operation operation, final ConnectionProvider connectionProvider) {
            return new PreparedSql("SELECT * FROM TABLE");
        }
    }
}