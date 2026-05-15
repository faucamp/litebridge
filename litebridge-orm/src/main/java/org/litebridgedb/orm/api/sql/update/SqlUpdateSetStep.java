package org.litebridgedb.orm.api.sql.update;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.math.MathOperation;
import org.litebridgedb.db.spi.update.ColumnValue;

public class SqlUpdateSetStep {

    private final SqlUpdater delegate;
    private final Column column;

    public SqlUpdateSetStep(final Column column, final SqlUpdater delegate) {
        this.column = column;
        this.delegate = delegate;
    }

    public SqlUpdateStep to(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, value));
        return delegate;
    }

    public SqlUpdateStep increment() {
        return add(1);
    }

    public SqlUpdateStep add(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.ADD, value)));
        return delegate;
    }

    public SqlUpdateStep minus(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.SUBTRACT, value)));
        return delegate;
    }

    public SqlUpdateStep multiply(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.MULTIPLY, value)));
        return delegate;
    }

    public SqlUpdateStep divide(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.DIVIDE, value)));
        return delegate;
    }

    public SqlUpdateStep mod(final Object value) {
        delegate.updateSpec().addColumnValue(new ColumnValue(column, new MathOperation(MathOperation.Operator.MOD, value)));
        return delegate;
    }
}
