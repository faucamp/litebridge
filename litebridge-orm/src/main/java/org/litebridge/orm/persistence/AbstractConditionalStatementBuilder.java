package org.litebridge.orm.persistence;

import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;

/**
 * Abstract base class for building SQL statements.
 */
public abstract sealed class AbstractConditionalStatementBuilder extends AbstractStatementBuilder
        permits UpdateBuilder, DeleteBuilder {

    public AbstractConditionalStatementBuilder(final OrmTable ormTable,
                                               final LitebridgeContext litebridgeContext) {
        super(ormTable, litebridgeContext);
    }

    public AbstractConditionalStatementBuilder where(final QueryNode conditionNode) {
        this.node = new WhereNode(this.node, conditionNode);
        return this;
    }
}
