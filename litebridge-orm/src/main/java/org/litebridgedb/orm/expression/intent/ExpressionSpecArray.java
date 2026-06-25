package org.litebridgedb.orm.expression.intent;

import org.litebridgedb.orm.expression.ExpressionSpec;
import org.litebridgedb.orm.expression.Resolvable;

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
