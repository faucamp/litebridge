package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.persistence.AliasGenerator;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.SelectSpecDtoMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.orm.persistence.TransactionalDatabaseProvider;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

public final class DtoSelector<DTO> extends AbstractSelector<DTO, DtoSelectSpec> {

    private final TableRegistry tableRegistry;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final AliasGenerator aliasGenerator;

    public DtoSelector(final Class<DTO> dtoClass,
                       final OrmTable dtoTable,
                       final TableRegistry tableRegistry,
                       final ClassFieldAccessorCache classFieldAccessorCache,
                       final TransactionalDatabaseProvider databaseProvider,
                       final AliasGenerator aliasGenerator) {
        super(new DtoSelectSpec(dtoClass, dtoTable, aliasGenerator), databaseProvider, dtoClass);
        this.tableRegistry = tableRegistry;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.aliasGenerator = aliasGenerator;
    }

    public DtoFromClauseTerminal<DTO> select(final String... fields) {
        return selectImpl(selectSpec.getTable(), Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData columnMetaData = selectSpec.dtoTable().getColumnForFieldName(field);
                    return new DtoSelectSpec.FieldColumn(classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, field), aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData));
                })
                .toList());
    }

    public DtoFromClauseTerminal<DTO> select(final Aliased... fields) {
        return selectImpl(selectSpec.getTable(), Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData columnMetaData = selectSpec.dtoTable().getColumnForFieldName(field.name());
                    return new DtoSelectSpec.FieldColumn(classFieldAccessorCache.fieldAccessorOrThrow(dtoClass, field.name()), aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData));
                })
                .toList());
    }

    public DtoFromClauseTerminal<DTO> select() {
        return selectImpl(selectSpec.getTable(), selectSpec.dtoTable().mappedFieldTargets().stream()
                .filter(entry -> entry.getValue() instanceof ColumnMetaData)
                .map(entry -> (ColumnMetaData) entry.getValue())
                .map(columnMetaData -> {
                    final Column column = aliasGenerator.aliasColumn(selectSpec.getTable(), columnMetaData);
                    return new DtoSelectSpec.FieldColumn(selectSpec.dtoTable().getFieldForColumnName(column.name()), column);
                })
                .toList());
    }

    private DtoFromClauseTerminal<DTO> selectImpl(final Table table, final List<DtoSelectSpec.FieldColumn> fieldColumns) {
        assert table.alias() != null;
        selectSpec.setTable(table);
        selectSpec.setDtoAlias(selectSpec.dtoClass(), Objects.requireNonNull(table.alias()));
        selectSpec.setFieldColumns(fieldColumns);
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
    public @Nullable DTO oneOrNull() {
        return fetchOneDto(false);
    }

    @Override
    public @Nullable DTO firstOrNull() {
        return fetchOneDto(true);
    }

    @Override
    public Stream<DTO> stream() {
        return list().stream();
    }

    @Override
    public List<DTO> list() {
        final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter());
        return selectSpecDtoMapper.toDtos(dtoClass, executeQuery());
    }

    @Override
    protected DtoSelectSpec selectSpec() {
        return super.selectSpec();
    }

    public ClassFieldAccessorCache classFieldAccessorCache() {
        return classFieldAccessorCache;
    }

    private @Nullable DTO fetchOneDto(final boolean first) {
        if (first) {
            // Set LIMIT since we are only interested in the first record
            selectSpec.ensureLimit().setLimit(1);
        }

        final List<DTO> result = list();

        if (CollectionUtils.isEmpty(result)) {
            return null;
        }

        if (!first && result.size() > 1) {
            throw new IllegalStateException("Expected exactly one result, but got %d".formatted(result.size()));
        }

        return result.getFirst();
    }
}
