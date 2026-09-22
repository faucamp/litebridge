package org.litebridge.db.spi.impl.expression.function.scalar;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.function.FunctionExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * {@code LOWER(column)} scalar function.
 */
public class Lower extends FunctionExpression {

    /**
     * Creates a new {@code LOWER} scalar function expression.
     *
     * @param target         the target column expression
     * @param alias          the alias for the column expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public Lower(final SelectExpression target, final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(target, alias, labelGenerator);
    }

    @Override
    protected String template() {
        return "LOWER(%s)";
    }
}
