package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.ParameterExtractor;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryCompiler;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO, SSP extends SelectSpec> implements SelectTerminal<DTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    protected final TransactionalDatabaseProvider databaseProvider;
    protected final TableRegistry tableRegistry;
    protected final Class<DTO> dtoClass;
    protected final LitebridgeContext litebridgeContext;
    protected QueryNode node;

    protected AbstractSelector(final TransactionalDatabaseProvider databaseProvider,
                               final TableRegistry tableRegistry,
                               final Class<DTO> dtoClass,
                               final LitebridgeContext litebridgeContext,
                               final QueryNode node) {
        this.databaseProvider = databaseProvider;
        this.tableRegistry = tableRegistry;
        this.dtoClass = dtoClass;
        this.litebridgeContext = litebridgeContext;
        this.node = node;
    }

    protected AbstractSelector(final AbstractSelector<DTO, SSP> delegate, final QueryNode node) {
        this(delegate.databaseProvider, delegate.tableRegistry, delegate.dtoClass, delegate.litebridgeContext, node);
    }

    @Override
    public Optional<DTO> one() {
        return Optional.<DTO>ofNullable(oneOrNull());
    }

    @Override
    public abstract @Nullable DTO oneOrNull();

    @Override
    public DTO oneOrThrow() {
        return oneOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    @Override
    public <X extends Throwable> DTO oneOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return one().orElseThrow(exceptionSupplier);
    }

    @Override
    public Optional<DTO> first() {
        return Optional.<DTO>ofNullable(firstOrNull());
    }

    @Override
    public abstract @Nullable DTO firstOrNull();

    @Override
    public DTO firstOrThrow() {
        return firstOrThrow(() -> new NoSuchElementException("No record found for query"));
    }

    @Override
    public <X extends Throwable> DTO firstOrThrow(final Supplier<? extends X> exceptionSupplier) throws X {
        return first().orElseThrow(exceptionSupplier);
    }

    @Override
    public Stream<DTO> stream() {
        return list().stream();
    }

    @Override
    public abstract List<DTO> list();

    @Override
    public String toSql() {
        final SSP selectSpec = compile();
        return databaseProvider.toSql(selectSpec.toSelect(), databaseProvider.transactionManager());
    }

    protected List<Row> executeQuery() {
        return executeQuery(compile());
    }

    /**
     * Compiles the current query AST into a {@link SelectSpec} instance.
     *
     * @return the compiled select specification
     */
    public SSP compile() {
        final AliasGenerator freshGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        final SSP spec = createSelectSpec(freshGenerator);
        new QueryCompiler(tableRegistry, freshGenerator).compile(node, spec);
        return spec;
    }

    protected abstract SSP createSelectSpec(final AliasGenerator aliasGenerator);

    protected List<Row> executeQuery(final SSP selectSpec) {
        // Execute SQL query
        final Select select = selectSpec.toSelect();

        // Check cache for prepared SQL (structural fingerprint)
        PreparedSql preparedSql = litebridgeContext.queryPlanCache().get(select);

        if (preparedSql == null) {
            preparedSql = databaseProvider.prepareSql(select, databaseProvider.transactionManager());
            litebridgeContext.queryPlanCache().put(select, preparedSql);
        }

        // Extract parameters from CURRENT select record (with actual values)
        final List<BindValue> bindValues = new ParameterExtractor().extractParameters(select);

        // Create execution SQL using cached string and current bind values
        final PreparedSql executionSql = new PreparedSql(preparedSql.sql(), bindValues);

        final List<Row> rows;

        try {
            rows = databaseProvider.select(select, executionSql, databaseProvider.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute select query", ex);
        }

        LOGGER.debug("Row count: {}", rows.size());
        LOGGER.trace("Query result: {}", rows);

        return rows;
    }

    /**
     * Returns the current query node.
     *
     * @return the query node
     */
    public QueryNode node() {
        return node;
    }

    /**
     * Sets the current node to the specified query node.
     *
     * @param node the new query node
     * @return the selector instance
     */
    public abstract AbstractSelector<DTO, SSP> withNode(final QueryNode node);

    public LitebridgeContext litebridgeContext() {
        return litebridgeContext;
    }
}
