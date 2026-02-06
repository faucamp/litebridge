package org.litebridge.orm.api.dto;

import org.jspecify.annotations.NonNull;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.JoinSpec;
import org.litebridge.orm.persistence.DtoAliasRegistry;
import org.litebridge.orm.persistence.MappedOneToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableRegistry;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.util.List;

public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoSelectSpec> {

    private final TableRegistry tableRegistry;
    private final OrmTable table;
    private final DtoAliasRegistry dtoAliasRegistry;
    /**
     * The target DTO class to be joined.
     */
    private final Class<?> joinDtoClass;
    private final DtoSelectSpec selectSpec;

    public DtoJoinClause(final Class<?> joinDtoClass, final JoinSpec joinSpec, final DtoSelector<DTO> delegate) {
        super(joinSpec, delegate);
        table = delegate.table();
        tableRegistry = delegate.tableRegistry();
        this.joinDtoClass = joinDtoClass;
        this.dtoAliasRegistry = delegate.dtoAliasRegistry();
        this.selectSpec = delegate.selectSpec();
    }

    /**
     * Adds a join ON condition to the current join clause based on the specified field.
     * The join condition constrains the relationship between the tables being joined.
     *
     * @param field the name of the field to be used in the join condition
     * @return an instance of the join condition clause to allow further configuration
     */
    public DtoJoinConditionClauseTerminal<DTO> on(final String field) {
        // Check if this is an inverse join
        final FieldAccessor fieldAccessor = ClassFieldAccessorCache.fieldAccessorOrThrow(table.dtoClass(), field);

        if (table.hasOneToManyMapping(fieldAccessor)) {
            // Inverse join
            final MappedOneToMany mappedOneToMany = table.getOneToManyMappingForField(fieldAccessor);
            final OrmTable joinTable = tableRegistry.getTableOrThrow(mappedOneToMany.mappedByField().dtoClass());
            return joinOn(joinTable, mappedOneToMany.mappedByField());
        } else {
            // Regular join
            return joinOn(table, fieldAccessor);
        }
    }

    private @NonNull DtoJoinConditionClauseTerminal<DTO> joinOn(final OrmTable table, final FieldAccessor field) {
        final ColumnMetaData column = table.getColumnForFieldName(field.name());

        if (column.getJoinColumn() == null) {
            throw new IllegalStateException("No join column specified for column '%s' mapped to field '%s'".formatted(column.name(), field));
        }

        final OrmTable joinTable = tableRegistry.getTableOrThrow(joinDtoClass);
        final ColumnMetaData targetColumnMetaData = joinTable.getColumn(column.getJoinColumn());
        final String joinAlias = dtoAliasRegistry.newAlias(joinTable.getMetaData());
        final List<DtoSelectSpec.FieldColumn> joinFieldColumns = joinTable.getMetaData().columns().stream()
                .map(joinColumn -> {
                    final FieldAccessor joinColumnField = joinTable.getFieldForColumnName(joinColumn.name());
                    return new DtoSelectSpec.FieldColumn(joinColumnField,
                            new Column(joinTable.getMetaData().as(joinAlias), joinColumn.name(), dtoAliasRegistry.alias(joinAlias, joinColumn)));
                })
                .toList();

        // Extend selects
        selectSpec.addFieldColumns(joinFieldColumns);

        // Create JOIN clause
        joinSpec.table().as(joinAlias);
        ((DtoJoinSpec) joinSpec).setFieldColumns(joinFieldColumns);

        final ColumnMetaData joinColumn = new ColumnMetaData(column);
        joinColumn.table().as(selectSpec.getTable().alias());
        final ConditionSpec conditionSpec = joinSpec.newCondition(joinColumn);

        if (column.name().equals(targetColumnMetaData.name())) {
            conditionSpec.setOperator(Operator.USING);
        } else {
            final Column targetColumn = joinFieldColumns.stream()
                    .map(DtoSelectSpec.FieldColumn::column)
                    .filter(c -> c.name().equals(targetColumnMetaData.name()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Target JOIN column not found"));
            conditionSpec.setOperator(Operator.EQ);
            conditionSpec.setValue(targetColumn);
        }

        return new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate);
    }
}
