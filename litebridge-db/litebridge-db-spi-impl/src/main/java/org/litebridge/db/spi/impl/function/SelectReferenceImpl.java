package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectReference;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;

import java.util.StringJoiner;

public class SelectReferenceImpl extends SelectReference {

    private final ColumnIdentifierGenerator columnIdentifierGenerator;

    /**
     * Constructor.
     *
     * @param column                    The target selected column to reference.
     * @param columnIdentifierGenerator The column identifier generator to use.
     */
    public SelectReferenceImpl(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column);
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        return columnIdentifierGenerator.createColumnRef(column, operation, clause);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", SelectReferenceImpl.class.getSimpleName() + "[", "]")
                .add("column=" + column)
                .toString();
    }
}
