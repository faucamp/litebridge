package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.engine.ast.LimitNode;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.SelectNode;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Provides methods for constructing SQL SELECT statements in a fluent, object-oriented manner.
 * <p>
 * This class supports the selection of data transfer objects (DTOs), raw fields/columns, and custom expressions with optional
 * support for related DTO strategies and contextual mappings.
 */
public class SelectEngineTerminal {

    private static final Logger LOGGER = LoggerFactory.getLogger(SelectEngineTerminal.class);
    private final DtoConstructor dtoConstructor;

    public SelectEngineTerminal(final DtoConstructor dtoConstructor) {
        this.dtoConstructor = dtoConstructor;
    }

    public <DTO> Optional<DTO> fetchOne(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return Optional.<DTO>ofNullable(fetchOneOrNull(node, litebridgeContext));
    }

    public <DTO> @Nullable DTO fetchOneOrNull(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchOneOrNullImpl(false, node, litebridgeContext);
    }

    public <DTO> DTO fetchOneOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchOneOrThrow(node, litebridgeContext, () -> new NoSuchElementException("No record found for query"));
    }

    public <DTO, X extends Throwable> DTO fetchOneOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<? extends X> exceptionSupplier) throws X {
        final DTO result = fetchOneOrNull(node, litebridgeContext);

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    public <DTO> Optional<DTO> fetchFirst(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return Optional.<DTO>ofNullable(fetchFirstOrNull(node, litebridgeContext));
    }

    public <DTO> @Nullable DTO fetchFirstOrNull(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchOneOrNullImpl(true, node, litebridgeContext);
    }

    public <DTO> DTO fetchFirstOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return fetchFirstOrThrow(node, litebridgeContext, () -> new NoSuchElementException("No record found for query"));
    }

    public <DTO, X extends Throwable> DTO fetchFirstOrThrow(final QueryNode node, final LitebridgeContext litebridgeContext, final Supplier<? extends X> exceptionSupplier) throws X {
        final DTO result = fetchFirstOrNull(node, litebridgeContext);

        if (result == null) {
            throw exceptionSupplier.get();
        }

        return result;
    }

    public <DTO> Stream<DTO> fetchStream(final QueryNode node, final LitebridgeContext litebridgeContext) {
        return (Stream<DTO>) fetchList(node, litebridgeContext).stream();
    }

    @SuppressWarnings("unchecked")
    public <DTO> List<DTO> fetchList(final QueryNode node, final LitebridgeContext litebridgeContext) {
        final TypeConverter typeConverter = litebridgeContext.typeConverter();
        final List<Row> rows = execute(node, litebridgeContext);
        final SelectNode selectNode = findSelectNode(node);

        if (selectNode.dtoClass() != null) {
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

            final OrmTable ormTable = litebridgeContext.tableRegistry().getOrmTableOrThrow(selectNode.dtoClass());

            if (dtoClass == ormTable.dtoClass()
                    || ormTable.getDtoClassInterfaces().contains(dtoClass)) {
                // Selecting the actual DTO
                return mapDtos(dtoClass, rows, ormTable, litebridgeContext);
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
            final List<@Nullable Object> bindValues = QueryBindValueExtractor.extractBindValues(node);
            return execute(cachedOperation.preparedSql(bindValues), litebridgeContext);
        } else {
            return compileAndExecute(nodeHash, node, litebridgeContext);
        }
    }

    private List<Row> compileAndExecute(final int astCacheKey, final QueryNode node, final LitebridgeContext litebridgeContext) {
        // Compile/prepare SQL query
        final PreparedOperation preparedOperation = litebridgeContext.createQueryCompiler().compile(node);
        final Select select = (Select) preparedOperation.operation();
        // Generate SQL and create type conversion metadata
        final String sql = litebridgeContext.databaseProvider().toSql(select, litebridgeContext.transactionManager());
        // Cache compiled SQL for this AST
        final List<Integer> bindValueSqlTypes = preparedOperation.bindValues().stream()
                .map(BindValue::sqlDataType)
                .toList();
        litebridgeContext.queryPlanCache().put(astCacheKey, new QueryPlanCache.CachedOperation(sql, bindValueSqlTypes, null, null));
        // Execute SQL query
        final PreparedSql executionSql = new PreparedSql(sql, preparedOperation.bindValues(), null, null);
        return execute(executionSql, litebridgeContext);
    }

    private List<Row> execute(final PreparedSql preparedSql, final LitebridgeContext litebridgeContext) {
        final List<Row> result;

        try {
            result = litebridgeContext.databaseProvider().select(preparedSql, litebridgeContext.transactionManager());
        } catch (final SQLException ex) {
            throw new IllegalStateException("Failed to execute update: " + preparedSql.sql(), ex);
        }

        LOGGER.debug("Row count: {}", result.size());
        LOGGER.trace("Query result: {}", result);
        return result;
    }

    private <DTO> @Nullable DTO fetchOneOrNullImpl(final boolean first, final QueryNode node, final LitebridgeContext litebridgeContext) {
        final SelectNode selectNode = findSelectNode(node);

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            // Map the rows to DTOs
            final List<DTO> dtos = fetchList(node, litebridgeContext);

            if (dtos.isEmpty()) {
                return null;
            } else if (!first && dtos.size() > 1) {
                throw new IllegalStateException("Expected exactly one mapped result, but got %d".formatted(dtos.size()));
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
            final List<Object> dtos = (List<Object>) mapDtos(resultClass, rows, ormTable, litebridgeContext);

            if (dtos.size() > 1) {
                throw new IllegalStateException("Expected exactly one mapped result, but got %d".formatted(dtos.size()));
            }

            return dtos.getFirst();
        } else {
            return unwrap(resultClass, rows, litebridgeContext.typeConverter());
        }
    }

    private <DTO> List<DTO> mapDtos(final Class<DTO> dtoClass,
                                    final List<Row> rows,
                                    final OrmTable ormTable,
                                    final LitebridgeContext litebridgeContext) {
        final DtoMapper dtoMapper = new DtoMapper(dtoConstructor, litebridgeContext);
        final List<DTO> dtos = dtoMapper.toDtos(dtoClass, rows);
        dtos.forEach(ormTable::syncPersistedDto);
        return dtos;
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
}
