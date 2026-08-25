//package org.litebridge.orm.api.sql.update;
//
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.math.MathOperation;
//
//public class SqlUpdateSetStep {
//
//    private final SqlUpdater delegate;
//    private final String column;
//
//    public SqlUpdateSetStep(final String column, final SqlUpdater delegate) {
//        this.column = column;
//        this.delegate = delegate;
//    }
//
//    public SqlUpdateStep to(final Object value) {
//        delegate.addSetNode(column, value);
//        return delegate;
//    }
//
//    public SqlUpdateStep increment() {
//        return add(1);
//    }
//
//    public SqlUpdateStep add(final Object value) {
//        delegate.addSetNode(column, new MathOperation(MathOperation.Operator.ADD, value));
//        return delegate;
//    }
//
//    public SqlUpdateStep minus(final Object value) {
//        delegate.addSetNode(column, new MathOperation(MathOperation.Operator.SUBTRACT, value));
//        return delegate;
//    }
//
//    public SqlUpdateStep multiply(final Object value) {
//        delegate.addSetNode(column, new MathOperation(MathOperation.Operator.MULTIPLY, value));
//        return delegate;
//    }
//
//    public SqlUpdateStep divide(final Object value) {
//        delegate.addSetNode(column, new MathOperation(MathOperation.Operator.DIVIDE, value));
//        return delegate;
//    }
//
//    public SqlUpdateStep mod(final Object value) {
//        delegate.addSetNode(column, new MathOperation(MathOperation.Operator.MOD, value));
//        return delegate;
//    }
//}
