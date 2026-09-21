package org.litebridge.orm.expression;

import org.jspecify.annotations.Nullable;

public interface Aliasable {

    @Nullable String getAlias();

    void setAlias(final String alias);

    Aliasable as(final String alias);
}
