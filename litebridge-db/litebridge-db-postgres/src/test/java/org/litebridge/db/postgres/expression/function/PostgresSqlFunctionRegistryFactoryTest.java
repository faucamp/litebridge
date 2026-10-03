package org.litebridge.db.postgres.expression.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.postgres.expression.function.aggregate.PostgresCount;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;

class PostgresSqlFunctionRegistryFactoryTest {

    @Test
    void createCount() {
        // Given
        final PostgresSqlFunctionRegistryFactory factory = new PostgresSqlFunctionRegistryFactory(mock(LabelGenerator.class), mock(SelectSqlGenerator.class));

        // When
        final SelectExpression result = factory.createCount();

        // Then
        assertInstanceOf(PostgresCount.class, result);
    }
}