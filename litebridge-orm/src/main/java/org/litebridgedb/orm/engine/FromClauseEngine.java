package org.litebridgedb.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.dto.DtoSelector;
import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.api.sql.SqlSelector;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
import org.litebridgedb.orm.expression.Expression;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridgedb.tracking.ChangeTracker;

public final class FromClauseEngine {

    /**
     * Used to indicate that all fields or columns should be selected.
     */
    public static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final ChangeTracker changeTracker;
    private final DtoConstructor dtoConstructor;
    private final LitebridgeConfig litebridgeConfig;

    public FromClauseEngine(final TransactionalDatabaseProvider databaseProvider,
                            final TableRegistry tableRegistry,
                            final ChangeTracker changeTracker,
                            final DtoConstructor dtoConstructor,
                            final LitebridgeConfig litebridgeConfig) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.changeTracker = changeTracker;
        this.dtoConstructor = dtoConstructor;
        this.litebridgeConfig = litebridgeConfig;
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Expression[] expressions, final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final DtoSelector<DTO> dtoSelector = createDtoSelectorForType(dtoClass, dtoClass, relatedDtoStrategy);
        return select(expressions, dtoSelector);
    }

    public <TypeOverride> DtoFromClauseTerminal<TypeOverride> from(final Expression[] expressions, final Class<?> dtoClass, final Class<TypeOverride> typeOverrideClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final DtoSelector<TypeOverride> dtoSelector = createDtoSelectorForType(typeOverrideClass, dtoClass, relatedDtoStrategy);

        if (expressions.length > 0) {
            return dtoSelector.select(expressions);
        } else {
            return dtoSelector.select();
        }
    }

    private <TypeOverride> DtoSelector<TypeOverride> createDtoSelectorForType(final Class<TypeOverride> typeOverride, final Class<?> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);

        final LitebridgeConfig activeConfig;

        if (relatedDtoStrategy != null) {
            activeConfig = new LitebridgeConfig(litebridgeConfig);
            activeConfig.setRelatedDtoStrategy(relatedDtoStrategy);
        } else {
            activeConfig = litebridgeConfig;
        }

        return new DtoSelector<>(typeOverride, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, activeConfig);
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        final OrmTable table = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, litebridgeConfig)
                .select();
    }

    public SqlFromClauseTerminal from(final Expression[] expressions, final String table) {
        return new SqlSelector(databaseProvider, tableRegistry, litebridgeConfig).select(expressions).from(table);
    }

    private static <DTO> DtoFromClauseTerminal<DTO> select(final Expression[] expressions, final DtoSelector<DTO> dtoSelector) {
        if (expressions.length > 0) {
            return dtoSelector.select(expressions);
        } else {
            return dtoSelector.select();
        }
    }
}
