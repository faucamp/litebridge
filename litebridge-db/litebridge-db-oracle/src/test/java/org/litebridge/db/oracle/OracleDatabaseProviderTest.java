package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OracleDatabaseProviderTest {

    @Test
    void testCreateSequenceNextValueForDirectInsert_validSequenceName() {
        // Given
        final OracleDatabaseProvider provider = new OracleDatabaseProvider();
        final String sequenceName = "MY_SEQUENCE";

        // When
        String result = provider.createSequenceNextValueForDirectInsert(sequenceName);

        // Then
        assertEquals("MY_SEQUENCE.NEXTVAL", result);
    }
}