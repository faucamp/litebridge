package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;

import static org.junit.jupiter.api.Assertions.*;

class OracleDatabaseProviderTest {

    private final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

    @Test
    void sequenceColumnValueGenerator() {
        // Given
        final String sequence = "myschema.sequence";

        // When
        final SequenceColumnValueGenerator result = oracleDatabaseProvider.sequenceColumnValueGenerator(sequence);

        // Then
        assertInstanceOf(OracleSequenceColumnValueGenerator.class, result);
    }

    @Test
    void createColumnIdentifierGenerator() {
        // When
        final ColumnIdentifierGenerator result = oracleDatabaseProvider.createColumnIdentifierGenerator();

        // Then
        assertNotNull(result);
    }

    @Test
    void createSqlFunctionRegistry() {
        // When
        final SqlFunctionRegistry result = oracleDatabaseProvider.createSqlFunctionRegistry();

        // Then
        assertNotNull(result);
    }

    @Test
    void getLogger() {
        assertNotNull(oracleDatabaseProvider.getLogger());
    }
}