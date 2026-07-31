package org.litebridge.orm.api.update;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.math.MathOperation;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.orm.api.update.impl.AbstractUpdater;

public class UpdateSetStep<US extends UpdateStep> {

    private final US delegate;
    private final Column column;

    public UpdateSetStep(final Column column, final US delegate) {
        this.column = column;
        this.delegate = delegate;
    }

    public US to(final Object value) {
        addSetNode(column, value);
        return delegate;
    }

    public US increment() {
        return add(1);
    }

    public US add(final Object value) {
        addSetNode(column, new MathOperation(MathOperation.Operator.ADD, value));
        return delegate;
    }

    public US minus(final Object value) {
        addSetNode(column, new MathOperation(MathOperation.Operator.SUBTRACT, value));
        return delegate;
    }

    public US multiply(final Object value) {
        addSetNode(column, new MathOperation(MathOperation.Operator.MULTIPLY, value));
        return delegate;
    }

    public US divide(final Object value) {
        addSetNode(column, new MathOperation(MathOperation.Operator.DIVIDE, value));
        return delegate;
    }

    public US mod(final Object value) {
        addSetNode(column, new MathOperation(MathOperation.Operator.MOD, value));
        return delegate;
    }

    private void addSetNode(final Column column, final Object value) {
        if (delegate instanceof AbstractUpdater<?> updater) {
            updater.addSetNode(column, value);
        } else {
            ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, value));
        }
    }
}
