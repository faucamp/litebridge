package org.litebridgedb.db.spi.expression;

import org.litebridgedb.db.spi.Operation;

public class ConvertExpression implements DelegateExpression {

    private final SelectExpression target;
    private final Class<?> typeOverride;

    /**
     * Constructor.
     *
     * @param target The encapsulated target column expression for this expression.
     */
    public ConvertExpression(final SelectExpression target, final Class<?> typeOverride) {
        this.target = target;
        this.typeOverride = typeOverride;
    }

    @Override
    public String toSql(final Operation operation) {
        return target.toSql(operation);
    }

    @Override
    public SelectExpression target() {
        return target;
    }

    public Class<?> typeOverride() {
        return typeOverride;
    }
}
