package org.litebridge.orm.persistence;

import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.Update;

import java.util.Collections;
import java.util.List;

/**
 * A no-operation implementation of the {@link StatementBuilder} interface.
 * <p>
 * This class represents a placeholder or default implementation that does not
 * provide actual functionality for building SQL statements. It is intended to
 * signify that no statement-building operations will be performed.
 * <p>
 * Invoking the methods {@link #statementChain()} or {@link #build()} in this
 * implementation will result in throwing an {@link UnsupportedOperationException}.
 * <p>
 * This class is final and cannot be extended.
 *
 * @see StatementBuilder
 * @see StatementChain
 * @see Update
 */
public final class NoOpStatementBuilder implements StatementBuilder<Update> {

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

    @Override
    public List<BindValue> bindValues() {
        return Collections.emptyList();
    }

    /**
     * Invoking this method will throw an {@link UnsupportedOperationException}.
     *
     * @return Not supported
     * @throws UnsupportedOperationException Always thrown as this operation is not supported in the {@code NoOpStatementBuilder} implementation.
     */
    @Override
    public Update build() {
        throw new UnsupportedOperationException();
    }
}
