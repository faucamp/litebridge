package org.litebridge.db.spi.impl.expression.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.function.FunctionExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * {@code MIN(column)} aggregate function.
 */
public class Min extends FunctionExpression {

    /**
     * Creates a new {@code MIN} aggregate function.
     *
     * @param target         the target column expression
     * @param alias          the alias for the column expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public Min(final SelectExpression target, final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(target, alias, labelGenerator);
    }

    @Override
    protected String template() {
        return "MIN(%s)";
    }
}
