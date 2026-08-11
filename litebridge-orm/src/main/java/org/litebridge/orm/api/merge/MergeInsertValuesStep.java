package org.litebridge.orm.api.merge;

import org.litebridge.orm.expression.ExpressionSpec;

public class MergeInsertValuesStep {

    public MergeTerminal values(final ExpressionSpec expression, final ExpressionSpec... otherExpressions) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MergeTerminal values(final Object value, final Object... otherValues) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
