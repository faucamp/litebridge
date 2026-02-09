package org.litebridge.orm.api.select.model;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.query.Limit;

import java.util.Optional;

/**
 * Specification for limiting and offsetting the results of a database query.
 * <p>
 * This class contains optional properties for specifying a limit and an offset
 * to control the number of records returned and the starting point for the result set.
 * The limit specifies the maximum number of records to return, while the offset specifies
 * the number of records to skip before starting to return results.
 */
public final class LimitSpec {

    @Nullable
    private Integer limit;
    @Nullable
    private Integer offset;

    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    public void setLimit(final @Nullable Integer limit) {
        this.limit = limit;
    }

    public Optional<Integer> getOffset() {
        return Optional.ofNullable(offset);
    }

    public void setOffset(final @Nullable Integer offset) {
        this.offset = offset;
    }

    Optional<Limit> toLimit() {
        return Optional.of(new Limit(getLimit(), getOffset()));
    }
}
