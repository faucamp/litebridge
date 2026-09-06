package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.orm.engine.AbstractInsertEngine;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;

import java.util.Objects;

/**
 * Abstract base class for building SQL statements.
 */
public abstract sealed class AbstractStatementBuilder implements StatementBuilder
        permits AbstractConditionalStatementBuilder, InsertBuilder {

    /**
     * The ORM table associated with the statement.
     */
    protected final OrmTable ormTable;
    private final StatementChain statementChain = new StatementChain();

    /**
     * The ORM context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * The current query node.
     */
    protected @Nullable QueryNode node;

    /**
     * Constructs a new {@code AbstractStatementBuilder}.
     *
     * @param ormTable          The ORM table.
     * @param litebridgeContext The ORM context.
     */
    protected AbstractStatementBuilder(final OrmTable ormTable,
                                       final LitebridgeContext litebridgeContext) {
        this.ormTable = ormTable;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public QueryNode node() {
        return Objects.requireNonNull(node, "Statement builder node not set");
    }

    @Override
    public StatementChain statementChain() {
        return statementChain;
    }

    @Override
    public abstract PreparedOperation build();

    @Override
    public UpdateMetaData createUpdateMetaData(final PreparedOperation preparedOperation) {
        return AbstractInsertEngine.createUpdateMetaData(preparedOperation,
                () -> preparedOperation.operation().table(),
                litebridgeContext);
    }
}
