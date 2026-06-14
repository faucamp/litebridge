package org.litebridgedb.db.spi.alias;

import org.jspecify.annotations.Nullable;

public interface AliasTransformer {

    @Nullable String transformAlias(@Nullable String dbAlias);
}
