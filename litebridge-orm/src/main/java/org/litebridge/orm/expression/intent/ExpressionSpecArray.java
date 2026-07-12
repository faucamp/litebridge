package org.litebridge.orm.expression.intent;

import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.Resolvable;

/**
 * Array of expression specifications.
 * @param expressions the expressions.
 */
public record ExpressionSpecArray(ExpressionSpec[] expressions) implements ExpressionSpec {

    /**
     * Checks if the array contains a resolvable expression.
     *
     * @return true if the array contains a resolvable expression.
     */
    public boolean containsResolvable() {
        for (final ExpressionSpec expression : expressions) {
            if (expression instanceof Resolvable) {
                return true;
            }
        }

        return false;
    }
}
