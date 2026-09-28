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
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.DefaultSqlGenerator;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.SqlGenerator;

import java.util.Objects;
import java.util.function.Function;

public final class ContextBuilder {

    private @Nullable DatabaseProviderMetaData databaseProviderMetaData;
    private @Nullable SqlGenerator sqlGenerator;
    private @Nullable LabelGenerator labelGenerator;
    private @Nullable MetaDataEngine metaDataEngine;
    private @Nullable ExecutionEngine executionEngine;
    private @Nullable MathOperationGenerator mathOperationGenerator;
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

    public ContextBuilder withLabelGenerator(final LabelGenerator labelGenerator) {
        this.labelGenerator = labelGenerator;
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

    public ContextBuilder withMathOperationGenerator(final MathOperationGenerator mathOperationGenerator) {
        this.mathOperationGenerator = mathOperationGenerator;
        return this;
    }

    public DatabaseProviderMetaData ensureDatabaseProviderMetaData() {
        databaseProviderMetaData = Objects.requireNonNullElseGet(databaseProviderMetaData, () -> new DatabaseProviderMetaData(true, true, DatabaseProviderMetaData.InsertCapability.NATIVE_MULTIROW));
        return databaseProviderMetaData;
    }

    public MetaDataEngine ensureMetaDataEngine() {
        metaDataEngine = Objects.requireNonNullElseGet(metaDataEngine, () -> new DefaultMetaDataEngine(ensureDatabaseProviderMetaData()));
        return metaDataEngine;
    }

    public LabelGenerator ensureLabelGenerator() {
        labelGenerator = Objects.requireNonNullElseGet(labelGenerator, LabelGenerator::new);
        return labelGenerator;
    }

    public MathOperationGenerator ensureMathOperationGenerator() {
        mathOperationGenerator = Objects.requireNonNullElseGet(mathOperationGenerator, () -> new MathOperationGenerator(ensureLabelGenerator()));
        return mathOperationGenerator;
    }

    public SqlGenerator ensureSqlGenerator() {
        sqlGenerator = Objects.requireNonNullElseGet(sqlGenerator, () -> new DefaultSqlGenerator(ensureMetaDataEngine(), ensureLabelGenerator(), ensureMathOperationGenerator()));
        return sqlGenerator;
    }

    public TypeConverter ensureTypeConverter() {
        typeConverter = Objects.requireNonNullElseGet(typeConverter, ContextBuilder::loadDefaultTypeConverter);
        return typeConverter;
    }

    public AliasTransformer ensureAliasTransformer() {
        aliasTransformer = Objects.requireNonNullElseGet(aliasTransformer, DefaultAliasTransformer::new);
        return aliasTransformer;
    }

    public ExecutionEngine ensureExecutionEngine() {
        executionEngine = Objects.requireNonNullElseGet(executionEngine, () -> new ExecutionEngineReturnedKeysAuto(ensureTypeConverter(), ensureAliasTransformer(), ensureDatabaseProviderMetaData().insertCapability()));
        return executionEngine;
    }

    public DatabaseProviderContext build() {
        final MetaDataEngine finalMetaDataEngine = ensureMetaDataEngine();
        final LabelGenerator finalLabelGenerator = ensureLabelGenerator();
        final SqlGenerator finalSqlGenerator = ensureSqlGenerator();
        final ExecutionEngine finalExecutionEngine = ensureExecutionEngine();
        final Function<String, SequenceColumnValueGenerator> finalSequenceColumnValueGenerator =
                Objects.requireNonNullElseGet(sequenceColumnValueGeneratorCreator, () -> DefaultSequenceColumnValueGenerator::new);

        final SqlFunctionRegistry sqlFunctionRegistry;

        if (sqlFunctionRegistryFactory != null) {
            sqlFunctionRegistry = sqlFunctionRegistryFactory.create();
        } else {
            sqlFunctionRegistry = new SqlFunctionRegistryFactory(finalLabelGenerator, finalSqlGenerator.selectSqlGenerator()).create();
        }

        return new DatabaseProviderContext(
                finalSqlGenerator,
                finalLabelGenerator,
                finalMetaDataEngine,
                finalExecutionEngine,
                sqlFunctionRegistry,
                finalSequenceColumnValueGenerator);
    }

    @SuppressWarnings("unchecked")
    private static TypeConverter loadDefaultTypeConverter() {
        final Module converterModule = ModuleLayer.boot().findModule("litebridge.converter").orElseThrow(() -> new IllegalStateException("No type converter specified, and litebridge.converter module not found"));
        final Class<TypeConverter> typeConverterClass = (Class<TypeConverter>) Class.forName(converterModule, "org.litebridge.db.spi.impl.DefaultTypeConverter");
        return ClassUtils.newInstance(typeConverterClass);
    }
}
