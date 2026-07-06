package org.litebridgedb.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ClauseType;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.DelegateExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;

/**
 * Base class for function expressions operating on a column.
 */
public abstract class FunctionExpression extends DelegateColumnExpressionImpl {

    /**
     * Constructor.
     *
     * @param target                    Target column expression to encapsulate.
     * @param columnIdentifierGenerator Database provider-specific column identifier generator.
     */
    public FunctionExpression(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
        super(target, columnIdentifierGenerator);
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
                && target.column().alias() != null) {
            return "%s %s".formatted(sql, columnIdentifierGenerator.createAliasDeclaration(column.alias()));
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
