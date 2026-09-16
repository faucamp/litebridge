package org.litebridge.orm.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.expression.ColumnExpressionSpec;
import org.litebridge.orm.expression.NumberTODelegateExpressionSpec;

/**
 * {@code ABS()}: Absolute value of a number.
 *
 */
public final class AbsSpec extends NumberTODelegateExpressionSpec {

    /**
     * @param target The target column/nested expression.
     */
    public AbsSpec(final ColumnExpressionSpec target, final @Nullable String alias) {
        super(target);
        this.alias = alias;
    }
}
