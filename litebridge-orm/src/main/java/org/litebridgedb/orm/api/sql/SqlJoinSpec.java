package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.JoinSpec;
import org.litebridgedb.orm.api.select.model.SelectExpressionMapper;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Specification for a JOIN operation in a database query.
 * <p>
 * This class includes the table to be joined and the conditions that establish the relationship between the tables.
 */
public class SqlJoinSpec implements JoinSpec {

    private final Table table;
    private final List<ConditionSpec> conditions = new ArrayList<>();
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
    public List<ConditionSpec> conditions() {
        return conditions;
    }

    public ConditionSpec newCondition(final Column column) {
        return newCondition(new SelectColumnSpec(column));
    }

    public ConditionSpec newCondition(final ExpressionSpec expression) {
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setLhs(expression);
        conditions.add(conditionSpec);
        return conditionSpec;
    }

    public ConditionSpec using(final String column) {
        final Column spiColumn = new Column(table, column);
        final ConditionSpec usingCondition = newCondition(spiColumn);
        usingCondition.setOperator(Operator.USING);
        return usingCondition;
    }

    @Override
    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionSpec -> conditionSpec.toCondition(selectExpressionMapper, Collections.singletonList(table)))
                .toList());
    }
}
