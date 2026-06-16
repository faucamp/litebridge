package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;

/**
 * Type override proto-expression, used to specify column names/aliases for use in the target expression type.
 * <p>
 * This record is used to create an expression instance (e.g. {@link SelectColumn}) when table information is available.
 *
 * @param column The column name to select.
 * @param alias  The column alias to use, or {@code null} if not specified.
 * @param type   The type of expression to create.
 */
public record ProtoTOColumnExpression<T>(Class<T> typeOverride, Class<? extends Expression> type, String column,
                                         @Nullable String alias) implements ProtoExpression, TypeOverrideExpression<T> {

    @Override
    public Class<T> returnType() {
        return typeOverride;
    }
}
