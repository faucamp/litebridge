package org.litebridge.db.postgres.expression.function.aggregate;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.expression.function.aggregate.Count;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * PostgreSQL-specific {@code COUNT(*)} aggregate function.
 */
public class PostgresCount extends Count {

    public PostgresCount(final @Nullable String alias, final LabelGenerator labelGenerator) {
        super(alias, labelGenerator);
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (clause == ClauseType.SELECT) {
            return addAliasAs("COUNT(*)", clause);
        } else {
            return "count";
        }
    }
}
