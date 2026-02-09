package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.SelectSpecDtoMapper;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ClassFieldAccessorCache;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

public final class DtoSelector<DTO> extends AbstractSelector<DTO, DtoSelectSpec> {

    private final OrmTable table;
    private final TableRegistry tableRegistry;
    private final DtoAliasRegistry dtoAliasRegistry;

    public DtoSelector(final Class<DTO> dtoClass,
                       final OrmTable dtoTable,
                       final TableRegistry tableRegistry,
                       final DatabaseProvider databaseProvider,
                       final DtoAliasRegistry dtoAliasRegistry) {
        super(new DtoSelectSpec(dtoClass, dtoTable), databaseProvider, dtoClass);
        this.table = dtoTable;
        this.tableRegistry = tableRegistry;
        this.dtoAliasRegistry = dtoAliasRegistry;
    }

    public DtoFromClauseTerminal<DTO> select(final String... fields) {
        final String tableAlias = dtoAliasRegistry.newAlias(table.getMetaData());
        final TableMetaData aliasedTable = table.getMetaData().as(tableAlias);

        return selectImpl(aliasedTable, Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field);
                    return new DtoSelectSpec.FieldColumn(ClassFieldAccessorCache.fieldAccessorOrThrow(dtoClass, field),
                            new Column(table.getMetaData().as(tableAlias), column.name(), dtoAliasRegistry.alias(tableAlias, column)));
                }));
    }

    public DtoFromClauseTerminal<DTO> select(final Aliased... fields) {
        final String tableAlias = dtoAliasRegistry.newAlias(table.getMetaData());
        final TableMetaData aliasedTable = table.getMetaData().as(tableAlias);

        return selectImpl(aliasedTable, Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field.name());
                    return new DtoSelectSpec.FieldColumn(ClassFieldAccessorCache.fieldAccessorOrThrow(dtoClass, field.name()),
                            new Column(aliasedTable, column.name(), dtoAliasRegistry.alias(tableAlias, column)));
                }));
    }

    public DtoFromClauseTerminal<DTO> select() {
        final String tableAlias = dtoAliasRegistry.newAlias(table.getMetaData());
        final TableMetaData aliasedTable = table.getMetaData().as(tableAlias);

        return selectImpl(aliasedTable, table.getMetaData().columns().stream()
                .map(column ->
                        new DtoSelectSpec.FieldColumn(table.getFieldForColumnName(column.name()),
                                new Column(aliasedTable, column.name(), dtoAliasRegistry.alias(tableAlias, column)))));
    }

    private DtoFromClauseTerminal<DTO> selectImpl(final Table table, final Stream<DtoSelectSpec.FieldColumn> fieldColumns) {
        selectSpec.setTable(table);
        selectSpec.setDtoAlias(table.alias());
        selectSpec.setFieldColumns(fieldColumns.toList());
        return new DtoFromClauseTerminal<>(this);
    }

    OrmTable table() {
        return table;
    }

    TableRegistry tableRegistry() {
        return tableRegistry;
    }

    DtoAliasRegistry dtoAliasRegistry() {
        return dtoAliasRegistry;
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
