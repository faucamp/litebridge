package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;

public class ConditionSpec {

    private Column column;
    private Operator operator;
    @Nullable
    private Object value;

    public Column getColumn() {
        return column;
    }

    public void setColumn(final Column column) {
        this.column = column;
    }

    public Operator getOperator() {
        return operator;
    }

    public void setOperator(final Operator operator) {
        this.operator = operator;
    }

    public @Nullable Object getValue() {
        return value;
    }

    public void setValue(final @Nullable Object value) {
        this.value = value;
    }

    Condition toCondition() {
        return new Condition(column, operator, value);
    }
}
