package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.LogicOperator;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.model.ConditionGroupSpec;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.JoinSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Specification for a JOIN operation in a database query.
 * <p>
 * This class includes the table to be joined and the conditions that establish the relationship between the tables.
 */
public class SqlJoinSpec implements JoinSpec {

    private final Table table;
    private final List<ConditionGroupSpec> conditions = new ArrayList<>();
    private final SelectExpressionMapper selectExpressionMapper;

    public SqlJoinSpec(final Table table, final SelectExpressionMapper selectExpressionMapper) {
        this.table = table;
        this.selectExpressionMapper = selectExpressionMapper;
    }


    @Override
    public Table table() {
        return table;
    }

    @Override
    public List<ConditionGroupSpec> conditions() {
        return conditions;
    }

    public ConditionGroupSpec newConditionGroup(final LogicOperator logicOperator) {
        final ConditionGroupSpec conditionGroupSpec = new ConditionGroupSpec(logicOperator);
        conditions.add(conditionGroupSpec);
        return conditionGroupSpec;
    }

    public ConditionSpec using(final String column) {
        final Column spiColumn = new Column(table, column);
        final ConditionGroupSpec conditionGroupSpec = newConditionGroup(LogicOperator.AND);
        final ConditionSpec usingCondition = conditionGroupSpec.newCondition(new SelectColumnSpec(spiColumn));
        usingCondition.setOperator(Operator.USING);
        return usingCondition;
    }

    @Override
    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionGroupSpec -> conditionGroupSpec.toConditionGroup(selectExpressionMapper, Collections.singleton(table)))
                .toList());
    }
}
