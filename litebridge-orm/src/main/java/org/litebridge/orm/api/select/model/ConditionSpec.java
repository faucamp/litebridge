package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.Operator;

/**
 * Specification for a condition in a database query.
 * <p>
 * This is used in a SQL query WHERE clause, JOIN clause, etc.
 * <p>
 * A condition consists of a column, an operator, and an optional value. The operator
 * dictates how the column will be compared to the provided value.
 */
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

    public Condition toCondition() {
        return new Condition(column, operator, value);
    }
}
