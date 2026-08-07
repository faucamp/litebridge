package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.sql.PreparedSql;
import org.litebridge.db.spi.tx.ConnectionProvider;

/**
 * A select expression in a SQL query that requires a {@link ConnectionProvider} to render.
 */
public interface ConnectionProviderExpression extends SelectExpression {

    /**
     * Creates a SQL representation of the expression using the specified @{link ConnectionProvider}.
     *
     * @param operation          the operation that is being executed
     * @param connectionProvider the connection provider to use to generate SQL
     * @return the SQL representation of the expression
     */
    String toSql(final Operation operation, final ConnectionProvider connectionProvider);

    /**
     * Not supported.
     * <p>
     * This default implementation throws an {@code UnsupportedOperationException},
     * indicating that a connection provider is required.
     *
     * @param operation the operation being executed to generate the SQL expression
     * @return the SQL representation of this expression
     * @throws UnsupportedOperationException since a {@link ConnectionProvider} is required
     */
    @Override
    default String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        throw new UnsupportedOperationException("toSql() for " + getClass().getSimpleName() + " requires a connection provider");
    }
}
