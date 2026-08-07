package org.litebridge.orm.persistence;

import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * Abstract base class for building SQL statements.
 */
public abstract sealed class AbstractConditionalStatementBuilder extends AbstractStatementBuilder
        permits UpdateBuilder, DeleteBuilder {

    /**
     * Constructs a new {@code AbstractConditionalStatementBuilder}.
     *
     * @param ormTable          the ORM table
     * @param litebridgeContext the ORM context
     */
    public AbstractConditionalStatementBuilder(final OrmTable ormTable,
                                               final LitebridgeContext litebridgeContext) {
        super(ormTable, litebridgeContext);
    }

    /**
     * Adds a WHERE condition to the statement.
     *
     * @param conditionNode the condition node to add
     * @return this builder instance
     */
    public AbstractConditionalStatementBuilder where(final QueryNode conditionNode) {
        this.node = new WhereNode(this.node, conditionNode);
        return this;
    }
}
