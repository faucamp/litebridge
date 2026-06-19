package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.expression.select.SelectColumn;

/**
 * Proto-expression, used to specify column names/aliases for use in the target expression type.
 * <p>
 * This record is used to create an expression instance (e.g. {@link SelectColumn}) when table information is available.
 *
 * @param column The column name to select.
 * @param alias  The column alias to use, or {@code null} if not specified.
 * @param type   The type of expression to create.
 */
public record ProtoColumnExpression(Class<? extends Expression> type, String column, @Nullable String alias)
        implements ProtoExpression {

    public ProtoColumnExpression(final Class<? extends Expression> type, final String column, final @Nullable String alias) {
        // Validate that a supported expression type is specified
        if (!ProtoExpressionRegistry.isSupported(type)) {
            throw new IllegalArgumentException("Unsupported expression type: " + type);
        }

        this.type = type;
        this.column = column;
        this.alias = alias;
    }

    public ProtoColumnExpression(final Class<? extends Expression> type, final String column) {
        this(type, column, null);
    }

    @Override
    public @Nullable Object @Nullable [] args() {
        return null;
    }

    public Expression resolve(final Column column) {
        return ProtoExpressionRegistry.resolve(type(), column, args());
    }

    public Expression resolve(final Table table) {
        return ProtoExpressionRegistry.resolve(type(), new Column(table, column(), alias()), args());
    }
}
