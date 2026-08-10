package org.litebridge.orm.persistence;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.update.RowValue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;

class DtoRowValueTest {

    @Test
    void testDtoRowValue() {
        Object dto = new Object();
        RowValue rowValue = mock(RowValue.class);
        DtoRowValue dtoRowValue = new DtoRowValue(dto, rowValue);

        assertEquals(dto, dtoRowValue.dto());
        assertEquals(rowValue, dtoRowValue.rowValue());
    }
}
