package org.litebridgedb.db.spi.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.sql.PreparedSql;
import org.litebridgedb.db.spi.tx.ConnectionProvider;

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
    PreparedSql toSql(final Operation operation, final ConnectionProvider connectionProvider);

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
