package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Limit;

import java.util.Optional;

public final class LimitSpec {

    private Optional<Integer> limit = Optional.empty();
    private Optional<Integer> offset = Optional.empty();

    public Optional<Integer> getLimit() {
        return limit;
    }

    public void setLimit(final @Nullable Integer limit) {
        this.limit = Optional.ofNullable(limit);
    }

    public Optional<Integer> getOffset() {
        return offset;
    }

    public void setOffset(final @Nullable Integer offset) {
        this.offset = Optional.ofNullable(offset);
    }

    Optional<Limit> toLimit() {
        return Optional.of(new Limit(limit, offset));
    }
}
