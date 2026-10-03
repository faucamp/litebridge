package org.litebridge.db.spi.impl.expression.function.date;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.impl.expression.function.date.CurrentTimestamp;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.query.Select;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DateFunctionsTest {

    @Test
    void currentTimestamp() {
        // Given
        final CurrentTimestamp currentTimestamp = new CurrentTimestamp(null, new LabelGenerator());
        final Select select = mock(Select.class);

        // When
        final String sql = currentTimestamp.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("CURRENT_TIMESTAMP", sql);
    }

    @Test
    void currentTimestamp_alias() {
        // Given
        final CurrentTimestamp currentTimestamp = new CurrentTimestamp("testAlias", new LabelGenerator());
        final Select select = mock(Select.class);

        // When
        final String sql = currentTimestamp.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("CURRENT_TIMESTAMP AS \"testAlias\"", sql);
    }
}
