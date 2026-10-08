package org.litebridge.db.spi.sql;

import org.junit.jupiter.api.Test;

import java.sql.Types;

import static org.junit.jupiter.api.Assertions.assertEquals;

class BindValueTest {

    @Test
    void bindValue() {
        // Given
        final Object value = "Hello World!";
        final int dataType = Types.VARCHAR;

        // When
        final BindValue result = new BindValue(value, dataType);

        // Then
        assertEquals(value, result.value());
        assertEquals(dataType, result.sqlDataType());
    }

    @Test
    void bindValue_singleArgConstructor() {
        // Given
        final Object value = "test";

        // When
        final BindValue result = new BindValue(value);

        // Then
        assertEquals(value, result.value());
        assertEquals(0, result.sqlDataType());
    }
}