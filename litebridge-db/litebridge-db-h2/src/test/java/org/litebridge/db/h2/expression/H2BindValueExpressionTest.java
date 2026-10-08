package org.litebridge.db.h2.expression;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.ColumnType;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class H2BindValueExpressionTest {

    @Test
    void toSql() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final Operation operation = mock(Select.class);
        final ColumnType columnType = new ColumnType(Types.VARCHAR, 10);
        final H2BindValueExpression bindValueExpression = new H2BindValueExpression(0, 1, columnType, "myAlias", labelGenerator);

        // When
        final String result = bindValueExpression.toSql(operation, ClauseType.WHERE, null);

        // Then
        assertEquals("?", result);
    }

    @Test
    void toSql_select_cast() {
        // Given
        final LabelGenerator labelGenerator = new LabelGenerator();
        final Operation operation = mock(Select.class);
        final ColumnType columnType = new ColumnType(Types.VARCHAR, 10);
        final H2BindValueExpression bindValueExpression = new H2BindValueExpression(0, 1, columnType, "myAlias", labelGenerator);

        // When
        final String result = bindValueExpression.toSql(operation, ClauseType.SELECT, null);

        // Then
        assertEquals("CAST(? AS VARCHAR(10)) AS \"myAlias\"", result);
    }
}