package org.litebridgedb.orm.expression;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

public interface Resolvable {

    String column();

    Class<? extends ExpressionSpec> type();

    /**
     * Resolves this {@code Resolvable} into an {@link ExpressionSpec} using the specified table.
     *
     * @param table the table to use for resolution
     * @return the resolved {@link ExpressionSpec} corresponding to the provide table
     */
//    ExpressionSpec resolve(Table table);

    /**
     * Resolves this {@code Resolvable} into an {@link ExpressionSpec} using the specified column.
     * <p>
     * Implementations may differ in handling {@code null} values for the {@code column} parameter.
     *
     * @param column the column to use for resolution; may be {@code null}
     * @return the resolved {@link ExpressionSpec} corresponding to the provided column
     */
//    ExpressionSpec resolve(@Nullable Column column);
}
