package org.litebridge.orm.persistence;

import org.litebridge.commons.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the result of a composite update operation, aggregating multiple {@link DtoUpdateResult} objects
 * that represent individual update results.
 * <p>
 * This class is immutable and maintains an internal list of {@link DtoUpdateResult}, allowing for chained
 * operations while maintaining encapsulation.
 */
public final class CompositeUpdateResult {

    private final List<DtoUpdateResult> dtoUpdateResults = new ArrayList<>();

    /**
     * Adds a {@link DtoUpdateResult} to the composite update result.
     *
     * @param dtoUpdateResult The {@link DtoUpdateResult} to add.
     * @return This {@link CompositeUpdateResult} instance, allowing for method chaining.
     */
    public CompositeUpdateResult add(final DtoUpdateResult dtoUpdateResult) {
        dtoUpdateResults.add(dtoUpdateResult);
        return this;
    }

    /**
     * Merges another {@link CompositeUpdateResult} into this one, adding all its {@link DtoUpdateResult} objects.
     *
     * @param compositeUpdateResult The {@link CompositeUpdateResult} to merge.
     * @return This {@link CompositeUpdateResult} instance, allowing for method chaining.
     */
    public CompositeUpdateResult merge(final CompositeUpdateResult compositeUpdateResult) {
        dtoUpdateResults.addAll(compositeUpdateResult.dtoUpdateResults);
        return this;
    }

    /**
     * Returns the list of {@link DtoUpdateResult} objects contained in this composite update result.
     *
     * @return The list of {@link DtoUpdateResult} objects.
     */
    public List<DtoUpdateResult> results() {
        return dtoUpdateResults;
    }

    /**
     * Returns the primary {@link DtoUpdateResult} from the composite update result.
     * The primary result is the first non-null result in the list of {@link DtoUpdateResult} objects.
     *
     * @return The primary {@link DtoUpdateResult} or null if no non-null result is found.
     */
    public DtoUpdateResult primary() {
        return CollectionUtils.requireNonEmpty(dtoUpdateResults, "Update results not set").getFirst();
    }
}
