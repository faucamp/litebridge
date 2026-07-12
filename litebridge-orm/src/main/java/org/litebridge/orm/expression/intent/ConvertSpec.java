package org.litebridge.orm.expression.intent;

import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Resolvable;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

/**
 * Converts a database result into the specified Java type.
 * <p>
 * This uses Litebridge's registered type converter to perform the conversion;
 * it is not a database operation.
 *
 * @param <T> The target Java type for conversion.
 */
public final class ConvertSpec<T> implements TypeOverrideExpressionSpec<T>, Resolvable {

    private final ExpressionSpec target;
    private final Class<T> returnType;

    /**
     * Constructs a {@code ConvertSpec} with the specified target expression and return type.
     *
     * @param target     the target expression to convert
     * @param returnType the class of the target Java type
     */
    public ConvertSpec(final ExpressionSpec target, final Class<T> returnType) {
        this.target = target;
        this.returnType = returnType;
    }

    /**
     * Gets the target expression for conversion.
     *
     * @return the target expression
     */
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

    /**
     * Creates a new {@code ConvertSpec} with a replaced target expression.
     *
     * @param resolvedExpressionSpec the new target expression
     * @return a new {@code ConvertSpec} instance
     */
    public ConvertSpec<T> replaceTarget(final ExpressionSpec resolvedExpressionSpec) {
        return new ConvertSpec<>(resolvedExpressionSpec, returnType);
    }
}
