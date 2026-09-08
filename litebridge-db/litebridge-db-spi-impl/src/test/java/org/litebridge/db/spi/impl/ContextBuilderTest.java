package org.litebridge.db.spi.impl;

import org.junit.jupiter.api.Test;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class ContextBuilderTest {

    @Test
    void build_defaults_noTypeConverterModule() {
        // When / Then
        assertThrows(IllegalStateException.class, () -> ContextBuilder.newContext().build());
    }

    @Test
    void build_defaults_withTypeConverter() {
        // Given
        final TypeConverter typeConverter = mock(TypeConverter.class);

        // When
        final DatabaseProviderContext result = ContextBuilder.newContext()
                .withTypeConverter(typeConverter)
                .build();

        // Then
        assertNotNull(result);
        assertNotNull(result.executionEngine());
        assertNotNull(result.metaDataEngine());
        assertNotNull(result.sequenceColumnValueGeneratorCreator());
        assertNotNull(result.sqlGenerator());
    }

    @Test
    void build_custom() {
        // Given
        final DatabaseProviderMetaData databaseProviderMetaData = mock(DatabaseProviderMetaData.class);
        final ExecutionEngine executionEngine = mock(ExecutionEngine.class);
        final MetaDataEngine metaDataEngine = mock(MetaDataEngine.class);
        final ColumnIdentifierGenerator columnIdentifierGenerator = mock(ColumnIdentifierGenerator.class);
        final MathOperationGenerator mathOperationGenerator = mock(MathOperationGenerator.class);
        final SequenceColumnValueGenerator sequenceColumnValueGenerator = mock(SequenceColumnValueGenerator.class);
        final SqlGenerator sqlGenerator = mock(SqlGenerator.class);
        final AliasTransformer aliasTransformer = mock(AliasTransformer.class);
        final TypeConverter typeConverter = mock(TypeConverter.class);
        final SqlFunctionRegistryFactory sqlFunctionRegistryFactory = mock(SqlFunctionRegistryFactory.class);

        // When
        final DatabaseProviderContext result = ContextBuilder.newContext()
                .withAliasTransformer(aliasTransformer)
                .withColumnIdentifierGenerator(columnIdentifierGenerator)
                .withDatabaseProviderMetaData(databaseProviderMetaData)
                .withExecutionEngine(executionEngine)
                .withMetaDataEngine(metaDataEngine)
                .withMathOperationGenerator(mathOperationGenerator)
                .withSequenceColumnValueGenerator(str -> sequenceColumnValueGenerator)
                .withSqlFunctionRegistryFactory(sqlFunctionRegistryFactory)
                .withSqlGenerator(sqlGenerator)
                .withTypeConverter(typeConverter)
                .build();


        // Then
        assertNotNull(result);
        assertEquals(executionEngine, result.executionEngine());
        assertEquals(metaDataEngine, result.metaDataEngine());
        assertNotNull(result.sequenceColumnValueGeneratorCreator());
        assertNull(result.sqlFunctionRegistry()); // null because of mock
        assertEquals(sqlGenerator, result.sqlGenerator());
    }
}