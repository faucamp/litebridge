package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpression;
import org.litebridge.db.spi.expression.DelegateExpression;

import static org.litebridge.db.spi.impl.ColumnIdentifierGenerator.quoteIdentifier;

/**
 * Base class for function expressions operating on a column.
 */
public abstract class FunctionExpression extends DelegateColumnExpressionImpl {

    /**
     * Constructor.
     *
     * @param target Target column expression to encapsulate.
     * @param alias  The alias for the column expression
     */
    public FunctionExpression(final ColumnExpression target, final @Nullable String alias) {
        super(target, alias);
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
        final String nestedSql = target.toSql(operation, clause, this);
        final String sql = template().formatted(nestedSql);

        if (clause == ClauseType.SELECT
                && parent == null
                && alias != null) {
            return "%s AS %s".formatted(sql, quoteIdentifier(alias));
        } else {
            return sql;
        }
    }

    /**
     * Gets the template for the SQL representation of the function.
     * <p>
     * The template should contain a single "%s" placeholder for the column identifier.
     *
     * @return SQL representation template
     */
    protected abstract String template();
}
