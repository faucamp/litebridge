package org.litebridgedb.db.postgres;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.db.spi.alias.AliasTransformer;

public class PostgresAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(@Nullable final String dbAlias) {
        return dbAlias != null ? dbAlias.toLowerCase() : null;
    }
}
