package org.litebridge.orm.expression.intent;

import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Resolvable;

public record ExpressionSpecArray(ExpressionSpec[] expressions) implements ExpressionSpec {

    public boolean containsResolvable() {
        for (final ExpressionSpec expression : expressions) {
            if (expression instanceof Resolvable) {
                return true;
            }
        }

        return false;
    }
}
