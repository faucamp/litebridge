package org.litebridge.db.spi.impl.function;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.expression.ClauseType;
import org.litebridge.db.spi.expression.ColumnExpressionImpl;
import org.litebridge.db.spi.expression.DelegateExpression;

import static org.litebridge.db.spi.impl.ColumnIdentifierGenerator.quoteIdentifier;

/**
 * Expression to select a database column.
 */
public class SelectColumn extends ColumnExpressionImpl {

    /**
     * Creates a new {@code SelectColumn}.
     *
     * @param column the column to select
     * @param alias  the alias to assign to the column
     */
    public SelectColumn(final Column column, final @Nullable String alias, final @Nullable String tableAlias) {
        super(column, alias, tableAlias);
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
        final StringBuilder sb = new StringBuilder();
        final Table table = column.table();

        if (tableAlias != null) {
            sb.append(quoteIdentifier(tableAlias));
        } else {
            sb.append(quoteIdentifier(table.name()));
        }

        sb.append('.').append(quoteIdentifier(column.name()));

        if (alias != null) {
            sb.append(" AS \"").append(alias).append("\"");
        }

        return sb.toString();
    }
}
