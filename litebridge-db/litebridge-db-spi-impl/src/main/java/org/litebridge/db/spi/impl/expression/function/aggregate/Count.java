package org.litebridge.db.spi.impl.expression.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.AbstractAliasedExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * {@code COUNT(*)} aggregate function.
 */
public class Count extends AbstractAliasedExpression {

    public Count(final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return addAliasAs("COUNT(*)", clause);
    }
}
