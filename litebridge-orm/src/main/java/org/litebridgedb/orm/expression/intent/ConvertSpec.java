package org.litebridgedb.orm.expression.intent;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.expression.ColumnExpressionSpec;
import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;
import org.litebridgedb.orm.expression.TypeOverrideExpressionSpec;

/**
 * Converts a database result into the specified Java type.
 * <p>
 * This uses Litebridge's registered type converter to perform the conversion;
 * it is not a database operation.
 */
public final class ConvertSpec<T> implements TypeOverrideExpressionSpec<T>, Resolvable {

    private final ExpressionSpec target;
    private final Class<T> returnType;

    ConvertSpec(final ExpressionSpec target, final Class<T> returnType) {
        this.target = target;
        this.returnType = returnType;
    }

    public ExpressionSpec target() {
        return target;
    }

    @Override
    public Class<T> returnType() {
        return returnType;
    }

    @Override
    public String column() {
        return "";
    }

    @Override
    public Class<? extends ExpressionSpec> type() {
        return target.getClass();
    }

//    @Override
//    public ExpressionSpec resolve(final Table table) {
//        if (target instanceof Resolvable resolvable) {
//            return resolvable.resolve(table);
//        } else {
//            return target;
//        }
//    }
//
//    @Override
//    public ExpressionSpec resolve(final @Nullable Column column) {
//        if (target instanceof Resolvable resolvable) {
//            if (column == null) {
//                throw new IllegalStateException("Cannot resolve target; 'column' cannot be null");
//            }
//
//            return resolvable.resolve(column);
//        } else {
//            return target;
//        }
//    }
}
