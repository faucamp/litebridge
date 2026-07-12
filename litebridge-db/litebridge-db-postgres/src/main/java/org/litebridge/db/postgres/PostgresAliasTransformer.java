package org.litebridge.db.postgres;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.alias.AliasTransformer;

/**
 * PostgreSQL-specific implementation of {@link AliasTransformer}.
 * <p>
 * This transformer converts database aliases to lowercase, matching PostgreSQL's
 * default behavior of folding unquoted identifiers to lowercase.
 */
public class PostgresAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(@Nullable final String dbAlias) {
        return dbAlias != null ? dbAlias.toLowerCase() : null;
    }
}
