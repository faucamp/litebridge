package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryCompiler;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

public abstract class AbstractSelector<DTO, SSP extends SelectSpec> implements SelectTerminal<DTO> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractSelector.class);
    protected final TransactionalDatabaseProvider databaseProvider;
    protected final TableRegistry tableRegistry;
    protected final Class<DTO> dtoClass;
    protected final LitebridgeContext litebridgeContext;
    protected @Nullable QueryNode node;

    protected AbstractSelector(final TransactionalDatabaseProvider databaseProvider,
                               final TableRegistry tableRegistry,
                               final Class<DTO> dtoClass,
                               final LitebridgeContext litebridgeContext,
                               final @Nullable QueryNode node) {
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
    public PreparedSql toSql() {
        final SSP selectSpec = compile();
        final PreparedOperation preparedOperation = selectSpec.toSelect(litebridgeContext.tableMetaDataCache(), databaseProvider.getTypeConverter());
        final String sql = databaseProvider.toSql(preparedOperation.operation(), databaseProvider.transactionManager());
        return new PreparedSql(sql, preparedOperation.bindValues());
    }

    protected List<Row> executeQuery() {
        final int nodeHash = Objects.requireNonNull(node).hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            // Extract bind values and executed cached query
            final List<@Nullable Object> rawBindValues = QueryBindValueExtractor.extractBindValues(node);
            return executeQuery((Select) cachedOperation.operation(), cachedOperation.preparedSql(rawBindValues));
        } else {
            // Compile and execute query (it will be cached as part of this process)
            return executeQuery(compile(), nodeHash);
        }
    }

    /**
     * Compiles the current query AST into a {@link SelectSpec} instance.
     *
     * @return the compiled select specification
     */
    public SSP compile() {
        //final AliasGenerator freshGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());
        final DefaultAliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());

        final SSP spec = createSelectSpec(aliasGenerator);
        new QueryCompiler(tableRegistry, aliasGenerator).compile(node, spec);
        return spec;
    }

    protected abstract SSP createSelectSpec(final AliasGenerator aliasGenerator);

    protected List<Row> executeQuery(final SSP selectSpec, final int astCacheKey) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = selectSpec.toSelect(litebridgeContext.tableMetaDataCache(), databaseProvider.getTypeConverter());
        final Select select = (Select) preparedOperation.operation();
        // Generate SQL string
        final String sql = databaseProvider.toSql(preparedOperation.operation(), databaseProvider.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, select, bindValueSqlTypes, selectSpec));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues());
        return executeQuery(select, executionSql);
    }

    protected List<Row> executeQuery(final Select select, final PreparedSql executionSql) {
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
