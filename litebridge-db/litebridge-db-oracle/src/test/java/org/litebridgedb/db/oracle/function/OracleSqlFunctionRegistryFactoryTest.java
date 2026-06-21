package org.litebridgedb.db.oracle.function;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.oracle.function.scalar.Substr;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.impl.sql.SelectSqlGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class OracleSqlFunctionRegistryFactoryTest {

    @Test
    void createSubstring() {
        // Given
        final OracleSqlFunctionRegistryFactory oracleSqlFunctionRegistryFactory = new OracleSqlFunctionRegistryFactory(mock(ColumnIdentifierGenerator.class), mock(SelectSqlGenerator.class));

        // Whe
        final NestableExpression result = oracleSqlFunctionRegistryFactory.createSubstring(mock(ColumnExpression.class), 3, 7);

        // Then
        assertInstanceOf(Substr.class, result);
    }
}