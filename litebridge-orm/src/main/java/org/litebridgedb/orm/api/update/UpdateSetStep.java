package org.litebridgedb.orm.api.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.db.spi.update.ColumnValue;

public class UpdateSetStep<US extends UpdateStep> {

    private final US delegate;
    private final Column column;

    public UpdateSetStep(final Column column, final US delegate) {
        this.column = column;
        this.delegate = delegate;
    }

    public US to(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, value));
        return delegate;
    }

    public US increment() {
        return add(1);
    }

    public US add(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.ADD, value)));
        return delegate;
    }

    public US minus(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.SUBTRACT, value)));
        return delegate;
    }

    public US multiply(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.MULTIPLY, value)));
        return delegate;
    }

    public US divide(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.DIVIDE, value)));
        return delegate;
    }

    public US mod(final Object value) {
        ((UpdateTerminal) delegate).updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.MOD, value)));
        return delegate;
    }
}
