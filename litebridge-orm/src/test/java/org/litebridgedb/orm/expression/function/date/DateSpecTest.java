package org.litebridgedb.orm.expression.function.date;

import org.junit.jupiter.api.Test;

import java.time.ZonedDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DateSpecTest {

    @Test
    void testCurrentTimestampSpec() {
        final CurrentTimestampSpec spec = new CurrentTimestampSpec();
        assertEquals(ZonedDateTime.class, spec.returnType());
    }
}
