package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;

/**
 * Proto-SelectColumn expression, used to specify column names/aliases for selection.
 * <p>
 * This record is used to create a {@link SelectColumn} instance when table information is available.
 *
 * @param column The column name to select.
 * @param alias  The column alias to use, or {@code null} if not specified.
 */
public record ProtoSelectColumn(String column, @Nullable String alias) implements Expression {
}
