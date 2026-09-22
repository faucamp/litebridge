package org.litebridge.db.spi.impl.expression.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.function.FunctionExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * {@code AVG(column)} aggregate function.
 */
public class Avg extends FunctionExpression {

    /**
     * Constructs a new {@code AVG} function expression.
     *
     * @param target         The column expression to apply the function to.
     * @param alias          The alias for the column expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public Avg(final SelectExpression target, final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(target, alias, labelGenerator);
    }

    @Override
    protected String template() {
        return "AVG(%s)";
    }
}
