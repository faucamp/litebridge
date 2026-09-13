package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.query.Operator;
import org.litebridge.orm.expression.ExpressionSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Groups conditions and condition subgroups using a logical operator.
 *
 * @param conditions A list of conditions to be combined.
 * @param subgroups  A list of subgroups of conditions to be combined.
 */
public record ConditionGroupSpec(List<LogicConditionSpec> conditions,
                                 List<LogicConditionGroupSpec> subgroups) {

    /**
     * Constructs a {@code ConditionGroupSpec} with the specified conditions and subgroups.
     *
     * @param conditions the list of logic condition specifications
     * @param subgroups  the list of logic condition group specifications
     */
    public ConditionGroupSpec(final List<LogicConditionSpec> conditions, final List<LogicConditionGroupSpec> subgroups) {
        if (conditions instanceof ArrayList<LogicConditionSpec> arrayList) {
            this.conditions = arrayList;
        } else {
            this.conditions = new ArrayList<>(conditions);
        }

        if (subgroups instanceof ArrayList<LogicConditionGroupSpec> arrayList) {
            this.subgroups = arrayList;
        } else {
            this.subgroups = subgroups;
        }
    }

    /**
     * Constructs a {@code ConditionGroupSpec} with the specified conditions.
     *
     * @param conditions the list of logic condition specifications
     */
    public ConditionGroupSpec(final List<LogicConditionSpec> conditions) {
        this(conditions, new ArrayList<>());
    }

    /**
     * Constructs an empty {@code ConditionGroupSpec}.
     */
    public ConditionGroupSpec() {
        this(new ArrayList<>(), new ArrayList<>());
    }

    /**
     * Adds a new condition to the group and returns its specification.
     *
     * @param logicOperator the logic operator for the condition
     * @param lhsColumn     the left-hand side column name
     * @param lhsExpression the left-hand side expression
     * @return the newly created {@link ConditionSpec}
     */
    public ConditionSpec newCondition(final LogicOperator logicOperator,
                                      final @Nullable String lhsColumn,
                                      final @Nullable ExpressionSpec lhsExpression) {
        return newCondition(logicOperator, lhsColumn, lhsExpression, null, null);
    }

    /**
     * Adds a new condition to the group and returns its specification.
     *
     * @param logicOperator  the logic operator for the condition
     * @param fieldOrColumn  the field or column name for the condition
     * @param expressionSpec the expression specification for the condition
     * @param operator       the operator for the condition
     * @param rawValue       the raw value for the condition
     * @return the newly created {@link ConditionSpec}
     */
    public ConditionSpec newCondition(final LogicOperator logicOperator,
                                      final @Nullable String fieldOrColumn,
                                      final @Nullable ExpressionSpec expressionSpec,
                                      final @Nullable Operator operator,
                                      final @Nullable Object rawValue) {
        final ConditionSpec conditionSpec = new ConditionSpec(fieldOrColumn, expressionSpec, operator, rawValue);
        final LogicConditionSpec logicConditionSpec = new LogicConditionSpec(logicOperator, conditionSpec);
        conditions.add(logicConditionSpec);
        return conditionSpec;
    }

    /**
     * Adds a new subgroup to the group and returns its specification.
     *
     * @param logicOperator the logic operator for the subgroup
     * @return the newly created {@link LogicConditionGroupSpec}
     */
    public LogicConditionGroupSpec newSubgroup(final LogicOperator logicOperator) {
        final LogicConditionGroupSpec logicConditionGroupSpec = new LogicConditionGroupSpec(logicOperator);
        subgroups.add(logicConditionGroupSpec);
        return logicConditionGroupSpec;
    }
}
