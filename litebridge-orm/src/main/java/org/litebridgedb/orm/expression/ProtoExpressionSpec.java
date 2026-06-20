package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;

/**
 * Proto-expression, used to prescribe a {@link ExpressionSpec} type when sufficient information is available.
 */
public sealed interface ProtoExpressionSpec extends ExpressionSpec, Resolvable permits ProtoColumnExpressionSpec, ProtoNestableExpressionSpec {

    /**
     * Gets the column alias to use, or {@code null} if not specified.
     *
     * @return the column alias to use, or {@code null} if not specified.
     */
    @Nullable String alias();

    /**
     * Gets any extra expression-specific arguments.
     *
     * @return the extra arguments, or {@code null} if none
     */
    @Nullable Object @Nullable [] args();

}
