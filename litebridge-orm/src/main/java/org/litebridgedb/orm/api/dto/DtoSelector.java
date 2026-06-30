package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Row;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.convert.TypeConverter;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.engine.LitebridgeContext;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.SelectSpecDtoMapper;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class DtoSelector<TypeOverride> extends AbstractSelector<TypeOverride, DtoSelectSpec> {

    private final TableRegistry tableRegistry;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final DtoConstructor dtoConstructor;
    private final AliasGenerator aliasGenerator;

    public DtoSelector(final Class<TypeOverride> typeOverride,
                       final OrmTable ormTable,
                       final TableRegistry tableRegistry,
                       final ClassFieldAccessorCache classFieldAccessorCache,
                       final DtoConstructor dtoConstructor,
                       final TransactionalDatabaseProvider databaseProvider,
                       final AliasGenerator aliasGenerator,
                       final LitebridgeContext litebridgeContext) {
        super(new DtoSelectSpec(typeOverride, ormTable, aliasGenerator, litebridgeContext),
                databaseProvider,
                typeOverride,
                litebridgeContext);
        this.tableRegistry = tableRegistry;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.dtoConstructor = dtoConstructor;
        this.aliasGenerator = aliasGenerator;
    }

    public DtoFromClauseTerminal<TypeOverride> select(final ExpressionSpec... expressionSpecs) {
        return selectImpl(selectSpec.getTable(), Arrays.stream(expressionSpecs).toList());
    }

    public DtoFromClauseTerminal<TypeOverride> select() {
        return selectImpl(selectSpec.getTable(), createAllFieldsSelectExpressions());
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

    private DtoFromClauseTerminal<TypeOverride> selectImpl(final Table table, final List<ExpressionSpec> expressionSpecs) {
        assert table.alias() != null;
        selectSpec.setTable(table);
        selectSpec.setProtoExpressionResolver(new DtoProtoExpressionResolver(selectSpec, aliasGenerator, classFieldAccessorCache, tableRegistry));
        selectSpec.setDtoAlias(selectSpec.dtoClass(), Objects.requireNonNull(table.alias()));
        selectSpec.setExpressions(expressionSpecs);
        return new DtoFromClauseTerminal<>(this);
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
        final List<Row> rows = executeQuery();

        if (dtoClass == selectSpec.dtoTable().dtoClass()
                || selectSpec.dtoTable().getDtoClassInterfaces().contains(dtoClass)) {
            // Selecting the actual DTO
            final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter(), tableRegistry, dtoConstructor, litebridgeContext);
            return selectSpecDtoMapper.toDtos(dtoClass, rows);
        } else {
            // Type overridden-select (e.g. by a SQL function); <DTO> generic is not set to the actual DTO class
            return unwrap(dtoClass, rows);
        }
    }

    @Override
    protected DtoSelectSpec selectSpec() {
        return super.selectSpec();
    }

    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    private @Nullable TypeOverride fetchOneDto(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<TypeOverride> result = list();

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

        final TypeConverter typeConverter = databaseProvider.getTypeConverter();
        return rows.stream()
                .map(row -> typeConverter.convert(row.column(0).value(), type))
                .filter(Objects::nonNull)
                .toList();
    }
}
