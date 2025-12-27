package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;
import org.litebridge.db.spi.query.Operator;

import java.util.ArrayList;
import java.util.List;

public class JoinSpec {

    private final Table table;
    private final List<ConditionSpec> conditions = new ArrayList<>();

    public JoinSpec(final String schema, final String table) {
        this(new Table("", schema, table));
    }

    private JoinSpec(final Table table) {
        this.table = table;
    }

    public Table table() {
        return table;
    }

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

    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionSpec -> new Condition(conditionSpec.getColumn(),
                        conditionSpec.getOperator(),
                        conditionSpec.getValue()))
                .toList());
    }
}
