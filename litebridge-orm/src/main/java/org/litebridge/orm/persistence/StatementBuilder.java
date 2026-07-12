package org.litebridge.orm.persistence;

import org.litebridge.db.spi.update.UpdateStatement;

/**
 * Interface for building SQL statements.
 *
 * @param <US> The type of update statement being built.
 */
public sealed interface StatementBuilder<US extends UpdateStatement> permits AbstractStatementBuilder, NoOpStatementBuilder {

    /**
     * Returns the chain of statements built so far.
     *
     * @return The statement chain.
     */
    StatementChain statementChain();

    /**
     * Builds the final SQL statement.
     *
     * @return The built statement.
     */
    US build();
}
