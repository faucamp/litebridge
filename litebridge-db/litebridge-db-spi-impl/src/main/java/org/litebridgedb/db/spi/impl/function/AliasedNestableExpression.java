package org.litebridgedb.db.spi.impl.function;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Operation;
import org.litebridgedb.db.spi.expression.ColumnExpression;
import org.litebridgedb.db.spi.expression.NestableExpression;
import org.litebridgedb.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridgedb.db.spi.query.Select;

/**
 * A nestable column expression with support for SQL aliasing.
 * <p>
 * This class extends {@code NestableExpression} and integrates the functionality
 * of aliasing of results through a {@code ColumnIdentifierGenerator}.
 * <p>
 * The primary responsibility of this class is to provide SQL representations
 * of nestable function expressions, either with or without an alias.
 */
public class AliasedNestableExpression extends NestableExpression {

    protected final ColumnIdentifierGenerator columnIdentifierGenerator;

    public AliasedNestableExpression(final ColumnExpression target, final ColumnIdentifierGenerator columnIdentifierGenerator) {
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
    public String toSql(final Operation operation) {
        return id(column, operation);
    }

    /**
     * Creates a SQL representation of the expression, specifically including any required aliases.
     *
     * @param operation the operation that is being executed
     * @return the SQL representation of the expression
     */
    public String toSqlWithAlias(final Operation operation) {
        return idWithAlias(column, operation);
    }

    protected String id(final Column column, final Operation operation) {
        return columnIdentifierGenerator.createColumnIdentifier(column, false, operation);
    }

    protected String idWithAlias(final Column column, final Operation operation) {
        return columnIdentifierGenerator.createColumnIdentifier(column, true, operation);
    }

    protected String localId(final Operation operation) {
        if (column.alias() != null
                && operation instanceof Select select
                && select.expressions().stream()
                .filter(SelectColumn.class::isInstance)
                .anyMatch(selectColumn -> ((SelectColumn) selectColumn).column().equals(column))) {
            // If the column is selected, use the alias
            return column.alias();
        } else {
            return column.name();
        }
    }
}
