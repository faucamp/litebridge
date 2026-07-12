package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Aliased;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.api.sql.SqlSelector;
import org.litebridge.orm.config.RelatedDtoStrategy;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.litebridge.tracking.ChangeTracker;

import java.util.function.Supplier;

/**
 * The {@code FromClauseEngine} class represents the generation of SQL "FROM" clauses
 * with support for both DTO-based and raw expression-based queries.
 * <p>
 * It acts as a builder for specifying the table and columns to be used in a query,
 * while also managing aliasing, context, and relationships between entities.
 * <p>
 * This class depends on various components such as a database provider, table registry,
 * change tracker, DTO constructor, and context supplier to construct SQL queries effectively.
 * <p>
 * This class is immutable and thread-safe. Instances of this class should not be modified after construction.
 */
public final class FromClauseEngine {

    /**
     * Used to indicate that all fields or expressions should be selected.
     */
    public static final Aliased[] ALL_COLUMNS = new Aliased[0];

    private final TransactionalDatabaseProvider databaseProvider;
    private final TableRegistry tableRegistry;
    private final ChangeTracker changeTracker;
    private final DtoConstructor dtoConstructor;
    private final Supplier<LitebridgeContext> contextSupplier;
    private final AliasGenerator aliasGenerator;

    /**
     * Constructs a new {@code FromClauseEngine}.
     *
     * @param databaseProvider the database provider.
     * @param tableRegistry    the table registry.
     * @param changeTracker    the change tracker.
     * @param dtoConstructor   the DTO constructor.
     * @param contextSupplier  the context supplier.
     */
    public FromClauseEngine(final TransactionalDatabaseProvider databaseProvider,
                            final TableRegistry tableRegistry,
                            final ChangeTracker changeTracker,
                            final DtoConstructor dtoConstructor,
                            final Supplier<LitebridgeContext> contextSupplier) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.changeTracker = changeTracker;
        this.dtoConstructor = dtoConstructor;
        this.contextSupplier = contextSupplier;
        this.aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
    }

    /**
     * Constructs a DTO-based FROM clause.
     *
     * @param expressionSpecs    the expression specifications.
     * @param dtoClass           the DTO class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @param <DTO>              the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final ExpressionSpec[] expressionSpecs, final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final DtoSelector<DTO> dtoSelector = createDtoSelectorForType(dtoClass, dtoClass, relatedDtoStrategy);
        return select(expressionSpecs, dtoSelector);
    }

    /**
     * Constructs a DTO-based FROM clause with a type override.
     *
     * @param expressionSpecs    the expression specifications.
     * @param dtoClass           the DTO class.
     * @param typeOverrideClass  the type override class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @param <TypeOverride>      the type override.
     * @return the DTO from clause terminal.
     */
    public <TypeOverride> DtoFromClauseTerminal<TypeOverride> from(final ExpressionSpec[] expressionSpecs, final Class<?> dtoClass, final Class<TypeOverride> typeOverrideClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final DtoSelector<TypeOverride> dtoSelector = createDtoSelectorForType(typeOverrideClass, dtoClass, relatedDtoStrategy);

        if (expressionSpecs.length > 0) {
            return dtoSelector.select(expressionSpecs);
        } else {
            return dtoSelector.select();
        }
    }

    private <TypeOverride> DtoSelector<TypeOverride> createDtoSelectorForType(final Class<TypeOverride> typeOverride, final Class<?> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);
        final LitebridgeContext litebridgeContext = createLitebridgeContext(relatedDtoStrategy);

        return new DtoSelector<>(typeOverride, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, litebridgeContext);
    }

    private LitebridgeContext createLitebridgeContext() {
        return createLitebridgeContext(null);
    }

    private LitebridgeContext createLitebridgeContext(final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final LitebridgeContext litebridgeContext = contextSupplier.get();

        if (relatedDtoStrategy != null) {
            litebridgeContext.config().setRelatedDtoStrategy(relatedDtoStrategy);
        }

        return litebridgeContext;
    }

    /**
     * Constructs a DTO-based FROM clause using a context DTO class.
     *
     * @param dtoClass        the DTO class.
     * @param contextDtoClass the context DTO class.
     * @param <DTO>           the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final Class<?> contextDtoClass) {
        final OrmTable table = tableRegistry.getTableInContextOrThrow(dtoClass, contextDtoClass);
        final AliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, aliasGenerator, createLitebridgeContext())
                .select();
    }

    /**
     * Constructs an SQL-based FROM clause.
     *
     * @param expressionSpecs the expression specifications.
     * @param table           the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final ExpressionSpec[] expressionSpecs, final String table) {
        return new SqlSelector(databaseProvider, tableRegistry, createLitebridgeContext()).select(expressionSpecs).from(table);
    }

    private static <DTO> DtoFromClauseTerminal<DTO> select(final ExpressionSpec[] expressionSpecs, final DtoSelector<DTO> dtoSelector) {
        if (expressionSpecs.length > 0) {
            return dtoSelector.select(expressionSpecs);
        } else {
            return dtoSelector.select();
        }
    }
}
