package org.litebridge.db.oracle.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.function.scalar.Substr;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.AbstractColumnExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class OracleSqlFunctionRegistryFactoryTest {

    @Test
    void createSubstring() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final OracleSqlFunctionRegistryFactory oracleSqlFunctionRegistryFactory = new OracleSqlFunctionRegistryFactory(labelGenerator, mock(SelectSqlGenerator.class));

        // Whe
        final DelegateExpression result = oracleSqlFunctionRegistryFactory.createSubstring(mock(AbstractColumnExpression.class), 3, 7, null);

        // Then
        assertInstanceOf(Substr.class, result);
    }
}