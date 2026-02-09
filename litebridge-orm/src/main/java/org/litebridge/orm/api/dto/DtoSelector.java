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

        // Calculate and add joins to embedded DTOs (or flatten them into the current table if no join/subselect is needed)
//        ClassFieldCache.nestedDtoFields(dtoClass).forEach(nestedDtoField -> {
//            if (table.hasColumnForFieldName(nestedDtoField.getName())) {
//                addJoinForNestedDto(nestedDtoField);
//            }
//        });

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

//    private void addJoinForNestedDto(final FieldAccessor nestedDtoField) {
//        final ColumnMetaData joinColumnMetaData = table.getColumnForFieldName(nestedDtoField.name());
//
//        if (joinColumnMetaData.getJoinColumn() == null) {
//            throw new IllegalStateException("No join column specified for nested DTO '%s' in field '%s' of DTO '%s'"
//                    .formatted(nestedDtoField.type().getName(), nestedDtoField.name(), dtoClass.getName()));
//        }
//
//        final OrmTable joinTable = tableRegistry.getTableOrThrow(nestedDtoField.type());
//        final ColumnMetaData targetColumnMetaData = joinTable.getColumn(joinColumnMetaData.getJoinColumn());
//        final String joinAlias = dtoAliasRegistry.newAlias(joinTable.getMetaData());
//
//        final List<DtoSelectSpec.FieldColumn> joinFieldColumns = joinTable.getMetaData().columns().stream()
//                .map(columnMetaData -> {
//                    final FieldAccessor fieldAccessor = joinTable.getFieldForColumnName(columnMetaData.name());
//                    return new DtoSelectSpec.FieldColumn(fieldAccessor, new Column(joinTable.getMetaData(), columnMetaData.name(), dtoAliasRegistry.alias(joinAlias, columnMetaData)));
//                })
//                .toList();
//
//        // Extend selects
//        selectSpec.addFieldColumns(joinFieldColumns);
//
//        // Create JOIN clause
//        final JoinSpec joinSpec = selectSpec.newJoinSpec(table.getMetaData().schema(), joinTable.getMetaData().name());
//        joinSpec.table().as(joinAlias);
//
//        final ColumnMetaData joinColumn = new ColumnMetaData(joinColumnMetaData);
//        joinColumn.table().as(selectSpec.getTable().alias());
//        final ConditionSpec conditionSpec = joinSpec.newCondition(joinColumn);
//
//        if (joinColumnMetaData.name().equals(targetColumnMetaData.name())) {
//            conditionSpec.setOperator(Operator.USING);
//        } else {
//            final Column targetColumn = joinFieldColumns.stream()
//                    .map(DtoSelectSpec.FieldColumn::column)
//                    .filter(column -> column.name().equals(targetColumnMetaData.name()))
//                    .findFirst().orElseThrow();
//            conditionSpec.setOperator(Operator.EQ);
//            conditionSpec.setValue(targetColumn);
//        }
//    }


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
