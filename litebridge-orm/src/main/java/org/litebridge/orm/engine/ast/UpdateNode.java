package org.litebridge.orm.engine.ast;

import org.jspecify.annotations.Nullable;

/**
 * Represents an UPDATE statement in the query AST.
 *
 * @param dtoClass        Mapped DTO class to update.
 * @param contextDtoClass The parent/context of the mapped DTO class.
 * @param table           the table to update
 * @param dtoClass        class of the DTO to update
 */
public record UpdateNode(@Nullable Class<?> dtoClass,
                         @Nullable Class<?> contextDtoClass,
                         @Nullable String table) implements QueryNode {

    /**
     * Creates an {@code UpdateNode} for updating a mapped DTO class.
     *
     * @param dtoClass        Mapped DTO class to update.
     * @param contextDtoClass The parent/context of the mapped DTO class.
     */
    public UpdateNode(final Class<?> dtoClass, final @Nullable Class<?> contextDtoClass) {
        this(dtoClass, contextDtoClass, null);
    }

    /**
     * Creates an {@code UpdateNode} for updating a table directly.
     *
     * @param table Name of the table to update.
     */
    public UpdateNode(String table) {
        this(null, null, table);
    }

    @Override
    public @Nullable QueryNode previous() {
        return null;
    }
}
