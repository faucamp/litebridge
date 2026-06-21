package org.litebridgedb.db.spi.alias;

import org.jspecify.annotations.Nullable;

public final class DefaultAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(final @Nullable String dbAlias) {
        return dbAlias;
    }
}
