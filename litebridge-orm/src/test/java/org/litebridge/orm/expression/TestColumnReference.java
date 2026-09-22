//package org.litebridge.orm.expression;
//
//import org.jspecify.annotations.Nullable;
//import org.litebridge.db.spi.Column;
//import org.litebridge.db.spi.Operation;
//import org.litebridge.db.spi.expression.ClauseType;
//import org.litebridge.db.spi.expression.ColumnReference;
//import org.litebridge.db.spi.expression.DelegateExpression;
//
//import java.util.Objects;
//
//public class TestColumnReference extends ColumnReference {
//
//    public TestColumnReference(final Column column) {
//        super(column);
//    }
//
//    @Override
//    public String toSql(final Operation operation, final ClauseType context, final @Nullable DelegateExpression parent) {
//        return column().name();
//    }
//
//    @Override
//    public boolean equals(final Object obj) {
//        return obj instanceof TestColumnReference testColumnExpression
//                && Objects.equals(this.column, testColumnExpression.column);
//    }
//
//    @Override
//    public int hashCode() {
//        return column.hashCode();
//    }
//}
