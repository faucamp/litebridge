package org.litebridge.db.oracle.function.scalar;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.impl.expression.SelectColumn;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SubstrTest {

    private static final LabelGenerator labelGenerator = new LabelGenerator();

    @Test
    void template() {
        // Given
        final Substr substr = new Substr(new SelectColumn(new Column("TEST_COL"), null, null, labelGenerator), 2, 5, null, labelGenerator);

        // When
        final String result = substr.template();

        // Then
        assertEquals("SUBSTR(%s, 2, 5)", result);
    }

    @Test
    void template_nullLength() {
        // Given
        final Substr substr = new Substr(new SelectColumn(new Column("TEST_COL"), null, null, labelGenerator), 3, null, null, labelGenerator);

        // When
        final String result = substr.template();

        // Then
        assertEquals("SUBSTR(%s, 3)", result);
    }
}