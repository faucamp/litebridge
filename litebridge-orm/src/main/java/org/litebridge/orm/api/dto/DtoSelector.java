package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Row;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SelectNode;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.QueryCompiler;
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

/**
 * Selector for DTOs.
 *
 * @param <TypeOverride> the type of the DTO
 */
public final class DtoSelector<TypeOverride> extends AbstractSelector<TypeOverride, DtoSelectSpec> {

    private final OrmTable ormTable;
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
                       final QueryNode node) {
        this(new DtoSelectSpec(typeOverride, ormTable, aliasGenerator, litebridgeContext),
                ormTable,
                tableRegistry,
                classFieldAccessorCache,
                dtoConstructor,
                databaseProvider,
                aliasGenerator,
                litebridgeContext,
                node);
    }

    private DtoSelector(final DtoSelectSpec selectSpec,
                        final OrmTable ormTable,
                        final TableRegistry tableRegistry,
                        final ClassFieldAccessorCache classFieldAccessorCache,
                        final DtoConstructor dtoConstructor,
                        final TransactionalDatabaseProvider databaseProvider,
                        final AliasGenerator aliasGenerator,
                        final LitebridgeContext litebridgeContext,
                        final QueryNode node) {
        super(selectSpec,
                databaseProvider,
                tableRegistry,
                (Class<TypeOverride>) selectSpec.dtoClass(),
                litebridgeContext,
                node);
        this.ormTable = ormTable;
        this.tableRegistry = tableRegistry;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.dtoConstructor = dtoConstructor;
        this.aliasGenerator = aliasGenerator;

        // Ensure SelectExpressionMapper is set
        if (!selectSpec.isTableSet()) {
            selectSpec.setTable(aliasGenerator.aliasTable(ormTable));
        }
        if (!selectSpec.isSelectExpressionMapperSet()) {
            selectSpec.setProtoExpressionResolver(new DtoProtoExpressionResolver(selectSpec, aliasGenerator, classFieldAccessorCache, tableRegistry));
        }
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
        return selectImpl(selectSpec.getTable(), Arrays.stream(expressionSpecs).toList(), selectNode);
    }

    /**
     * Selects all fields of the DTO.
     *
     * @return the from clause terminal
     */
    public DtoFromClauseTerminal<TypeOverride> select() {
        final List<ExpressionSpec> expressionSpecs = createAllFieldsSelectExpressions();
        final QueryNode selectNode = new SelectNode(node, expressionSpecs.toArray(ExpressionSpec[]::new), dtoClass);
        return selectImpl(selectSpec.getTable(), expressionSpecs, selectNode);
    }

    private List<ExpressionSpec> createAllFieldsSelectExpressions() {
        return selectSpec.dtoTable().mappedFieldTargets().stream()
                .filter(entry -> entry.getValue() instanceof ColumnMetaData)
                .map(entry -> (ColumnMetaData) entry.getValue())
                .map(columnMetaData -> {
                    final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData);
                    final FieldAccessor fieldAccessor = selectSpec.dtoTable().getFieldForColumnName(column.name());
                    return (ExpressionSpec) new SelectFieldSpec(fieldAccessor, column);
                })
                .toList();
    }

    private DtoFromClauseTerminal<TypeOverride> selectImpl(final Table table, final List<ExpressionSpec> expressionSpecs, final QueryNode selectNode) {
        selectSpec.setTable(table);
        selectSpec.setProtoExpressionResolver(new DtoProtoExpressionResolver(selectSpec, aliasGenerator, classFieldAccessorCache, tableRegistry));
        selectSpec.setExpressions(expressionSpecs);
        return new DtoFromClauseTerminal<>(new DtoSelector<>(selectSpec, selectSpec.dtoTable(), tableRegistry, classFieldAccessorCache, dtoConstructor, databaseProvider, aliasGenerator, litebridgeContext, selectNode));
    }

    @Override
    public DtoSelector<TypeOverride> withNode(final QueryNode node) {
        return new DtoSelector<>(selectSpec, ormTable, tableRegistry, classFieldAccessorCache, dtoConstructor, databaseProvider, aliasGenerator, litebridgeContext, node);
    }

    OrmTable table() {
        return selectSpec.dtoTable();
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
        final DtoSelectSpec compiledSpec = compile();

        final List<Row> rows = executeQuery(compiledSpec);
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

    @Override
    public DtoSelectSpec selectSpec() {
        return (DtoSelectSpec) super.selectSpec();
    }

    /**
     * Returns the class field accessor cache.
     *
     * @return the class field accessor cache
     */
    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    private @Nullable TypeOverride fetchOneDto(final boolean first) {
        final List<TypeOverride> result;
        if (first) {
            // Use a new selector with a LIMIT node
            result = withNode(new org.litebridge.orm.api.select.ast.LimitNode(node, java.util.Optional.of(1), java.util.Optional.empty())).list();
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
