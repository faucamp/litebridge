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
@Deprecated(forRemoval = true)
public final class LimitSpec {

    @Nullable
    private Integer limit;
    @Nullable
    private Integer offset;

    /**
     * Returns the limit as an {@link Optional}.
     *
     * @return an optional containing the limit.
     */
    public Optional<Integer> getLimit() {
        return Optional.ofNullable(limit);
    }

    /**
     * Sets the limit for the query results.
     *
     * @param limit the limit to set.
     */
    public void setLimit(final @Nullable Integer limit) {
        this.limit = limit;
    }

    /**
     * Returns the offset as an {@link Optional}.
     *
     * @return an optional containing the offset.
     */
    public Optional<Integer> getOffset() {
        return Optional.ofNullable(offset);
    }

    /**
     * Sets the offset for the query results.
     *
     * @param offset the offset to set.
     */
    public void setOffset(final @Nullable Integer offset) {
        this.offset = offset;
    }

    /**
     * Converts this specification to a database SPI {@link Limit} object.
     *
     * @return an optional containing the SPI limit object.
     */
    public Optional<Limit> toLimit() {
//        return Optional.of(new Limit(getLimit(), getOffset()));
        throw new UnsupportedOperationException("Deprecated");
    }
}
