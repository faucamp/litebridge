package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.update.UpdateStatement;

/**
 * Abstract base class for building SQL statements.
 *
 * @param <US> The type of update statement being built.
 */
public abstract sealed class AbstractStatementBuilder<US extends UpdateStatement> implements StatementBuilder<US>
        permits InsertBuilder, UpdateBuilder, DeleteBuilder {

    /**
     * The ORM table associated with the statement.
     */
    protected final OrmTable ormTable;
    private final StatementChain statementChain = new StatementChain();

    /**
     * Constructs a new {@code AbstractStatementBuilder}.
     *
     * @param ormTable The ORM table.
     */
    protected AbstractStatementBuilder(final OrmTable ormTable) {
        this.ormTable = ormTable;
    }

    public StatementChain statementChain() {
        return statementChain;
    }

    public abstract US build();
}
