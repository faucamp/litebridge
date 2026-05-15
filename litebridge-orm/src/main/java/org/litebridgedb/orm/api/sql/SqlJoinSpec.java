package org.litebridgedb.orm.api.sql;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.db.spi.query.Condition;
import org.litebridgedb.db.spi.query.Join;
import org.litebridgedb.db.spi.query.Operator;
import org.litebridgedb.orm.api.select.model.ConditionSpec;
import org.litebridgedb.orm.api.select.model.JoinSpec;

import java.util.ArrayList;
import java.util.List;

/**
 * Specification for a JOIN operation in a database query.
 * <p>
 * This class includes the table to be joined and the conditions that establish the relationship between the tables.
 */
public class SqlJoinSpec implements JoinSpec {

    private final Table table;
    private final List<ConditionSpec> conditions = new ArrayList<>();

    public SqlJoinSpec(final Table table) {
        this.table = table;
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
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
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
                .map(conditionSpec -> new Condition(conditionSpec.getColumn(),
                        conditionSpec.getOperator(),
                        conditionSpec.getValue()))
                .toList());
    }
}
