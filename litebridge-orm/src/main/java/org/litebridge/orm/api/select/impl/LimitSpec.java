package org.litebridge.orm.api.select.impl;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Limit;

public final class LimitSpec {

    @Nullable
    private Integer limit;
    @Nullable
    private Integer offset;

    @Nullable
    public Integer getLimit() {
        return limit;
    }

    public void setLimit(final @Nullable Integer limit) {
        this.limit = limit;
    }

    @Nullable
    public Integer getOffset() {
        return offset;
    }

    public void setOffset(final @Nullable Integer offset) {
        this.offset = offset;
    }

    Limit toLimit() {
        return new Limit(limit, offset);
    }
}
