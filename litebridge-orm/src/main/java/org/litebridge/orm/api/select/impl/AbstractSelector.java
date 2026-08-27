package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.SelectTerminal;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.compiler.QueryCompiler;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.orm.persistence.alias.DefaultAliasGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

@Deprecated(forRemoval = true)
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
            return executeQuery(cachedOperation.preparedSql(rawBindValues));
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
        final DefaultAliasGenerator aliasGenerator = new DefaultAliasGenerator(databaseProvider.getAliasTransformer());

        final SSP spec = createSelectSpec(aliasGenerator);
        new QueryCompiler(litebridgeContext);
        return spec;
    }

    protected abstract SSP createSelectSpec(final AliasGenerator aliasGenerator);

    protected List<Row> executeQuery(final SSP selectSpec, final int astCacheKey) {
//        // Compile/prepare SQL query
//        final PreparedOperation preparedOperation = selectSpec.toSelect(litebridgeContext.tableMetaDataCache(), databaseProvider.getTypeConverter());
//        final Select select = (Select) preparedOperation.operation();
//        // Generate SQL and create type conversion metadata
//        final String sql = databaseProvider.toSql(preparedOperation.operation(), databaseProvider.transactionManager());
//        final TypeConversionMetaData typeConversionMetaData = createTypeConversionMetaData(select);
//        // Cache compiled SQL for this AST
//        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
//                .map(BindValue::sqlDataType)
//                .toList();
//        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, typeConversionMetaData, null, selectSpec));
//        // Execute SQL query
//        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), typeConversionMetaData, null);
//        return executeQuery(executionSql);
        throw new UnsupportedOperationException("Deprecated");
    }

    protected List<Row> executeQuery(final PreparedSql preparedSql) {
        final List<Row> rows;

        try {
            rows = databaseProvider.select(preparedSql, databaseProvider.transactionManager());
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
    public @Nullable QueryNode node() {
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

    private TypeConversionMetaData createTypeConversionMetaData(final Select select) {
        final Map<String, ColumnMetaData> columnLabelsToColumnMetaData = new HashMap<>(select.expressions().size());
        final Class<?>[] typeOverrides = new Class<?>[select.expressions().size()];

        for (int i = 0; i < select.expressions().size(); i++) {
            SelectExpression expression = select.expressions().get(i);

            if (expression instanceof ConvertExpression convertExpression) {
                typeOverrides[i] = convertExpression.typeOverride();
                // Process the nested expression (in case it targets a column)
                expression = convertExpression.target();
            }

            if (expression instanceof ColumnExpression columnExpression) {
                final Column column = columnExpression.column();
                final String key = Objects.requireNonNull(databaseProvider.getAliasTransformer().transformAlias(column.alias() != null ? column.alias() : column.name()));
                final TableMetaData table = litebridgeContext.tableMetaDataCache().ensureTableMetaData(column.table());
                final ColumnMetaData columnMetaData = table.column(column.name());
                columnLabelsToColumnMetaData.put(key, columnMetaData);
            }
        }

        return new TypeConversionMetaData(columnLabelsToColumnMetaData, typeOverrides);
    }
}
