package org.litebridgedb.db.spi.impl.alias;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.alias.AliasTransformer;

public final class UppercaseAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(final @Nullable String dbAlias) {
        return dbAlias != null ? dbAlias.toUpperCase() : null;
    }
}
