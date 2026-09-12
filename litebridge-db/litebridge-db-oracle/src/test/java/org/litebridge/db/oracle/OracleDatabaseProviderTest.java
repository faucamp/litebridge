package org.litebridge.db.oracle;

import org.junit.jupiter.api.Test;
import org.litebridge.db.oracle.api.LitebridgeOracle;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.LitebridgeBuilder;
import org.litebridge.orm.config.LitebridgeConfig;

import java.lang.invoke.MethodHandles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

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

    @Test
    void litebridgeClass_returnsLitebridgeOracleClass() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

        // When
        final Class<LitebridgeOracle> result = oracleDatabaseProvider.litebridgeClass();

        // Then
        assertEquals(LitebridgeOracle.class, result);
    }

    @Test
    void createLitebridge_withValidArgs_returnsInitializedInstance() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();
        final DatabaseProvider databaseProvider = mock(DatabaseProvider.class);
        final TransactionManager transactionManager = mock(TransactionManager.class);
        final LitebridgeConfig config = new LitebridgeConfig();
        final MethodHandles.Lookup lookup = MethodHandles.lookup();
        final LitebridgeBuilder.ConstructorArgs args = new LitebridgeBuilder.ConstructorArgs(databaseProvider, transactionManager, config, lookup);

        // When
        final LitebridgeOracle litebridge = oracleDatabaseProvider.createLitebridge(args);

        // Then
        assertNotNull(litebridge);
        assertInstanceOf(LitebridgeOracle.class, litebridge);
    }

    @Test
    void metaData_configuredWithOracleCapabilities() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

        // When
        final DatabaseProviderMetaData metaData = oracleDatabaseProvider.metaData();

        // Then
        assertEquals(DatabaseProviderMetaData.InsertCapability.BATCHED_INSERTS, metaData.insertCapability());
        assertTrue(metaData.supportsMerge());
        assertTrue(metaData.supportsSequenceColumnValueGenerator());
    }

    @Test
    void providerDelegates_returnConfiguredComponents() {
        // Given
        final OracleDatabaseProvider oracleDatabaseProvider = new OracleDatabaseProvider();

        // When / Then
        assertNotNull(oracleDatabaseProvider.typeConverter());
        assertNotNull(oracleDatabaseProvider.aliasTransformer());
        assertNotNull(oracleDatabaseProvider.sqlFunctionRegistry());
    }
}
