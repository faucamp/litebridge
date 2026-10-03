package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.engine.ast.QueryNode;
import org.litebridge.orm.engine.ast.WhereNode;

/**
 * Abstract base class for building SQL statements.
 */
public abstract sealed class AbstractConditionalStatementBuilder extends AbstractStatementBuilder
        permits DeleteBuilder, UpdateBuilder {

    /**
     * Creates a new {@code AbstractConditionalStatementBuilder} instance.
     *
     * @param ormTable          The ORM table for the target DTO.
     * @param contextDtoClass   The parent/context DTO class.
     * @param litebridgeContext Litebridge context.
     */
    public AbstractConditionalStatementBuilder(final OrmTable ormTable,
                                               final @Nullable Class<?> contextDtoClass,
                                               final LitebridgeContext litebridgeContext) {
        super(ormTable, contextDtoClass, litebridgeContext);
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
