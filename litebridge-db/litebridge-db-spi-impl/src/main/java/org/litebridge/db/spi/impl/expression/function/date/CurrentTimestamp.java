package org.litebridge.db.spi.impl.expression.function.date;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.AbstractAliasedExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * {@code CURRENT_TIMESTAMP} date function.
 */
public class CurrentTimestamp extends AbstractAliasedExpression {

    /**
     * Creates a new {@code CURRENT_TIMESTAMP} function.
     *
     * @param alias          the alias for the column expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    public CurrentTimestamp(final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return addAliasAs("CURRENT_TIMESTAMP", clause);
    }
}
