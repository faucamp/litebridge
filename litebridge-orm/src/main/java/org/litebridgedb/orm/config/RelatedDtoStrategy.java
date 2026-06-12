package org.litebridgedb.orm.config;

/**
 * Defines how related DTOs should be handled when not included as a JOIN in a query.
 */
public enum RelatedDtoStrategy {

    /**
     * Fields containing related DTOs will be set to null if not added to the JOIN clauses of a query.
     * <p>
     * This is the default behaviour.
     * <p>
     * For example, given:
     * <code>
     * class MyDto {
     * private MyRelatedDto relatedDto;
     * }
     * </code>
     * <p>
     * The related DTO field {@code relatedDto} will be set to null if not added to the JOIN clauses of a query.
     */
    NULL_IF_NO_JOIN,

    /**
     * Fields containing related DTOs will be partially constructed, containing only the primary key fields,
     * if not added to the JOIN clauses of a query.
     * <p>
     * For example, given:
     * <code>
     * class MyDto {
     * private MyRelatedDto relatedDto;
     * }
     * </code>
     * <code>
     * class MyRelatedDto {
     * // Primary key
     * private Long id;
     * // Other fields
     * private String description;
     * }
     * </code>
     * <p>
     * The related DTO field {@code relatedDto} will contain an instance of {@code MyRelatedDto} with only the primary
     * key field {@code id} set if the JOIN clause doesn't include the @{code relatedDto} field.
     */
    PARTIAL_OBJECT_IF_NO_JOIN;
}
