package org.litebridgedb.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.orm.api.dto.DtoFromClauseTerminal;
import org.litebridgedb.orm.api.dto.DtoSelector;
import org.litebridgedb.orm.api.sql.SqlFromClauseTerminal;
import org.litebridgedb.orm.api.sql.SqlSelector;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.config.RelatedDtoStrategy;
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

    public <DTO> DtoFromClauseTerminal<DTO> from(final Aliased[] fields, final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);

        final LitebridgeConfig activeConfig;

        if (relatedDtoStrategy != null) {
            activeConfig = new LitebridgeConfig(litebridgeConfig);
            activeConfig.setRelatedDtoStrategy(relatedDtoStrategy);
        } else {
            activeConfig = litebridgeConfig;
        }

        final DtoSelector<DTO> dtoSelector = new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, activeConfig);

        if (fields == ALL_COLUMNS) {
            return dtoSelector.select();
        } else {
            return dtoSelector.select(fields);
        }
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        final OrmTable table = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, litebridgeConfig)
                .select();
    }

    public SqlFromClauseTerminal from(final Aliased[] columns, final String table) {
        return new SqlSelector(databaseProvider, tableRegistry, litebridgeConfig).select(columns).from(table);
    }
}
