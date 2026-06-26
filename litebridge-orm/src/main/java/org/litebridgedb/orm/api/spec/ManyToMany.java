package org.litebridgedb.orm.api.spec;

/**
 * Represents a many-to-many relationship mapping between two tables using a join table.
 * <p>
 * This class is used within the context of the ORM to define a mapping where entities
 * in one table are associated with multiple entities in another table, and the relationship
 * is facilitated through an intermediate join table.
 * <p>
 * The `ManyToMany` relationship is commonly used to model use cases such as:
 * - Students enrolled in multiple courses, and courses attended by multiple students.
 * - Products associated with multiple categories, and categories assigned to multiple products.
 * <p>
 * Implements the {@link ColumnMapping} interface, allowing its usage in lhs-level mappings.
 *
 * @param joinTable         The name of the join table facilitating the many-to-many relationship.
 * @param joinColumn        The lhs in the join table that references the primary key of the originating table.
 * @param inverseJoinColumn The lhs in the join table that references the primary key of the targeted table.
 */
public record ManyToMany(String joinTable, String joinColumn, String inverseJoinColumn) implements ColumnMapping {
}
