package org.litebridgedb.db.spi.alias;

import org.jspecify.annotations.Nullable;

/**
 * Default implementation of {@link AliasTransformer} that returns the alias as-is.
 */
public final class DefaultAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(final @Nullable String dbAlias) {
        return dbAlias;
    }
}
