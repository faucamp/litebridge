package org.litebridge.orm.config;

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
 *
 * @param relatedDtoStrategy the strategy used when related DTOs are not explicitly included in JOIN clauses
 */
public record LitebridgeConfig(RelatedDtoStrategy relatedDtoStrategy) {

    public LitebridgeConfig() {
        this(RelatedDtoStrategy.NULL_IF_NO_JOIN);
    }
}
