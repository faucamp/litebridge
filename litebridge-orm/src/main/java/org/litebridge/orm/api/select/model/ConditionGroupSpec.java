package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.query.LogicOperator;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.persistence.TableMetaDataCache;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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
     * @param logicOperator  the logic operator for the condition
     * @param expressionSpec the expression specification for the condition
     * @return the newly created {@link ConditionSpec}
     */
    public ConditionSpec newCondition(final LogicOperator logicOperator, final ExpressionSpec expressionSpec) {
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(expressionSpec);
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

    /**
     * Converts this specification into a {@link ConditionGroup}.
     *
     * @param selectExpressionMapper the mapper to use for expressions
     * @param selectedTables         the set of tables included in the query
     * @param tableMetaDataCache
     * @return the resulting {@link ConditionGroup}
     */
    public ConditionGroup toConditionGroup(final SelectExpressionMapper selectExpressionMapper,
                                           final Set<Table> selectedTables,
                                           final List<BindValue> bindValues,
                                           final TableMetaDataCache tableMetaDataCache,
                                           final TypeConverter typeConverter) {
        final List<LogicConditionGroup> subConditionGroups = subgroups.stream()
                .map(subgroup -> {
                    final ConditionGroup conditionGroup = subgroup.conditionGroupSpec().toConditionGroup(selectExpressionMapper, selectedTables, bindValues, tableMetaDataCache, typeConverter);
                    return new LogicConditionGroup(subgroup.logicOperator(), conditionGroup);
                })
                .toList();

        return new ConditionGroup(conditions.stream()
                .map(spec -> new LogicCondition(spec.logicOperator(),
                        spec.conditionSpec().toCondition(selectExpressionMapper, selectedTables, bindValues, tableMetaDataCache, typeConverter)))
                .toList(),
                subConditionGroups);
    }
}
