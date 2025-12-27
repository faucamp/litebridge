package org.litebridge.orm.api.dto;

import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.impl.AbstractSelector;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.api.select.model.SelectSpec;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.DtoMapper;
import org.litebridge.orm.persistence.Table;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ClassFieldCache;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.stream.Stream;

public final class DtoSelector<DTO> extends AbstractSelector<DTO> {

    private final Table table;
    private final TableRegistry tableRegistry;
    private final DtoAliasRegistry dtoAliasRegistry;

    public DtoSelector(final Class<DTO> dtoClass,
                       final Table table,
                       final TableRegistry tableRegistry,
                       final DatabaseProvider databaseProvider,
                       final DtoMapper dtoMapper,
                       final DtoAliasRegistry dtoAliasRegistry) {
        super(new SelectSpec(), databaseProvider, dtoMapper, dtoClass);
        this.table = table;
        this.tableRegistry = tableRegistry;
        this.dtoAliasRegistry = dtoAliasRegistry;
    }

    public DtoFromClauseTerminal<DTO> select(final String... fields) {
        return selectImpl(Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field);
                    return new Column(table.getMetaData(), column.name());
                }));
    }

    public DtoFromClauseTerminal<DTO> select(final Aliased... fields) {
        return selectImpl(Arrays.stream(fields)
                .map(field -> {
                    // Map the input DTO field names to database column names
                    final ColumnMetaData column = table.getColumnForFieldName(field.name());
                    return new Column(table.getMetaData(), column.name(), field.alias());
                }));
    }

    public DtoFromClauseTerminal<DTO> select() {
        return selectImpl(table.getMetaData().columns().stream().map(column -> (Column) column));
    }

    private DtoFromClauseTerminal<DTO> selectImpl(final Stream<Column> columns) {
        final String tableAlias = dtoAliasRegistry.alias(table.getMetaData());

        selectSpec.setTable(table.getMetaData().as(tableAlias));
        selectSpec.setColumns(columns.map(column -> column.as(dtoAliasRegistry.alias(tableAlias, column))));

        // Calculate and add joins to embedded DTOs
        ClassFieldCache.getNestedDtoFields(dtoClass).forEach(this::addJoinForNestedDto);

        return new DtoFromClauseTerminal<>(this);
    }

    Table table() {
        return table;
    }

    private void addJoinForNestedDto(final Field nestedDtoField) {
        final ColumnMetaData joinColumn = table.getColumnForFieldName(nestedDtoField.getName());

        if (joinColumn.getJoinColumn() == null) {
            throw new IllegalStateException("No join column specified for nested DTO '%s' in field '%s' of DTO '%s'".formatted(nestedDtoField.getType().getName(), nestedDtoField.getName(), dtoClass.getName()));
        }

        final Table joinTable = tableRegistry.getTableOrThrow(nestedDtoField.getType());
        final ColumnMetaData targetColumn = joinTable.getColumn(joinColumn.getJoinColumn());
        final String joinAlias = dtoAliasRegistry.alias(joinTable.getMetaData());

        // Extend selects
        selectSpec.addColumns(joinTable.getMetaData().columns().stream()
                .map(columnMetaData ->
                        new Column(joinTable.getMetaData().as(joinAlias), columnMetaData.name(), dtoAliasRegistry.alias(joinAlias, columnMetaData)))
                .toList());

        // Create JOIN clause
        final JoinSpec joinSpec = selectSpec.newJoinSpec(table.getMetaData().schema(), joinTable.getMetaData().name());
        joinSpec.table().as(joinAlias);
        final ConditionSpec conditionSpec = joinSpec.newCondition(joinColumn);

        if (joinColumn.name().equals(targetColumn.name())) {
            conditionSpec.setOperator(Operator.USING);
        } else {
            conditionSpec.setOperator(Operator.EQ);
            conditionSpec.setValue(targetColumn);
        }
    }
}
