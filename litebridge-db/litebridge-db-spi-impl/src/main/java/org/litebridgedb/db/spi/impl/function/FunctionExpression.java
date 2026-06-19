package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;

/**
 * Base class for function expressions operating on a column.
 */
public abstract class FunctionExpression extends AliasedNestableExpression {

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
    public String toSql(final Operation operation) {
        return prepareSql(operation, false);
    }

    /**
     * Creates a SQL representation of the expression, specifically including any required aliases.
     *
     * @param operation the operation that is being executed
     * @return the SQL representation of the expression
     */
    public String toSqlWithAlias(final Operation operation) {
        return prepareSql(operation, column.alias() != null);
    }

    /**
     * Prepare SQL representation of the function.
     *
     * @param operation The operation that is being executed
     * @param alias     Whether to alias the SQL function result
     * @return SQL representation of the function
     */
    protected String prepareSql(final Operation operation, final boolean alias) {
        final String nestedSql = target.toSql(operation);
        final String sql = template().formatted(nestedSql);

        if (alias) {
            return "%s %s".formatted(sql, columnIdentifierGenerator.createAlias(column.alias()));
        } else {
            return sql;
        }

//        if (alias) {
//            return "%s %s".formatted(template().formatted(localId(operation)),
//                    columnIdentifierGenerator.createAlias(column.alias()));
//        } else {
//            return template().formatted(localId(operation));
//        }
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
