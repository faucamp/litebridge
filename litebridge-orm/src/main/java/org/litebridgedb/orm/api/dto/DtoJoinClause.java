package org.litebridgedb.orm.api.dto;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.impl.AbstractJoinClause;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.ProtoExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;
import org.litebridgedb.orm.expression.select.SelectFieldSpec;
import org.litebridgedb.orm.meta.QFInspector;
import org.litebridgedb.orm.meta.QueryField;
import org.litebridgedb.orm.persistence.MappedManyToMany;
import org.litebridgedb.orm.persistence.OrmTable;
import org.litebridgedb.orm.persistence.alias.AliasGenerator;
import org.litebridgedb.tracking.ClassFieldAccessorCache;
import org.litebridgedb.tracking.FieldAccessor;

import java.util.List;

public final class DtoJoinClause<DTO> extends AbstractJoinClause<DTO,
        DtoJoinConditionClause<DTO>,
        DtoJoinConditionClauseTerminal<DTO>,
        DtoSelectSpec,
        DtoJoinSpec> {

    private final OrmTable table;
    private final AliasGenerator aliasGenerator;
    private final DtoSelectSpec selectSpec;
    private final ClassFieldAccessorCache classFieldAccessorCache;

    public DtoJoinClause(final Class<?> dtoClass, final OrmTable joinTable, final DtoSelector<DTO> delegate) {
        super(delegate.selectSpec().newJoinSpec(dtoClass, joinTable, delegate.dtoAliasRegistry().aliasTable(joinTable)), delegate);
        table = delegate.table();
        this.aliasGenerator = delegate.dtoAliasRegistry();
        this.selectSpec = delegate.selectSpec();
        this.classFieldAccessorCache = delegate.classFieldAccessorCache();
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
        final FieldAccessor fieldAccessor = classFieldAccessorCache.fieldAccessorOrThrow(table.dtoClass(), field);

        return table.getOneToManyMappingForField(fieldAccessor)
                // Inverse join
                .map(mappedOneToMany -> joinOn(joinSpec.dtoTable(), mappedOneToMany.mappedByField().name()))
                .orElseGet(() -> table.getManyToManyMappingForField(fieldAccessor)
                        // Many-to-many join
                        .map(this::manyToManyJoin)
                        // Regular join
                        .orElseGet(() -> joinOn(table, fieldAccessor.name())));
    }

    public DtoJoinConditionClauseTerminal<DTO> on(final ExpressionSpec expression) {
        return switch (expression) {
            case QueryField queryField -> on(QFInspector.getFieldName(queryField));
            case ProtoExpressionSpec protoExpressionSpec -> on(protoExpressionSpec.column());
            case SelectFieldSpec selectFieldSpec -> on(selectFieldSpec.field().name());
            default -> throw new IllegalArgumentException("Unsupported JOIN ON expression: " + expression);
        };
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOn(final OrmTable ormTable, final String field) {
        return joinOn(joinSpec.dtoTable(), joinSpec.table(), ormTable.getColumnForFieldName(field), field);
    }

    private DtoJoinConditionClauseTerminal<DTO> joinOn(final OrmTable rightOrmTable, final Table rightTable, final ColumnMetaData rightColumnMetaData, final @Nullable String field) {
        if (rightColumnMetaData.getJoinColumn() == null) {
            throw new IllegalStateException("No join column specified for column '%s' %s".formatted(rightColumnMetaData.name(), field != null ? "mapped to field '%s'".formatted(field) : "(no field)"));
        }

        final List<SelectFieldSpec> joinFieldColumns = rightOrmTable.getMetaData().columns().stream()
                .map(joinColumn -> {
                    final FieldAccessor joinColumnField = rightOrmTable.getFieldForColumnName(joinColumn.name());
                    final Column column = aliasGenerator.aliasColumn(rightTable, joinColumn);
                    return new SelectFieldSpec(joinColumnField, column);
                })
                .toList();

        // Extend selects
        selectSpec.addExpressions(joinFieldColumns);

        // Create JOIN clause
        joinSpec.setFieldColumns(joinFieldColumns.stream().map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn())).toList());
        final Column rightColumn = rightColumnMetaData.toColumn();

        final Column leftColumn = selectSpec.getExpressions().stream()
                .filter(expression -> expression instanceof SelectFieldSpec)
                .map(expression -> ((SelectFieldSpec) expression).getColumn())
                .filter(column -> column.table().equalsIgnoreAlias(rightColumnMetaData.table())
                        && column.equalsIgnoreAlias(rightColumn))
                .findFirst().orElseThrow(() -> new IllegalArgumentException("Left JOIN column not found"));

        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(leftColumn));
        final ColumnMetaData targetColumnMetaData = rightOrmTable.getColumnMetaData(rightColumnMetaData.getJoinColumn());

        if (rightColumnMetaData.name().equals(targetColumnMetaData.name())) {
            conditionSpec.setOperator(Operator.USING);
        } else {
            final Column targetColumn = joinFieldColumns.stream()
                    .map(SelectFieldSpec::getColumn)
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
        final Column leftColumn = aliasGenerator.aliasColumn(leftTable, mappedManyToMany.joinTable().getColumnMetaData(mappedManyToMany.inverseJoinColumn()));

        final OrmTable rightOrmTable = mappedManyToMany.targetTable().optional().orElseThrow();
        final Table rightTable = joinSpec.table();

        final List<SelectFieldSpec> joinFieldColumns = rightOrmTable.mappedFieldTargets().stream()
                .filter(entry -> entry.getValue() instanceof ColumnMetaData)
                .map(entry -> {
                    final FieldAccessor field = entry.getKey();
                    final ColumnMetaData columnMetaData = (ColumnMetaData) entry.getValue();
                    final Column column = aliasGenerator.aliasColumn(rightTable, columnMetaData);
                    return new SelectFieldSpec(field, column);
                })
                .toList();

        // Extend selects
        selectSpec.addExpressions(joinFieldColumns);

        // Create JOIN clause
        joinSpec.setFieldColumns(joinFieldColumns.stream().map(selectField -> new DtoSelectSpec.FieldColumn(selectField.field(), selectField.getColumn())).toList());

        final Column rightColumn = aliasGenerator.aliasColumn(rightTable, rightOrmTable.getColumnMetaData(mappedManyToMany.inverseJoinColumn()));

        final ConditionSpec conditionSpec = joinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(leftColumn));
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
        final Column rightColumn = aliasGenerator.aliasColumn(rightTable, rightOrmTable.getColumnMetaData(mappedManyToMany.joinColumn()));

        final DtoJoinSpec intermediateJoinSpec = selectSpec.newJoinSpecBefore(joinSpec, selectSpec.dtoClass(), rightOrmTable, rightTable);
        final ConditionSpec intermediateJoinCondition = intermediateJoinSpec.currentConditionGroupSpec().newCondition(LogicOperator.NOOP, new SelectColumnSpec(leftColumn));
        intermediateJoinCondition.setOperator(Operator.EQ);
        intermediateJoinCondition.setValue(rightColumn);

        return intermediateJoinSpec;
    }
}
