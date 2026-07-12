package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpressionImpl;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;

/**
 * Expression to select a database column.
 */
public class SelectColumn extends ColumnExpressionImpl {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public SelectColumn(final Column column, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(column);
        this.columnIdentifierGenerator = columnIdentifierGenerator;
    }

    /**
     * Creates a SQL representation of the expression.
     * <p>
     * This is usually used for expressions that do not require any aliases.
     *
     * @param operation the operation that is being executed
     * @return the SQL representation of the expression
     */
    @Override
    public String toSql(final Operation operation, final ClauseType clause, final @Nullable DelegateExpression parent) {
        if (clause == ClauseType.SELECT) {
            return columnIdentifierGenerator.createSelectColumn(column, operation, clause, (parent != null));
        } else {
            return columnIdentifierGenerator.createColumnRef(column, operation, clause);
        }
    }
}
