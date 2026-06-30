package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.orm.api.select.model.ProtoExpressionResolver;
import org.litebridgedb.orm.expression.select.SelectColumnSpec;

/**
 * Proto-expression, used to specify column names/aliases for use in the target expression type.
 * <p>
 * This record is used to create an expression instance (e.g. {@link SelectColumnSpec}) when table information is available.
 *
 * @param column The column name to select.
 * @param alias  The column alias to use, or {@code null} if not specified.
 * @param type   The type of expression to create.
 */
public record ProtoColumnExpressionSpec(Class<? extends ExpressionSpec> type,
                                        String column,
                                        @Nullable String alias,
                                        @Nullable Object @Nullable [] args)
        implements ProtoExpressionSpec {

    public ProtoColumnExpressionSpec(final Class<? extends ExpressionSpec> type, final String column, final @Nullable String alias) {
        this(type, column, alias, null);

        // Validate that a supported expression type is specified
        if (!ProtoExpressionResolver.isSupported(type)) {
            throw new IllegalArgumentException("Unsupported expression type: " + type);
        }
    }

    public ProtoColumnExpressionSpec(final Class<? extends ExpressionSpec> type, final String column) {
        this(type, column, null);
    }
}
