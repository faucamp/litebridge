package org.litebridge.orm.api.select.model;

import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;

public class ConditionSpec {

    private String column;
    private Operator operator;
    private Object value;

    public String getColumn() {
        return column;
    }

    public void setColumn(final String column) {
        this.column = column;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(final Object value) {
        this.value = value;
    }

    Condition toCondition() {
        return new Condition(column, operator, value);
    }
}
