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

    QueryNode node();

    void addSetNode(Column column, @Nullable Object value, boolean bindValue);

    /**
     * Returns the chain of statements built so far.
     *
     * @return The statement chain.
     */
    StatementChain statementChain();

    UpdateMetaData createUpdateMetaData();

    /**
     * Builds the final SQL statement.
     *
     * @return The built statement.
     */
    PreparedOperation build();
}
