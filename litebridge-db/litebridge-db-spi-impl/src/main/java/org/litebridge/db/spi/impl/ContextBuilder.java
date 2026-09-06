package org.litebridge.db.spi.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.ClassUtils;
import org.litebridge.db.spi.DatabaseProviderMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.alias.DefaultAliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.impl.engine.DefaultMetaDataEngine;
import org.litebridge.db.spi.impl.engine.ExecutionEngine;
import org.litebridge.db.spi.impl.engine.ExecutionEngineReturnedKeysAuto;
import org.litebridge.db.spi.impl.engine.MetaDataEngine;
import org.litebridge.db.spi.impl.function.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

import java.util.Objects;
import java.util.function.Function;

public final class ContextBuilder {

    private @Nullable DatabaseProviderMetaData databaseProviderMetaData;
    private @Nullable SqlGenerator sqlGenerator;
    private @Nullable MetaDataEngine metaDataEngine;
    private @Nullable ExecutionEngine executionEngine;
    private @Nullable ColumnIdentifierGenerator columnIdentifierGenerator;
    private @Nullable TypeConverter typeConverter;
    private @Nullable AliasTransformer aliasTransformer;
    private @Nullable SqlFunctionRegistryFactory sqlFunctionRegistryFactory;
    private @Nullable Function<String, SequenceColumnValueGenerator> sequenceColumnValueGeneratorCreator;

    private ContextBuilder() {
    }

    public static ContextBuilder newContext() {
        return new ContextBuilder();
    }

    public ContextBuilder withDatabaseProviderMetaData(final DatabaseProviderMetaData databaseProviderMetaData) {
        this.databaseProviderMetaData = databaseProviderMetaData;
        return this;
    }

    public ContextBuilder withSqlGenerator(final SqlGenerator sqlGenerator) {
        this.sqlGenerator = sqlGenerator;
        return this;
    }

    public ContextBuilder withMetaDataEngine(final MetaDataEngine metaDataEngine) {
        this.metaDataEngine = metaDataEngine;
        return this;
    }

    public ContextBuilder withExecutionEngine(final ExecutionEngine executionEngine) {
        this.executionEngine = executionEngine;
        return this;
    }

    public ContextBuilder withColumnIdentifierGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator) {
        this.columnIdentifierGenerator = columnIdentifierGenerator;
        return this;
    }

    public ContextBuilder withTypeConverter(final TypeConverter typeConverter) {
        this.typeConverter = typeConverter;
        return this;
    }

    public ContextBuilder withAliasTransformer(final AliasTransformer aliasTransformer) {
        this.aliasTransformer = aliasTransformer;
        return this;
    }

    public ContextBuilder withSqlFunctionRegistryFactory(final SqlFunctionRegistryFactory sqlFunctionRegistryFactory) {
        this.sqlFunctionRegistryFactory = sqlFunctionRegistryFactory;
        return this;
    }

    public ContextBuilder withSequenceColumnValueGenerator(final Function<String, SequenceColumnValueGenerator> sequenceColumnValueGeneratorCreator) {
        this.sequenceColumnValueGeneratorCreator = sequenceColumnValueGeneratorCreator;
        return this;
    }

    public DatabaseProviderContext build() {
        final DatabaseProviderMetaData finalDatabaseProviderMetaData =
                Objects.requireNonNullElseGet(databaseProviderMetaData, () -> new DatabaseProviderMetaData(true, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW));
        final MetaDataEngine finalMetaDataEngine =
                Objects.requireNonNullElseGet(metaDataEngine, () -> new DefaultMetaDataEngine(finalDatabaseProviderMetaData));
        final SqlGenerator finalSqlGenerator =
                Objects.requireNonNullElseGet(sqlGenerator, () -> new DefaultSqlGenerator(finalMetaDataEngine));
        final TypeConverter finalTypeConverter =
                Objects.requireNonNullElseGet(typeConverter, ContextBuilder::loadDefaultTypeConverter);
        final AliasTransformer finalAliasTransformer =
                Objects.requireNonNullElseGet(aliasTransformer, DefaultAliasTransformer::new);
        final ExecutionEngine finalExecutionEngine =
                Objects.requireNonNullElseGet(executionEngine, () -> new ExecutionEngineReturnedKeysAuto(finalTypeConverter, finalAliasTransformer));
        final ColumnIdentifierGenerator finalColumnIdentifierGenerator =
                Objects.requireNonNullElseGet(columnIdentifierGenerator, ColumnIdentifierGenerator::new);
        final Function<String, SequenceColumnValueGenerator> finalSequenceColumnValueGenerator =
                Objects.requireNonNullElseGet(sequenceColumnValueGeneratorCreator, () -> DefaultSequenceColumnValueGenerator::new);

        final SqlFunctionRegistry sqlFunctionRegistry;

        if (sqlFunctionRegistryFactory != null) {
            sqlFunctionRegistry = sqlFunctionRegistryFactory.create();
        } else {
            sqlFunctionRegistry = new SqlFunctionRegistryFactory(finalColumnIdentifierGenerator, finalSqlGenerator.selectSqlGenerator()).create();
        }

        return new DatabaseProviderContext(
                finalSqlGenerator,
                finalMetaDataEngine,
                finalExecutionEngine,
                sqlFunctionRegistry,
                finalColumnIdentifierGenerator,
                finalSequenceColumnValueGenerator);
    }

    @SuppressWarnings("unchecked")
    private static TypeConverter loadDefaultTypeConverter() {
        final Module converterModule = ModuleLayer.boot().findModule("litebridge.converter").orElseThrow(() -> new IllegalStateException("No type converter specified, and litebridge.converter module not found"));
        final Class<TypeConverter> typeConverterClass = (Class<TypeConverter>) Class.forName(converterModule, "org.litebridge.db.spi.impl.DefaultTypeConverter");
        return ClassUtils.newInstance(typeConverterClass);
    }
}
