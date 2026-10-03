package org.litebridge.db.spi.impl.expression.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.DelegateExpression;
import org.litebridge.db.spi.expression.SelectExpression;
import org.litebridge.db.spi.impl.expression.AbstractAliasedExpression;
import org.litebridge.db.spi.impl.sql.LabelGenerator;

/**
 * Base class for function expressions operating on a column.
 */
public abstract class FunctionExpression extends AbstractAliasedExpression implements DelegateExpression {

    private final SelectExpression target;

    /**
     * Constructor.
     *
     * @param target         Target column expression to encapsulate.
     * @param alias          The alias for the column expression
     * @param labelGenerator the label generator for rendering aliases/identifiers
     */
    protected FunctionExpression(final SelectExpression target,
                                 final @Nullable String alias,
                                 final LabelGenerator labelGenerator) {
        super(alias, null, labelGenerator);
        this.target = target;
    }

    @Override
    public SelectExpression target() {
        return target;
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
        final String nestedSql = target.toSql(operation, clause, null);
        final String sql = template().formatted(nestedSql);
        return addAliasAs(sql, clause);
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
