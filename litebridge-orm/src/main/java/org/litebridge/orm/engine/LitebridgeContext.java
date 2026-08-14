package org.litebridge.orm.engine;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.SqlFunctionRegistry;
import org.litebridge.db.spi.tx.TransactionManager;
import org.litebridge.orm.api.dto.DtoProtoExpressionResolver;
import org.litebridge.orm.api.select.model.ProtoExpressionResolver;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.api.sql.SqlProtoExpressionResolver;
import org.litebridge.orm.config.LitebridgeConfig;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;

/**
 * A context object that provides access to core Litebridge components and configuration.
 * <p>
 * This record provides the core configuration and utility components required for
 * query generation, function resolution, and execution.
 */
public final class LitebridgeContext {

    private final Mode mode;
    private final LitebridgeConfig config;
    private final DatabaseProvider databaseProvider;
    private final FromClauseEngine fromClauseEngine;
    private final SqlFunctionRegistry sqlFunctionRegistry;
    private final QueryPlanCache queryPlanCache;
    private final AliasGenerator aliasGenerator;
    private final TableMetaDataCache tableMetaDataCache;
    private final TypeConverter typeConverter;
    private final SelectExpressionMapper selectExpressionMapper;
    private RelatedDtoStrategy relatedDtoStrategy;

    /**
     * Create a new Litebridge context with the specified components.
     *
     * @param mode                   The mode of operation for the Litebridge context.
     * @param config                 Configuration for managing runtime behaviour
     * @param fromClauseEngine       The engine responsible for managing table registries alias generation, and facilitating query specifications.
     * @param queryPlanCache         A cache for storing execution plans based on query structure.
     * @param aliasGenerator         An alias generator for creating unique table and column aliases.
     */
    public LitebridgeContext(final Mode mode,
                             final LitebridgeConfig config,
                             final DatabaseProvider databaseProvider,
                             final FromClauseEngine fromClauseEngine,
                             final QueryPlanCache queryPlanCache,
                             final AliasGenerator aliasGenerator,
                             final TableMetaDataCache tableMetaDataCache) {
        this.mode = mode;
        this.config = config;
        this.databaseProvider = databaseProvider;
        this.fromClauseEngine = fromClauseEngine;
        this.sqlFunctionRegistry = databaseProvider.getSqlFunctionRegistry();
        this.queryPlanCache = queryPlanCache;
        this.aliasGenerator = aliasGenerator;
        this.relatedDtoStrategy = config.relatedDtoStrategy();
        this.tableMetaDataCache = tableMetaDataCache;
        this.typeConverter = databaseProvider.getTypeConverter();
        this.selectExpressionMapper = createSelectExpressionMapper();
    }

    public Mode mode() {
        return mode;
    }

    public LitebridgeConfig config() {
        return config;
    }

    public FromClauseEngine fromClauseEngine() {
        return fromClauseEngine;
    }

    public SqlFunctionRegistry sqlFunctionRegistry() {
        return sqlFunctionRegistry;
    }

    public QueryPlanCache queryPlanCache() {
        return queryPlanCache;
    }

    public AliasGenerator aliasGenerator() {
        return aliasGenerator;
    }

    public TableMetaDataCache tableMetaDataCache() {
        return tableMetaDataCache;
    }

    public TableRegistry tableRegistry() {
        return fromClauseEngine.tableRegistry();
    }

    public DatabaseProvider databaseProvider() {
        return fromClauseEngine.databaseProvider();
    }

    public TransactionManager transactionManager() {
        return fromClauseEngine.databaseProvider().transactionManager();
    }

    public ClassFieldAccessorCache classFieldAccessorCache() {
        return fromClauseEngine.changeTracker().classFieldAccessorCache();
    }

    public TypeConverter typeConverter() {
        return typeConverter;
    }

    public SelectExpressionMapper selectExpressionMapper() {
        return selectExpressionMapper;
    }

    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = relatedDtoStrategy;
    }

    public QueryCompiler createQueryCompiler() {
        return new QueryCompiler(fromClauseEngine.tableRegistry(), tableMetaDataCache, typeConverter, aliasGenerator, selectExpressionMapper);
    }

    private SelectExpressionMapper createSelectExpressionMapper() {
        final ProtoExpressionResolver protoExpressionResolver;

        if (mode == Mode.DTO) {
            protoExpressionResolver = new DtoProtoExpressionResolver(aliasGenerator, classFieldAccessorCache(), tableRegistry());
        } else {
            protoExpressionResolver = new SqlProtoExpressionResolver();
        }

        return new SelectExpressionMapper(databaseProvider.getSqlFunctionRegistry(), protoExpressionResolver, tableMetaDataCache, databaseProvider.getTypeConverter());
    }

    public enum Mode {
        DTO,
        SQL,
        NATIVE_SQL;
    }
}
