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
public record ConvertSpec<T>(ExpressionSpec target,
                             Class<T> returnType) implements TypeOverrideExpressionSpec<T>, Resolvable {

    /**
     * Constructs a {@code ConvertSpec} with the specified target expression and return type.
     *
     * @param target     the target expression to convert
     * @param returnType the class of the target Java type
     */
    public ConvertSpec {
    }

    /**
     * Gets the target expression for conversion.
     *
     * @return the target expression
     */
    @Override
    public ExpressionSpec target() {
        return target;
    }

    @Override
    public String column() {
        return target instanceof Resolvable resolvable ? resolvable.column() : "";
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
