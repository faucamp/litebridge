package org.litebridgedb.db.spi.alias;

import org.jspecify.annotations.Nullable;

/**
 * A transformer for database aliases
 * <p>
 * This interface provides a method for transforming a given alias string
 * into another form, which might be based on specific database or application requirements.
 * <p>
 * Implementations of this interface can apply various transformation rules,
 * such as returning the alias as-is, converting it to uppercase, or other
 * modifications that suit particular use cases.
 */
public interface AliasTransformer {

    /**
     * Transforms the given database alias into another form.
     * <p>
     * The transformation rules are implementation-specific and can vary based on the requirements of the database or application.
     *
     * @param dbAlias The alias to be transformed.
     * @return The transformed alias, or null if the input alias is null.
     */
    @Nullable String transformAlias(@Nullable String dbAlias);
}
