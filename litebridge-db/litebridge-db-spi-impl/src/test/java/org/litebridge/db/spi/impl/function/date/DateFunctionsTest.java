package org.litebridge.db.spi.impl.function.date;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.query.Select;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DateFunctionsTest {

    @Test
    void currentTimestamp() {
        // Given
        final CurrentTimestamp currentTimestamp = new CurrentTimestamp();
        final Select select = mock(Select.class);

        // When
        final String sql = currentTimestamp.toSql(select, ClauseType.SELECT);

        // Then
        assertEquals("CURRENT_TIMESTAMP", sql);
    }
}
