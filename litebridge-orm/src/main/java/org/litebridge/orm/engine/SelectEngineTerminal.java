package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Collections;
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
        final Row row = fetchOneRecord(false, node, litebridgeContext);

        if (row == null) {
            return null;
        }

        // Map the rows to DTOs
        final SelectNode selectNode = findSelectNode(node);

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (DTO) mapDto(selectNode, row, litebridgeContext);
        } else {
            return (DTO) row;
        }
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
        final Row row = fetchOneRecord(true, node, litebridgeContext);

        if (row == null) {
            return null;
        }

        final SelectNode selectNode = findSelectNode(node);

        if (litebridgeContext.mode() == LitebridgeContext.Mode.DTO) {
            return (DTO) mapDto(selectNode, row, litebridgeContext);
        } else {
            return (DTO) row;
        }
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

    public <DTO> List<DTO> fetchList(final QueryNode node, final LitebridgeContext litebridgeContext) {
        // DtoSelector
        final List<Row> rows = execute(node, litebridgeContext);

        final SelectNode selectNode = findSelectNode(node);

        if (selectNode.dtoClass() != null) {
            final Class<DTO> dtoClass = (Class<DTO>) selectNode.dtoClass();
            final OrmTable ormTable = litebridgeContext.tableRegistry().getTableOrThrow(selectNode.dtoClass());

            if (dtoClass == ormTable.dtoClass()
                    || ormTable.getDtoClassInterfaces().contains(dtoClass)) {
                // Selecting the actual DTO
                return mapDtos(dtoClass, rows, ormTable, litebridgeContext);
            } else {
                return unwrap(dtoClass, rows, litebridgeContext.typeConverter());
            }
        } else {
            return (List<DTO>) rows;
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

        return (List<T>) rows.stream()
                .filter(row -> row.size() > 0)
                .map(row -> {
                    final Object converted = typeConverter.convert(row.column(0).value(), type);
                    return converted;
                })
                .filter(Objects::nonNull)
                .toList();
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

    private Object mapDto(final SelectNode selectNode,
                          final Row row,
                          final LitebridgeContext litebridgeContext) {
        final TableRegistry tableRegistry = litebridgeContext.tableRegistry();
        final OrmTable ormTable;
        final Class<?> resultClass;

        if (selectNode.resultType() != null) {
            resultClass = selectNode.resultType();

            if (selectNode.dtoClass() != null) {
                ormTable = tableRegistry.getTableOrThrow(selectNode.dtoClass());
            } else {
                ormTable = tableRegistry.getOrmTable(Objects.requireNonNull(selectNode.table(), "No DTO class or table name specified"));
            }
        } else if (selectNode.dtoClass() != null) {
            resultClass = selectNode.dtoClass();
            ormTable = tableRegistry.getTableOrThrow(resultClass);
        } else {
            // No mapping required
            return row;
        }

        if (ormTable != null) {
            return mapDtos(resultClass, Collections.singletonList(row), ormTable, litebridgeContext).getFirst();
        } else {
            return unwrap(resultClass, Collections.singletonList(row), litebridgeContext.typeConverter()).getFirst();
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
