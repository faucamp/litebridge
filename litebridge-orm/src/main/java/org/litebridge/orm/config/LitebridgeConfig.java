package org.litebridge.orm.config;

import java.util.Objects;

/**
 * Runtime configuration class for managing the behaviour of Litebridge, particularly
 * the handling strategy for related DTO (Data Transfer Object) fields
 * in queries.
 * <p>
 * This class provides the ability to specify or modify the strategy used when
 * related DTOs are not explicitly included in JOIN clauses of a query.
 * <p>
 * Defaults:
 * <ul>
 *     <li>{@link #relatedDtoStrategy} set to {@link RelatedDtoStrategy#NULL_IF_NO_JOIN}
 * </ul>
 */
public final class LitebridgeConfig {

    private RelatedDtoStrategy relatedDtoStrategy = RelatedDtoStrategy.NULL_IF_NO_JOIN;

    /**
     * Retrieves the current strategy for handling related DTO (Data Transfer Object) fields
     * when they are not explicitly included in the JOIN clauses of a query.
     *
     * @return the configured {@link RelatedDtoStrategy}, which determines how related DTOs
     * are processed in the absence of corresponding JOIN clauses.
     */
    public RelatedDtoStrategy getRelatedDtoStrategy() {
        return relatedDtoStrategy;
    }

    /**
     * Sets the current strategy for handling related DTO (Data Transfer Object) fields
     * when they are not explicitly included in the JOIN clauses of a query.
     *
     * @param relatedDtoStrategy the new strategy to use for handling related DTOs
     */
    public void setRelatedDtoStrategy(final RelatedDtoStrategy relatedDtoStrategy) {
        this.relatedDtoStrategy = Objects.requireNonNull(relatedDtoStrategy, "relatedDtoStrategy must not be null");
    }
}
