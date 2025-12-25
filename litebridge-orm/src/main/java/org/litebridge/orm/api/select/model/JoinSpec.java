package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Join;

import java.util.ArrayList;
import java.util.List;

public class JoinSpec {

    private final String table;
    private final List<ConditionSpec> conditions = new ArrayList<>();

    public JoinSpec(final String table) {
        this.table = table;
    }

    public String getTable() {
        return table;
    }

    public List<ConditionSpec> conditions() {
        return conditions;
    }

    public ConditionSpec newCondition(final String column) {
        final ConditionSpec conditionSpec = new ConditionSpec();
        conditionSpec.setColumn(column);
        conditions.add(conditionSpec);
        return conditionSpec;
    }

    public Join toJoin() {
        return new Join(table, conditions.stream()
                .map(conditionSpec -> new Condition(conditionSpec.getColumn(),
                        conditionSpec.getOperator(),
                        conditionSpec.getValue()))
                .toList());
    }
}
