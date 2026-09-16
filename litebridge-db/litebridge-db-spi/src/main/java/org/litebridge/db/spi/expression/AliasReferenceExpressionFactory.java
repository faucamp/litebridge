package org.litebridge.db.spi.expression;

import org.jspecify.annotations.Nullable;

/**
 * Factory to create references to selected columns.
 */
@FunctionalInterface
public interface AliasReferenceExpressionFactory {

    /**
     * Creates a reference to a selected column.
     *
     * @param tableAlias The "from alias" to use for the reference.
     * @param alias      The selected column from the alias to reference.
     * @return A new reference expression.
     */
    AliasReference create(final String alias, final @Nullable String tableAlias);
}
