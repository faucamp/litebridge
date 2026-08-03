package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Select;
import org.litebridge.orm.api.select.ast.LimitNode;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryBindValueExtractor;
import org.litebridge.orm.engine.QueryPlanCache;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.DtoConstructor;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.SelectSpecDtoMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.orm.persistence.alias.AliasGenerator;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Selector for DTOs.
 *
 * @param <TypeOverride> the type of the DTO
 */
public final class DtoSelector<TypeOverride> extends AbstractSelector<TypeOverride, DtoSelectSpec> {

    private final OrmTable ormTable;
    private final Table table;
    private final TableRegistry tableRegistry;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final DtoConstructor dtoConstructor;
    private final AliasGenerator aliasGenerator;

    /**
     * Creates a new DtoSelector.
     *
     * @param typeOverride            the type of the DTO
     * @param ormTable                the ORM table
     * @param tableRegistry           the table registry
     * @param classFieldAccessorCache the class field accessor cache
     * @param dtoConstructor          the DTO constructor
     * @param databaseProvider        the database provider
     * @param aliasGenerator          the alias generator
     * @param litebridgeContext       the litebridge context
     */
    public DtoSelector(final Class<TypeOverride> typeOverride,
                       final OrmTable ormTable,
                       final TableRegistry tableRegistry,
                       final ClassFieldAccessorCache classFieldAccessorCache,
                       final DtoConstructor dtoConstructor,
                       final TransactionalDatabaseProvider databaseProvider,
                       final AliasGenerator aliasGenerator,
                       final LitebridgeContext litebridgeContext,
                       final @Nullable QueryNode node) {
        super(databaseProvider,
                tableRegistry,
                typeOverride,
                litebridgeContext,
                node);
        this.ormTable = ormTable;
        this.table = ormTable.getMetaData().toTable();
        this.tableRegistry = tableRegistry;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.dtoConstructor = dtoConstructor;
        this.aliasGenerator = aliasGenerator;
    }

    @Override
    protected DtoSelectSpec createSelectSpec(final AliasGenerator aliasGenerator) {
        final DtoSelectSpec selectSpec = new DtoSelectSpec(dtoClass, ormTable, aliasGenerator, litebridgeContext);
        selectSpec.setProtoExpressionResolver(new DtoProtoExpressionResolver(selectSpec, aliasGenerator, classFieldAccessorCache, tableRegistry));
        return selectSpec;
    }

    /**
     * Selects specific fields.
     *
     * @param expressionSpecs the expression specifications to select
     * @return the from clause terminal
     */
    public DtoFromClauseTerminal<TypeOverride> select(final ExpressionSpec... expressionSpecs) {
        final QueryNode selectNode = new SelectNode(node, expressionSpecs, dtoClass);
        return new DtoFromClauseTerminal<>(withNode(selectNode));
    }

    /**
     * Selects all fields of the DTO.
     *
     * @return the from clause terminal
     */
    public DtoFromClauseTerminal<TypeOverride> select() {
        final List<ExpressionSpec> expressionSpecs = createAllFieldsSelectExpressions();
        final QueryNode selectNode = new SelectNode(node, expressionSpecs.toArray(ExpressionSpec[]::new), dtoClass);
        return new DtoFromClauseTerminal<>(withNode(selectNode));
    }

    private List<ExpressionSpec> createAllFieldsSelectExpressions() {
        return ormTable.mappedFieldTargets().stream()
                .filter(entry -> entry.getValue() instanceof ColumnMetaData)
                .map(entry -> (ColumnMetaData) entry.getValue())
                .map(columnMetaData -> {
                    final Column column = columnMetaData.toColumn();
                    final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(column.name());
                    return (ExpressionSpec) new SelectFieldSpec(fieldAccessor, column);
                })
                .toList();
    }

    @Override
    public DtoSelector<TypeOverride> withNode(final QueryNode node) {
        this.node = node;
        return this;
    }

    OrmTable ormTable() {
        return ormTable;
    }

    TableRegistry tableRegistry() {
        return tableRegistry;
    }

    AliasGenerator dtoAliasRegistry() {
        return aliasGenerator;
    }

    @Override
    public @Nullable TypeOverride oneOrNull() {
        return fetchOneDto(false);
    }

    @Override
    public @Nullable TypeOverride firstOrNull() {
        return fetchOneDto(true);
    }

    @Override
    public List<TypeOverride> list() {
        final int nodeHash = Objects.requireNonNull(node).hashCode();
        final QueryPlanCache.CachedOperation cachedOperation = litebridgeContext.queryPlanCache().get(nodeHash);
        final List<Row> rows;
        final DtoSelectSpec compiledSpec;

        if (cachedOperation != null) {
            // Extract bind values and executed cached query
            final List<@Nullable Object> rawBindValues = QueryBindValueExtractor.extractBindValues(node);
            rows = executeQuery((Select) cachedOperation.operation(), cachedOperation.preparedSql(rawBindValues));
            compiledSpec = (DtoSelectSpec) Objects.requireNonNull(cachedOperation.selectSpec());
        } else {
            // Compile and execute query (it will be cached as part of this process)
            compiledSpec = compile();
            rows = executeQuery(compiledSpec, nodeHash);
        }

        final OrmTable ormTable = compiledSpec.dtoTable();

        if (dtoClass == ormTable.dtoClass()
                || ormTable.getDtoClassInterfaces().contains(dtoClass)) {
            // Selecting the actual DTO
            final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(compiledSpec, databaseProvider.getTypeConverter(), tableRegistry, dtoConstructor, litebridgeContext);
            final List<TypeOverride> dtos = selectSpecDtoMapper.toDtos(dtoClass, rows);
            dtos.forEach(ormTable::syncPersistedDto);
            return dtos;
        } else {
            return unwrap(dtoClass, rows);
        }
    }

    /**
     * Returns the class field accessor cache.
     *
     * @return the class field accessor cache
     */
    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    public Table table() {
        return table;
    }

    /**
     * Creates select field specifications for the given fields.
     *
     * @param fields the field names
     * @return the list of expression specifications
     */
    public List<ExpressionSpec> createSelectFieldSpecs(final String[] fields) {
        return Arrays.stream(fields)
                .map(this::createSelectFieldSpec)
                .toList();
    }

    private ExpressionSpec createSelectFieldSpec(final String field) {
        final ColumnMetaData columnMetaData = ormTable.getColumnForFieldName(field);
        final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(columnMetaData.name());
        return new SelectFieldSpec(fieldAccessor, columnMetaData.toColumn());
    }

    private @Nullable TypeOverride fetchOneDto(final boolean first) {
        final List<TypeOverride> result;
        if (first) {
            // Use a new selector with a LIMIT node
            result = withNode(new LimitNode(node, Optional.of(1), Optional.empty())).list();
        } else {
            result = list();
        }

        if (CollectionUtils.isEmpty(result)) {
            return null;
        }

        if (!first && result.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(result.size()));
        }

        return result.getFirst();
    }

    @SuppressWarnings("unchecked")
    private <T> List<T> unwrap(final Class<T> type, final List<Row> rows) {
        if (type == Row.class) {
            return (List<T>) rows;
        }

        final org.litebridge.db.spi.convert.TypeConverter typeConverter = databaseProvider.getTypeConverter();
        return rows.stream()
                .map(row -> {
                    if (row.size() == 0) return null;
                    final Object converted = typeConverter.convert(row.column(0).value(), type);
                    return type.cast(converted);
                })
                .filter(Objects::nonNull)
                .toList();
    }
}
