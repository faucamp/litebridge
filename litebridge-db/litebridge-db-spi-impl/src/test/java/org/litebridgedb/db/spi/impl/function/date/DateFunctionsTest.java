package org.litebridgedb.db.spi.impl.function.date;

import org.junit.jupiter.api.Test;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.query.Select;

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
