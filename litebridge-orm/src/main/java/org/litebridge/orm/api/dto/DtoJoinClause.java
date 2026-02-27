package org.litebridge.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.api.select.impl.AbstractJoinClause;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.persistence.AliasGenerator;
import org.litebridge.orm.persistence.MappedManyToMany;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.tracking.ClassFieldAccessorCache;
import org.litebridge.tracking.FieldAccessor;

import java.util.List;

public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoSelectSpec,
        DtoJoinSpec> {

    private final OrmTable table;
    private final AliasGenerator aliasGenerator;
    private final DtoSelectSpec selectSpec;

    public DtoJoinClause(final Class<?> dtoClass, final OrmTable joinTable, final DtoSelector<DTO> delegate) {
        super(delegate.selectSpec().newJoinSpec(dtoClass, joinTable, delegate.dtoAliasRegistry().aliasTable(joinTable)), delegate);
        table = delegate.table();
        this.aliasGenerator = delegate.dtoAliasRegistry();
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

        return table.getOneToManyMappingForField(fieldAccessor)
                // Inverse join
                .map(mappedOneToMany -> joinOn(joinSpec.dtoTable(), mappedOneToMany.mappedByField().name()))
                .orElseGet(() -> table.getManyToManyMappingForField(fieldAccessor)
                        // Many-to-many join
                        .map(this::manyToManyJoin)
                        // Regular join
                        .orElseGet(() -> joinOn(table, fieldAccessor.name())));
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOn(final OrmTable ormTable, final String field) {
        return joinOn(joinSpec.dtoTable(), joinSpec.table(), ormTable.getColumnForFieldName(field), field);
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOn(final OrmTable rightOrmTable, final Table rightTable, final ColumnMetaData rightColumnMetaData, final @Nullable String field) {
        if (rightColumnMetaData.getJoinColumn() == null) {
            throw new IllegalStateException("No join column specified for column '%s' %s".formatted(rightColumnMetaData.name(), field != null ? "mapped to field '%s'".formatted(field) : "(no field)"));
        }

        final List<DtoSelectSpec.FieldColumn> joinFieldColumns = rightOrmTable.getMetaData().columns().stream()
                .map(joinColumn -> {
                    final FieldAccessor joinColumnField = rightOrmTable.getFieldForColumnName(joinColumn.name());
                    return new DtoSelectSpec.FieldColumn(joinColumnField, aliasGenerator.aliasColumn(rightTable, joinColumn));
                })
                .toList();

        // Extend selects
        selectSpec.addFieldColumns(joinFieldColumns);

        // Create JOIN clause
        joinSpec.setFieldColumns(joinFieldColumns);

        final Column leftColumn = selectSpec.getFieldColumns().stream()
                .map(DtoSelectSpec.FieldColumn::column)
                .filter(column -> column.table().equalsIgnoreAlias(rightColumnMetaData.table())
                        && column.equalsIgnoreAlias(rightColumnMetaData))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Left JOIN column not found"));

        final ConditionSpec conditionSpec = joinSpec.newCondition(leftColumn);
        final ColumnMetaData targetColumnMetaData = rightOrmTable.getColumn(rightColumnMetaData.getJoinColumn());

        if (rightColumnMetaData.name().equals(targetColumnMetaData.name())) {
            conditionSpec.setOperator(Operator.USING);
        } else {
            final Column targetColumn = joinFieldColumns.stream()
                    .map(DtoSelectSpec.FieldColumn::column)
                    .filter(c -> c.name().equals(targetColumnMetaData.name()))
                    .findFirst().orElseThrow(() -> new IllegalArgumentException("Target JOIN column not found"));
            conditionSpec.setOperator(Operator.EQ);
            conditionSpec.setValue(targetColumn);
        }

        return new DtoJoinConditionClauseTerminal<>(joinSpec, (DtoSelector<DTO>) delegate, aliasGenerator);
    }

    private DtoJoinConditionClauseTerminal<DTO> manyToManyJoin(MappedManyToMany mappedManyToMany) {
        // Join with the intermediate join table
        final DtoJoinSpec intermediateJoinSpec = createIntermediateJoinSpec(mappedManyToMany);

        // Join with the target table
        final Table leftTable = intermediateJoinSpec.table();
        final Column leftColumn = aliasGenerator.aliasColumn(leftTable, mappedManyToMany.joinTable().getColumn(mappedManyToMany.inverseJoinColumn()));

        final OrmTable rightOrmTable = mappedManyToMany.targetTable().optional().orElseThrow();
        final Table rightTable = joinSpec.table();

        final List<DtoSelectSpec.FieldColumn> joinFieldColumns = rightOrmTable.mappedFieldTargets().stream()
                .filter(entry -> entry.getValue() instanceof ColumnMetaData)
                .map(entry -> {
                    final FieldAccessor field = entry.getKey();
                    final ColumnMetaData column = (ColumnMetaData) entry.getValue();
                    return new DtoSelectSpec.FieldColumn(field, aliasGenerator.aliasColumn(rightTable, column));
                })
                .toList();

        // Extend selects
        selectSpec.addFieldColumns(joinFieldColumns);

        // Create JOIN clause
        joinSpec.setFieldColumns(joinFieldColumns);

        final Column rightColumn = aliasGenerator.aliasColumn(rightTable, rightOrmTable.getColumn(mappedManyToMany.inverseJoinColumn()));

        final ConditionSpec conditionSpec = joinSpec.newCondition(leftColumn);
        conditionSpec.setOperator(Operator.EQ);
        conditionSpec.setValue(rightColumn);

        return new DtoJoinConditionClauseTerminal<>(intermediateJoinSpec, (DtoSelector<DTO>) delegate, aliasGenerator);
    }

    private DtoJoinSpec createIntermediateJoinSpec(final MappedManyToMany mappedManyToMany) {
        // Left table: source DTO table (main SELECT table)
        final OrmTable leftOrmTable = selectSpec.dtoTable();
        final Table leftTable = selectSpec.getTable();
        //TODO: support for composite PKs
        final Column leftColumn = aliasGenerator.aliasColumn(leftTable, leftOrmTable.getMetaData().primaryKey().getFirst());

        // Right table: intermediate join table
        final OrmTable rightOrmTable = mappedManyToMany.joinTable();
        final Table rightTable = aliasGenerator.aliasTable(rightOrmTable);
        final Column rightColumn = aliasGenerator.aliasColumn(rightTable, rightOrmTable.getColumn(mappedManyToMany.joinColumn()));

        final DtoJoinSpec intermediateJoinSpec = selectSpec.newJoinSpecBefore(joinSpec, selectSpec.dtoClass(), rightOrmTable, rightTable);
        final ConditionSpec intermediateJoinCondition = intermediateJoinSpec.newCondition(leftColumn);
        intermediateJoinCondition.setOperator(Operator.EQ);
        intermediateJoinCondition.setValue(rightColumn);

        return intermediateJoinSpec;
    }
}
