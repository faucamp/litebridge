package org.litebridge.orm.persistence;

import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.UpdateStatement;

import java.util.ArrayList;
import java.util.List;

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
    private final List<BindValue> bindValues = new ArrayList<>();

    /**
     * Constructs a new {@code AbstractStatementBuilder}.
     *
     * @param ormTable The ORM table.
     */
    protected AbstractStatementBuilder(final OrmTable ormTable) {
        this.ormTable = ormTable;
    }

    @Override
    public StatementChain statementChain() {
        return statementChain;
    }

    @Override
    public List<BindValue> bindValues() {
        return bindValues;
    }

    @Override
    public abstract US build();
}
