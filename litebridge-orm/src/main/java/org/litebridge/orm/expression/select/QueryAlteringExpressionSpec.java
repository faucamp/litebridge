package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Limit;
import org.litebridge.orm.expression.TypeOverrideExpressionSpec;

/**
 * An expression that alters the query in some way.
 * <p>
 * It allows setting a SQL limit and can return a fallback value if no database records were matched.
 *
 * @param delegate
 * @param noMatchFallback
 * @param limit
 * @param <T>
 */
public record QueryAlteringExpressionSpec<T>(TypeOverrideExpressionSpec<T> delegate,
                                             @Nullable T noMatchFallback,
                                             Limit limit) implements TypeOverrideExpressionSpec<T> {

    @Override
    public Class<T> returnType() {
        return delegate.returnType();
    }
}
