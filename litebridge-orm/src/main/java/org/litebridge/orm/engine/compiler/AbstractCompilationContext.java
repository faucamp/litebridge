package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.LiteralExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.expression.SubselectExpression;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.db.spi.query.Select;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.model.ConditionGroupSpec;
import org.litebridge.orm.api.select.model.ConditionSpec;
import org.litebridge.orm.api.select.model.SelectExpressionMapper;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;
import org.litebridge.orm.expression.select.SelectFieldSpec;
import org.litebridge.orm.persistence.OrmTable;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.tracking.FieldAccessor;

import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

abstract sealed class AbstractCompilationContext implements CompilationContext permits DeleteCompilationContext, SelectCompilationContext, UpdateCompilationContext {

    protected final LitebridgeContext litebridgeContext;
    protected final List<BindValue> bindValues = new ArrayList<>();

    protected AbstractCompilationContext(final LitebridgeContext litebridgeContext) {
        this.litebridgeContext = litebridgeContext;
    }

    protected final ConditionGroup toConditionGroup(final ConditionGroupSpec conditionGroupSpec, final @Nullable OrmTable ormTable, final Table table) {
        final List<LogicCondition> resolvedConditions = conditionGroupSpec.conditions().stream()
                .map(spec -> new LogicCondition(spec.logicOperator(),
                        toCondition(spec.conditionSpec(), ormTable, table)))
                .toList();

        final List<LogicConditionGroup> subConditionGroups = conditionGroupSpec.subgroups().stream()
                .map(subgroup -> {
                    final ConditionGroup conditionGroup = toConditionGroup(subgroup.conditionGroupSpec(), ormTable, table);
                    return new LogicConditionGroup(subgroup.logicOperator(), conditionGroup);
                })
                .toList();

        return new ConditionGroup(resolvedConditions, subConditionGroups);
    }

    protected Condition toCondition(final ConditionSpec conditionSpec, final @Nullable OrmTable ormTable, final Table table) {
        final SelectExpressionMapper selectExpressionMapper = litebridgeContext.selectExpressionMapper();
        final ExpressionSpec lhsExpressionSpec;

        if (conditionSpec.getLhsExpression() != null) {
            // Expression specification
            final List<ExpressionSpec> lhsResolvedExpressionSpecs = selectExpressionMapper
                    .resolveProtoExpression(conditionSpec.getLhsExpression(), ormTable, table, ClauseType.WHERE).stream()
                    .toList();

            if (lhsResolvedExpressionSpecs.size() != 1) {
                throw new IllegalArgumentException("Expected exactly one LHS expression spec, but got " + lhsResolvedExpressionSpecs.size());
            }

            lhsExpressionSpec = lhsResolvedExpressionSpecs.getFirst();
        } else if (ormTable != null) {
            // DTO field name
            final ColumnMetaData columnMetaData = ormTable.columnMetaDataForField(Objects.requireNonNull(conditionSpec.getLhsColumn()));
            final FieldAccessor fieldAccessor = ormTable.getFieldForColumnName(columnMetaData.name());
            lhsExpressionSpec = new SelectFieldSpec(fieldAccessor, columnMetaData.toColumn());
        } else {
            // Column name
            lhsExpressionSpec = new SelectColumnSpec(new Column(table, Objects.requireNonNull(conditionSpec.getLhsColumn())));
        }

        final SelectExpression lhsSelectExpression = selectExpressionMapper.toSelectExpression(lhsExpressionSpec, true);
        final Operator operator = conditionSpec.getOperator();
        final Object value = conditionSpec.getValue();

        if (value instanceof QueryNode subselectNode) {
            // Subselect
            final QueryCompiler queryCompiler = litebridgeContext.createQueryCompiler();
            final PreparedOperation preparedOperation = queryCompiler.compile(subselectNode);
            bindValues.addAll(preparedOperation.bindValues());
            final Select subselect = (Select) preparedOperation.operation();
            final SubselectExpression subselectExpression = litebridgeContext.sqlFunctionRegistry().select().subselect().create(subselect);
            return new Condition(lhsSelectExpression, operator, subselectExpression);
        } else if (value instanceof ExpressionSpec expressionSpec) {
            final List<ExpressionSpec> rhsResolvedExpressionSpecs = selectExpressionMapper.resolveProtoExpression(expressionSpec, ormTable, table, ClauseType.WHERE);

            if (rhsResolvedExpressionSpecs.size() != 1) {
                throw new IllegalArgumentException("Expected exactly one RHS expression spec, but got " + rhsResolvedExpressionSpecs.size());
            }

            return new Condition(lhsSelectExpression, operator, selectExpressionMapper.toSelectExpression(rhsResolvedExpressionSpecs.getFirst(), true));
        } else if (value instanceof Column referencedColumn) {
            // Reference to a selected column
            final SelectReference selectReference = litebridgeContext.sqlFunctionRegistry().select().reference().create(referencedColumn);
            return new Condition(lhsSelectExpression, operator, selectReference);
        }

        // Setup bind value creators
        switch (operator) {
            case USING -> {
                final LiteralExpression literalExpression = litebridgeContext.sqlFunctionRegistry().select().literal().create(value, true);
                return new Condition(lhsSelectExpression, operator, literalExpression);
            }
            default -> {
                final BindValueExpression bindValueExpression = createBindValueExpression(value, bindValues.size());
                bindValues.addAll(createBindValues(lhsSelectExpression, value, litebridgeContext.tableMetaDataCache(), litebridgeContext.typeConverter()));
                return new Condition(lhsSelectExpression, operator, bindValueExpression);
            }
        }
    }

    /**
     * Creates a bind value for a column and raw value.
     *
     * @param lhsSelectExpression LHS select expression for the condition.
     * @param rawValue            The raw value.
     * @param tableMetaDataCache  Table metadata cache.
     * @return The bind value.
     */
    protected List<BindValue> createBindValues(final SelectExpression lhsSelectExpression, final @Nullable Object rawValue, final TableMetaDataCache tableMetaDataCache, final TypeConverter typeConverter) {
        final Column column;

        if (lhsSelectExpression instanceof ColumnExpression columnExpression) {
            column = columnExpression.column();
        } else {
            column = null;
        }

        if (column != null) {
            final ColumnMetaData columnMetaData = tableMetaDataCache.ensureTableMetaData(column.table()).column(column.name());

            if (rawValue instanceof Collection<?> collection) {
                // Multiple bind values
                return collection.stream()
                        .map(value -> typeConverter.convert(value, columnMetaData.getDataType()))
                        .map(convertedValue -> new BindValue(convertedValue, columnMetaData.getDataType()))
                        .toList();
            } else {
                // Single bind value
                final Object convertedValue = typeConverter.convert(rawValue, columnMetaData.getDataType());
                return Collections.singletonList(new BindValue(convertedValue, columnMetaData.getDataType()));
            }
        } else if (rawValue != null) {
            return Collections.singletonList(new BindValue(rawValue, typeConverter.getSqlDataType(rawValue.getClass())));
        } else {
            return Collections.singletonList(new BindValue(null, Types.NULL));
        }
    }

    private static BindValueExpression createBindValueExpression(final @Nullable Object value, final int index) {
        final int valueSize;

        if (value instanceof Collection<?> collection) {
            valueSize = collection.size();
        } else {
            valueSize = 1;
        }

        return new BindValueExpression(index, valueSize);
    }
}
