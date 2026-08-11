package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.orm.expression.ExpressionSpec;
import org.litebridge.orm.expression.select.SelectColumnSpec;

public class MergeInsertStep {

    public MergeTerminal insert(final Object dto) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public MergeInsertValuesStep insert(final ExpressionSpec expression, final ExpressionSpec... otherExpressions) {
        return new MergeInsertValuesStep();
    }

    public MergeInsertValuesStep insert(final String column, final String... otherColumns) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
