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
