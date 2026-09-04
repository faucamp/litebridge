package org.litebridge.orm.persistence;

import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.update.Update;
import org.litebridge.orm.engine.ast.QueryNode;

/**
 * A no-operation implementation of the {@link StatementBuilder} interface.
 * <p>
 * This marker class indicates that no statement-building operations will be performed.
 * <p>
 * Invoking the methods in this implementation will result in throwing an {@link UnsupportedOperationException}.
 *
 * @see StatementBuilder
 * @see StatementChain
 * @see Update
 */
public final class NoOpStatementBuilder implements StatementBuilder {

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @return Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public QueryNode node() {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @return Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public StatementChain statementChain() {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @return Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public UpdateMetaData createUpdateMetaData() {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @param fieldName Not supported
     * @param value     Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public void setField(final String fieldName, final Object value) {
        throw new UnsupportedOperationException();
    }

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @return Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public PreparedOperation build() {
        throw new UnsupportedOperationException();
    }
}
