package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.CollectionUtils;
import org.litebridgedb.db.spi.Aliased;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.api.select.impl.AbstractSelector;
import org.litebridgedb.orm.config.LitebridgeConfig;
import org.litebridgedb.orm.persistence.DtoConstructor;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.SelectSpecDtoMapper;
import org.litebridgedb.orm.persistence.TableRegistry;
import org.litebridgedb.orm.persistence.TransactionalDatabaseProvider;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public final class DtoSelector<DTO> extends AbstractSelector<DTO, DtoSelectSpec> {

    private final TableRegistry tableRegistry;
    private final ClassFieldAccessorCache classFieldAccessorCache;
    private final DtoConstructor dtoConstructor;
    private final AliasGenerator aliasGenerator;

    public DtoSelector(final Class<DTO> dtoClass,
                       final OrmTable dtoTable,
                       final TableRegistry tableRegistry,
                       final ClassFieldAccessorCache classFieldAccessorCache,
                       final DtoConstructor dtoConstructor,
                       final TransactionalDatabaseProvider databaseProvider,
                       final AliasGenerator aliasGenerator,
                       final LitebridgeConfig litebridgeConfig) {
        super(new DtoSelectSpec(dtoClass, dtoTable, aliasGenerator, databaseProvider.getSqlFunctionRegistry()),
                databaseProvider,
                dtoClass,
                litebridgeConfig);
        this.tableRegistry = tableRegistry;
        this.classFieldAccessorCache = classFieldAccessorCache;
        this.dtoConstructor = dtoConstructor;
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
    public List<DTO> list() {
        final SelectSpecDtoMapper selectSpecDtoMapper = new SelectSpecDtoMapper(selectSpec, databaseProvider.getTypeConverter(), tableRegistry, dtoConstructor, litebridgeConfig);
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
