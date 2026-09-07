package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class OracleDatabaseProviderTest {

    @Test
    void sequenceColumnValueGenerator() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();
        final String sequence = "myschema.sequence";

        // When
        final SequenceColumnValueGenerator result = oracleDatabaseProvider.sequenceColumnValueGenerator(sequence);

        // Then
        assertInstanceOf(OracleSequenceColumnValueGenerator.class, result);
    }
}