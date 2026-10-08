package org.litebridge.db.h2.expression.function;

import org.junit.jupiter.api.Test;
import org.litebridge.db.h2.expression.H2BindValueExpression;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;

class H2SqlFunctionRegistryFactoryTest {

    @Test
    void createBindValue() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final SelectSqlGenerator selectSqlGenerator = mock(SelectSqlGenerator.class);
        final H2SqlFunctionRegistryFactory h2SqlFunctionRegistryFactory = new H2SqlFunctionRegistryFactory(labelGenerator, selectSqlGenerator);
        final ColumnType columnType = new ColumnType(Types.VARCHAR, 10);

        // When
        final BindValueExpression result = h2SqlFunctionRegistryFactory.createBindValue(0, 1, columnType, "myAlias");

        // Then
        assertInstanceOf(H2BindValueExpression.class, result);
        final H2BindValueExpression bindValue = (H2BindValueExpression) result;
        assertEquals(0, bindValue.index());
        assertEquals(1, bindValue.size());
        // Wrapped in a CAST()
        assertNull(bindValue.alias());
        assertSame(columnType, result.columnType());
    }
}