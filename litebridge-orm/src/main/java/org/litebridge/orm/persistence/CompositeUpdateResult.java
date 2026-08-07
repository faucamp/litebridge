package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.commons.CollectionUtils;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents the result of a composite update operation, aggregating multiple {@link DtoUpdateResult} objects
 * that represent individual update results.
 * <p>
 * This class is immutable and maintains an internal list of {@link DtoUpdateResult}, allowing for chained
 * operations while maintaining encapsulation.
 */
public final class CompositeUpdateResult {

    private final List<DtoUpdateResult> dtoUpdateResults = new ArrayList<>();
    private final Map<Object, DtoUpdateResult> resultsByDto = new IdentityHashMap<>();

    /**
     * Adds a {@link DtoUpdateResult} to the composite update result.
     *
     * @param dtoUpdateResult The {@link DtoUpdateResult} to add.
     * @return This {@link CompositeUpdateResult} instance, allowing for method chaining.
     */
    public CompositeUpdateResult add(final DtoUpdateResult dtoUpdateResult) {
        dtoUpdateResults.add(dtoUpdateResult);
        resultsByDto.put(dtoUpdateResult.getDto(), dtoUpdateResult);
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

    /**
     * Returns the {@link DtoUpdateResult} for the given DTO.
     *
     * @param dto The DTO to look up.
     * @return The {@link DtoUpdateResult} for the DTO, or null if not found.
     */
    public @Nullable DtoUpdateResult getDtoUpdateResult(final Object dto) {
        return resultsByDto.get(dto);
    }
}
