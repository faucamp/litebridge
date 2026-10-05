package org.litebridge.db.postgres.expression.function.aggregate;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class PostgresCountTest {

    @Test
    void toSql_selectClause() {
        // Given
        final PostgresCount count = new PostgresCount(null, new LabelGenerator());

        // When
        final String result = count.toSql(mock(Select.class), ClauseType.SELECT, null);

        // Then
        assertEquals("count(*)", result);
    }

    @Test
    void toSql_havingClause() {
        // Given
        final PostgresCount count = new PostgresCount(null, new LabelGenerator());

        // When
        final String result = count.toSql(mock(Select.class), ClauseType.HAVING, null);

        // Then
        assertEquals("count(*)", result);
    }

    @Test
    void toSql_otherClause() {
        // Given
        final PostgresCount count = new PostgresCount(null, new LabelGenerator());

        // When
        final String result = count.toSql(mock(Select.class), ClauseType.WHERE, null);

        // Then
        assertEquals("count", result);
    }
}