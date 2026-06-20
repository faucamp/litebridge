package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;

public sealed interface ProtoExpressionSpec extends ExpressionSpec, Resolvable permits ProtoColumnExpressionSpec, ProtoNestableExpressionSpec {

    @Nullable String alias();

    /**
     * Gets any extra expression-specific arguments.
     *
     * @return the extra arguments, or {@code null} if none
     */
    @Nullable Object @Nullable [] args();

}
