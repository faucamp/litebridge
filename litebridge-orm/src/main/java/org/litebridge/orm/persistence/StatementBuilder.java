package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.update.UpdateResult;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * Interface for building SQL execution/update statements.
 */
public sealed interface StatementBuilder permits AbstractStatementBuilder, NoOpStatementBuilder {

    /**
     * Returns the current query node.
     *
     * @return the query node
     */
    QueryNode node();

    /**
     * Returns the chain of statements built so far.
     *
     * @return The statement chain.
     */
    StatementChain statementChain();

    /**
     * Creates the update metadata for the statement.
     *
     * @return the update metadata
     */
    UpdateMetaData createUpdateMetaData(final PreparedOperation preparedOperation);

    /**
     * Sets a field value on the statement.
     *
     * @param fieldName the name of the field to set
     * @param value     the value to set
     */
    void setField(String fieldName, @Nullable Object value);

    /**
     * Builds the final SQL statement.
     *
     * @return The built statement.
     */
    PreparedOperation build();

    default Class<? extends UpdateResult> resultType() {
        return UpdateResult.class;
    }
}
