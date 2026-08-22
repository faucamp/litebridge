package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.orm.api.select.ast.QueryNode;

import java.util.List;

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
    UpdateMetaData createUpdateMetaData();

    /**
     * Builds the final SQL statement.
     *
     * @return The built statement.
     */
    PreparedOperation build();
}
