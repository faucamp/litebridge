package org.litebridge.db.oracle.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.function.scalar.Substr;
import org.litebridge.db.spi.expression.ColumnExpressionImpl;
import org.litebridge.db.spi.expression.DelegateColumnExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class OracleSqlFunctionRegistryFactoryTest {

    @Test
    void createSubstring() {
        // Given
        final OracleSqlFunctionRegistryFactory oracleSqlFunctionRegistryFactory = new OracleSqlFunctionRegistryFactory(mock(ColumnIdentifierGenerator.class), mock(SelectSqlGenerator.class));

        // Whe
        final DelegateColumnExpression result = oracleSqlFunctionRegistryFactory.createSubstring(mock(ColumnExpressionImpl.class), 3, 7);

        // Then
        assertInstanceOf(Substr.class, result);
    }
}