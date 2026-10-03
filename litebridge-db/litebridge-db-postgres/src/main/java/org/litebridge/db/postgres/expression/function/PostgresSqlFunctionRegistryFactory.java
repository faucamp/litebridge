package org.litebridge.db.postgres.expression.function;

import org.litebridge.db.postgres.expression.function.aggregate.PostgresCount;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.SqlFunctionRegistryFactory;
import org.litebridge.db.spi.impl.sql.LabelGenerator;
import org.litebridge.db.spi.impl.sql.SelectSqlGenerator;

/**
 * Oracle-specific {@link SqlFunctionRegistryFactory}.
 * <p>
 * This substitutes specific SQL functions for Oracle-specific ones.
 */
public final class PostgresSqlFunctionRegistryFactory extends SqlFunctionRegistryFactory {

    /**
     * Constructs a new {@code OracleSqlFunctionRegistryFactory}.
     *
     * @param labelGenerator     the label generator for rendering aliases/identifiers
     * @param selectSqlGenerator The database provider's select SQL generator
     */
    public PostgresSqlFunctionRegistryFactory(final LabelGenerator labelGenerator,
                                              final SelectSqlGenerator selectSqlGenerator) {
        super(labelGenerator, selectSqlGenerator);
    }

    @Override
    protected SelectExpression createCount() {
        return new PostgresCount(null, labelGenerator);
    }
}
