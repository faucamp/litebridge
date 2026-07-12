package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.DelegateColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;

/**
 * A nestable column expression with support for SQL aliasing.
 * <p>
 * This class extends {@code NestableExpression} and integrates the functionality
 * of aliasing of results through a {@code ColumnIdentifierGenerator}.
 * <p>
 * The primary responsibility of this class is to provide SQL representations
 * of nestable function expressions, either with or without an alias.
 */
public class DelegateColumnExpressionImpl extends DelegateColumnExpression {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public DelegateColumnExpressionImpl(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target);
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
