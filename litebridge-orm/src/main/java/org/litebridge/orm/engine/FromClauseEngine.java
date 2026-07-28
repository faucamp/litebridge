package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.dto.DtoFromClauseTerminal;
import org.litebridge.orm.api.dto.DtoSelector;
import org.litebridge.orm.api.select.ast.FromNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.sql.SqlFromClauseTerminal;
import org.litebridge.orm.api.sql.SqlSelectSpec;
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

import java.util.Arrays;
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
    }

    public <DTO> DtoFromClauseTerminal<DTO> from(final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        return from(null, dtoClass, relatedDtoStrategy);
    }

    /**
     * Constructs a DTO-based FROM clause.
     *
     * @param node               the current query node.
     * @param dtoClass           the DTO class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @param <DTO>              the DTO type.
     * @return the DTO from clause terminal.
     */
    public <DTO> DtoFromClauseTerminal<DTO> from(final @Nullable SelectNode node, final Class<DTO> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final QueryNode fromNode = new FromNode(node, dtoClass, null, null, relatedDtoStrategy);
        final DtoSelector<DTO> dtoSelector = createDtoSelectorForType(dtoClass, dtoClass, relatedDtoStrategy, fromNode);

        if (node != null && node.expressions().length > 0) {
            return new DtoFromClauseTerminal<>(dtoSelector);
        } else {
            return dtoSelector.select();
        }
    }

    /**
     * Constructs a DTO-based FROM clause with a type override.
     *
     * @param node               the current query node.
     * @param dtoClass           the DTO class.
     * @param typeOverrideClass  the type override class.
     * @param relatedDtoStrategy the related DTO strategy.
     * @param <TypeOverride>      the type override.
     * @return the DTO from clause terminal.
     */
    public <TypeOverride> DtoFromClauseTerminal<TypeOverride> from(final QueryNode node, final Class<?> dtoClass, final Class<TypeOverride> typeOverrideClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final QueryNode fromNode = new FromNode(node, dtoClass, null, null, relatedDtoStrategy);
        final DtoSelector<TypeOverride> dtoSelector = createDtoSelectorForType(typeOverrideClass, dtoClass, relatedDtoStrategy, fromNode);

        if (hasExplicitSelect(node)) {
            return new DtoFromClauseTerminal<>(dtoSelector);
        } else {
            return dtoSelector.select();
        }
    }

    private <TypeOverride> DtoSelector<TypeOverride> createDtoSelectorForType(final Class<TypeOverride> typeOverride, final Class<?> dtoClass, final @Nullable RelatedDtoStrategy relatedDtoStrategy, final QueryNode node) {
        final OrmTable table = tableRegistry.getTableOrThrow(dtoClass);
        final LitebridgeContext litebridgeContext = createLitebridgeContext(relatedDtoStrategy);

        return new DtoSelector<>(typeOverride, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, litebridgeContext.aliasGenerator(), litebridgeContext, node);
    }

    private LitebridgeContext createLitebridgeContext() {
        return createLitebridgeContext(null);
    }

    private LitebridgeContext createLitebridgeContext(final @Nullable RelatedDtoStrategy relatedDtoStrategy) {
        final LitebridgeContext litebridgeContext = contextSupplier.get();

        if (relatedDtoStrategy != null) {
            litebridgeContext.setRelatedDtoStrategy(relatedDtoStrategy);
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
        final LitebridgeContext litebridgeContext = createLitebridgeContext();
        final QueryNode fromNode = new FromNode(null, dtoClass, contextDtoClass, null, null); // Root from context
        return new DtoSelector<>(dtoClass, table, tableRegistry, changeTracker.classFieldAccessorCache(), dtoConstructor, databaseProvider, litebridgeContext.aliasGenerator(), litebridgeContext, fromNode)
                .select();
    }

    /**
     * Constructs an SQL-based FROM clause.
     *
     * @param node  the current query node.
     * @param table the table name.
     * @return the SQL from clause terminal.
     */
    public SqlFromClauseTerminal from(final SelectNode node, final String table) {
        final QueryNode fromNode = new FromNode(node, null, null, table, null);
        final LitebridgeContext litebridgeContext = createLitebridgeContext();
        final SqlSelectSpec selectSpec = new SqlSelectSpec(litebridgeContext);
        selectSpec.addExpressions(Arrays.asList(node.expressions()));
        litebridgeContext.setSelectSpec(selectSpec);
        return new SqlSelector(new Table(table), databaseProvider, tableRegistry, litebridgeContext, fromNode).select().from(table);
    }

    private static <DTO> DtoFromClauseTerminal<DTO> select(final ExpressionSpec[] expressionSpecs, final DtoSelector<DTO> dtoSelector) {
        if (expressionSpecs.length > 0) {
            return dtoSelector.select(expressionSpecs);
        } else {
            return dtoSelector.select();
        }
    }
    public AliasGenerator aliasGenerator() {
        return contextSupplier.get().aliasGenerator();
    }

    private boolean hasExplicitSelect(final QueryNode node) {
        QueryNode current = node;

        while (current != null) {
            if (current instanceof SelectNode selectNode && selectNode.expressions().length > 0) {
                return true;
            }

            current = current.previous();
        }

        return false;
    }
}
