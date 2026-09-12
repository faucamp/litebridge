package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.alias.AliasTransformer;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.ConvertExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.query.TypeConversionMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.exception.NonUniqueResultException;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
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

/**
 * Executes SQL {@code SELECT} statements.
 */
public class SelectEngineTerminal {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectEngineTerminal.class);
    private final DtoConstructor dtoConstructor;

    /**
     * Creates a new {@link SelectEngineTerminal} instance.
     *
     * @param dtoConstructor the {@link DtoConstructor} used to construct DTOs
     */
    public SelectEngineTerminal(final DtoConstructor dtoConstructor) {
        this.dtoConstructor = dtoConstructor;
    }

    /**
     * Executes the query and returns exactly one result.
     * <p>
     * The returned {@link Optional} is empty when no row matches.
     * If more than one row matches, an {@code IllegalStateException} is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return an {@link Optional} containing the single result, if present
     * @throws NonUniqueResultException if the query returns more than one result
     */
    public <DTO> Optional<DTO> fetchOne(final QueryNode node, final LitebridgeContext litebridgeContext) throws NonUniqueResultException {
        return Optional.ofNullable(fetchOneOrNull(node, litebridgeContext));
    }

    /**
     * Executes the query and returns exactly one result, or {@code null} if no row matches.
     * <p>
     * If more than one row matches, an {@link NonUniqueResultException} is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return the single result, or {@code null} when no row matches
     * @throws NonUniqueResultException if more than one row matches
     */
    public <DTO> @Nullable DTO fetchOneOrNull(final QueryNode node, final LitebridgeContext litebridgeContext) throws NonUniqueResultException {
        return fetchOneOrNullImpl(false, node, litebridgeContext);
    }

    /**
     * Executes the query and returns exactly one result.
     * <p>
     * If no row matches, an {@link NoSuchElementException} is thrown.
     * If more than one row matches, an {@link NonUniqueResultException} is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return the single result
     * @throws NoSuchElementException   if no row matches
     * @throws NonUniqueResultException if more than one row matches
     */
    public <DTO> DTO fetchOneOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchOneOrThrow(node, litebridgeContext, () -> new NoSuchElementException("No record found for query"));
    }

    /**
     * Executes the query and returns exactly one result.
     * <p>
     * If no row matches, the supplied exception is thrown.
     * If more than one row matches, an {@link NonUniqueResultException} is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @param exceptionSupplier supplier used to create the exception to throw when the result is not exactly one row
     * @param <X>               exception type
     * @return the single result
     * @throws X                        if no row matches or more than one row matches
     * @throws NonUniqueResultException if more than one row matches
     */
    public <DTO, X extends Throwable> DTO fetchOneOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<? extends X> exceptionSupplier) throws X {
        final DTO result = fetchOneOrNull(node, litebridgeContext);

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    /**
     * Executes the query and returns the first row if present.
     * <p>
     * Unlike {@link #fetchOne(QueryNode, LitebridgeContext)}, this method does not require uniqueness; if multiple rows match,
     * only the first is returned (according to the effective ordering, if any).
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return an {@link Optional} with the first result, if present
     */
    public <DTO> Optional<DTO> fetchFirst(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return Optional.ofNullable(fetchFirstOrNull(node, litebridgeContext));
    }

    /**
     * Executes the query and returns the first row if present, or {@code null} if no row matches.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return the first result, or {@code null} if no row matches
     */
    public <DTO> @Nullable DTO fetchFirstOrNull(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchOneOrNullImpl(true, node, litebridgeContext);
    }

    /**
     * Executes the query and returns the first row.
     * <p>
     * If no row matches, an {@link NoSuchElementException} is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return the first result
     * @throws NoSuchElementException if no row matches
     */
    public <DTO> DTO fetchFirstOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchFirstOrThrow(node, litebridgeContext, () -> new NoSuchElementException("No record found for query"));
    }

    /**
     * Executes the query and returns the first row.
     * <p>
     * If no row matches, the supplied exception is thrown.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @param exceptionSupplier supplier used to create the exception to throw when no row matches
     * @param <X>               exception type
     * @return the first result
     * @throws X if no row matches
     */
    public <DTO, X extends Throwable> DTO fetchFirstOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<? extends X> exceptionSupplier) throws X {
        final DTO result = fetchFirstOrNull(node, litebridgeContext);

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    /**
     * Executes the query and returns results as a {@link Stream}.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return a stream of results
     */
    @SuppressWarnings("unchecked")
    public <DTO> Stream<DTO> fetchStream(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return (Stream<DTO>) fetchList(node, litebridgeContext).stream();
    }

    /**
     * Executes the query and returns a list of results.
     *
     * @param <DTO>             the result type
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return list of all matching results (possibly empty)
     */
    @SuppressWarnings("unchecked")
    public <DTO> List<DTO> fetchList(final QueryNode node, final LitebridgeContext litebridgeContext) {
        final TypeConverter typeConverter = litebridgeContext.typeConverter();
        final List<Row> rows = execute(node, litebridgeContext);
        final SelectNode selectNode = findSelectNode(node);

        if (selectNode.dtoClass() != null) {
            final Class<DTO> dtoClass = getDtoClass(selectNode);
            final OrmTable ormTable;
            final Class<?> contextDtoClass = selectNode.contextDtoClass();

            if (contextDtoClass != null) {
                ormTable = litebridgeContext.tableRegistry().getTableInContextOrThrow(selectNode.dtoClass(), contextDtoClass);
            } else {
                ormTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(selectNode.dtoClass());
            }

            if (dtoClass == ormTable.dtoClass()
                    || ormTable.getDtoClassInterfaces().contains(dtoClass)) {
                // Selecting the actual DTO
                return mapDtos(dtoClass, contextDtoClass, rows, ormTable, litebridgeContext);
            } else if (dtoClass == Row.class) {
                // Multipe type overrides
                return (List<DTO>) rows.stream()
                        .map(row -> convertRowValue(row, selectNode.resultTypes(), typeConverter))
                        .toList();
            } else {
                // Single type override
                return unwrap(dtoClass, rows, litebridgeContext.typeConverter());
            }
        } else {
            final List<Row> resultRows;

            if (selectNode.resultTypes() != null) {
                resultRows = rows.stream()
                        .map(row -> convertRowValue(row, selectNode.resultTypes(), typeConverter))
                        .toList();
            } else {
                resultRows = rows;
            }

            return (List<DTO>) resultRows;
        }
    }

    /**
     * Generates SQL for the query without executing it.
     *
     * @param node              the AST query node representing the query
     * @param litebridgeContext the Litebridge context
     * @return The generated SQL, bind values and query metadata.
     */
    public PreparedSql generateSql(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return generateSqlImpl(null, node, litebridgeContext);
    }

    private @Nullable Row fetchOneRecord(final boolean first, final QueryNode node, final LitebridgeContext litebridgeContext) {
        final List<Row> resultList;

        if (first) {
            final LimitNode limitNode = new LimitNode(node, 1, null);
            resultList = execute(limitNode, litebridgeContext);
        } else {
            resultList = execute(node, litebridgeContext);
        }

        if (CollectionUtils.isEmpty(resultList)) {
            return null;
        }

        if (!first && resultList.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(resultList.size()));
        }

        return resultList.getFirst();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> unwrap(final Class<T> type, final List<Row> rows, final TypeConverter typeConverter) {
        if (type == Row.class) {
            return (List<T>) rows;
        }

        return rows.stream()
                .map(row -> unwrap(type, row.column(0), typeConverter))
                .toList();
    }

    @SuppressWarnings("unchecked")
    private <T> T unwrap(final Class<T> type, final Row.RowColumn rowColumn, final TypeConverter typeConverter) {
        final Object converted = typeConverter.convert(rowColumn.value(), type);
        return (T) converted;
    }

    private Row convertRowValue(final Row row, final @Nullable Class<?>[] resultTypes, final TypeConverter typeConverter) {
        if (row.size() != resultTypes.length) {
            throw new IllegalStateException("Row size " + row.size() + " does not match result type array length " + resultTypes.length);
        }

        for (int i = 0; i < resultTypes.length; i++) {
            final Class<?> resultType = resultTypes[i];

            if (resultType == null) {
                continue;
            }

            final Row.RowColumn rowColumn = row.column(i);
            final Object converted = typeConverter.convert(rowColumn.value(), resultType);
            row.updateColumn(rowColumn.column(), converted);
        }

        return row;
    }

    private List<Row> execute(final QueryNode node, final LitebridgeContext litebridgeContext) {
        final int nodeHash = node.hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);

        if (cachedOperation != null) {
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node, litebridgeContext);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, litebridgeContext);
        }
    }

    private List<Row> compileAndExecute(final int astCacheKey, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile nodes, generate and cache SQL
        final PreparedSql preparedSql = generateSqlImpl(astCacheKey, node, litebridgeContext);

        // Execute SQL query
        return execute(preparedSql, litebridgeContext);
    }

    private List<Row> execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final List<Row> result;

        try {
            result = litebridgeContext.databaseProvider().executeQuery(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute query: " + preparedSql.sql(), ex);
        }

        LOGGER.debug("Row count: {}", result.size());
        LOGGER.trace("Query result: {}", result);
        return result;
    }

    private PreparedSql generateSqlImpl(final @Nullable Integer astCacheKey,
                                        final QueryNode node,
                                        final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        final Operation operation = preparedOperation.operation();

        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(operation, litebridgeContext.transactionManager());
        final TypeConversionMetaData typeConversionMetaData;

        // Cache compiled SQL for this AST
        if (astCacheKey != null) {
            final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                    .map(BindValue::sqlDataType)
                    .toList();
            typeConversionMetaData = createTypeConversionMetaData((Select) operation, litebridgeContext);
            litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, typeConversionMetaData, null));
        } else {
            typeConversionMetaData = null;
        }

        return new PreparedSql(sql, preparedOperation.bindValues(), typeConversionMetaData, null);
    }

    private <DTO> @Nullable DTO fetchOneOrNullImpl(final boolean first, final QueryNode node, final LitebridgeContext litebridgeContext) throws NonUniqueResultException {
        final SelectNode selectNode = findSelectNode(node);

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            // Map the rows to DTOs
            final List<DTO> dtos = fetchList(node, litebridgeContext);

            if (dtos.isEmpty()) {
                return null;
            } else if (!first && dtos.size() > 1) {
                throw new NonUniqueResultException("Expected exactly one mapped result, but got %d".formatted(dtos.size()));
            }

            return dtos.getFirst();
        } else {
            final Row row = fetchOneRecord(first, node, litebridgeContext);

            if (row == null) {
                return null;
            }

            final Row result;

            if (selectNode.resultTypes() != null) {
                result = convertRowValue(row, selectNode.resultTypes(), litebridgeContext.typeConverter());
            } else {
                result = row;
            }

            return (DTO) result;
        }
    }

    private Object mapDto(final SelectNode selectNode,
                          final List<Row> rows,
                          final LitebridgeContext litebridgeContext) {
        final TableRegistry tableRegistry = litebridgeContext.tableRegistry();
        final OrmTable ormTable;
        final Class<?> resultClass;

        if (selectNode.resultTypes() != null) {
            resultClass = selectNode.resultTypes()[0];

            if (selectNode.dtoClass() != null) {
                ormTable = tableRegistry.getOrmTableOrThrow(selectNode.dtoClass());
            } else {
                ormTable = tableRegistry.getOrmTable(Objects.requireNonNull(selectNode.table(), "No DTO class or table name specified"));
            }
        } else if (selectNode.dtoClass() != null) {
            resultClass = selectNode.dtoClass();
            ormTable = tableRegistry.getOrmTableOrThrow(resultClass);
        } else {
            // No mapping required
            return rows;
        }

        if (ormTable != null) {
            final List<Object> dtos = (List<Object>) mapDtos(resultClass, selectNode.contextDtoClass(), rows, ormTable, litebridgeContext);

            if (dtos.size() > 1) {
                throw new IllegalStateException("Expected exactly one mapped result, but got %d".formatted(dtos.size()));
            }

            return dtos.getFirst();
        } else {
            return unwrap(resultClass, rows, litebridgeContext.typeConverter());
        }
    }

    private <DTO> List<DTO> mapDtos(final Class<DTO> dtoClass,
                                    final @Nullable Class<?> contextDtoClass,
                                    final List<Row> rows,
                                    final OrmTable ormTable,
                                    final LitebridgeContext litebridgeContext) {
        final DtoMapper dtoMapper = new DtoMapper(dtoConstructor, litebridgeContext);
        final List<DTO> dtos = dtoMapper.toDtos(dtoClass, contextDtoClass, rows);
        dtos.forEach(ormTable::syncPersistedDto);
        return dtos;
    }

    private TypeConversionMetaData createTypeConversionMetaData(final Select select, final LitebridgeContext litebridgeContext) {
        final Map<String, ColumnMetaData> columnLabelsToColumnMetaData = new HashMap<>(select.expressions().size());
        final Map<String, Table> columnAliasesToTable = new HashMap<>();
        final Class<?>[] typeOverrides = new Class<?>[select.expressions().size()];
        final AliasTransformer aliasTransformer = litebridgeContext.databaseProvider().aliasTransformer();

        for (int i = 0; i < select.expressions().size(); i++) {
            SelectExpression expression = select.expressions().get(i);

            if (expression instanceof ConvertExpression convertExpression) {
                typeOverrides[i] = convertExpression.typeOverride();
                // Process the nested expression (in case it targets a column)
                expression = convertExpression.target();
            }

            if (expression instanceof ColumnExpression columnExpression) {
                final Column column = columnExpression.column();
                final String columnKey = Objects.requireNonNull(aliasTransformer.transformAlias(column.alias() != null ? column.alias() : column.name()));
                final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(column.table());
                final ColumnMetaData columnMetaData = tableMetaData.column(column.name());
                columnLabelsToColumnMetaData.put(columnKey, columnMetaData);
                columnAliasesToTable.put(columnKey, column.table());
            }
        }

        return new TypeConversionMetaData(columnLabelsToColumnMetaData, typeOverrides, columnAliasesToTable);
    }

    private static SelectNode findSelectNode(final QueryNode node) {
        QueryNode currentNode = node;

        do {
            if (currentNode instanceof SelectNode selectNode) {
                return selectNode;
            }

            currentNode = currentNode.previous();
        } while (currentNode != null);

        throw new IllegalArgumentException("No SelectNode found in the query AST");
    }

    @SuppressWarnings("unchecked")
    private static <DTO> Class<DTO> getDtoClass(final SelectNode selectNode) {
        final Class<DTO> dtoClass;

        if (!CollectionUtils.isEmpty(selectNode.resultTypes())) {
            if (selectNode.resultTypes().length == 1
                    && selectNode.resultTypes()[0] != selectNode.dtoClass()) {
                // Single type override
                dtoClass = (Class<DTO>) selectNode.resultTypes()[0];
            } else {
                dtoClass = (Class<DTO>) Row.class;
            }
        } else {
            dtoClass = (Class<DTO>) selectNode.dtoClass();
        }

        return Objects.requireNonNull(dtoClass, "Failed to determine DTO class");
    }
}
