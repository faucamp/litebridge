package org.litebridge.orm.api.spec;

/**
 * Represents a base type for various kinds of database column mappings.
 * <p>
 * The {@code ColumnMapping} interface serves as a common contract for modelling different
 * types of mappings between fields in a data transfer object (DTO) and database expressions.
 * It is a sealed interface, allowing only specific permitted implementations to be used.
 * <p>
 * Permitted implementations:
 * <ul>
 *   <li>{@link ColumnSpec}: Direct mapping between a DTO field and a single
 *      database column, with optional configuration for auto-increment and value generation.</li>
 *   <li>{@link OneToMany}: One-to-many relationship, where a DTO field maps to
 *      a collection of related database rows</li>
 *   <li>{@link ManyToMany}: Many-to-many relationship, where a DTO field maps to
 *      related entities through an intermediate join table.</li>
 *   <li>{@link MultiColumnSpec}: Multiple columns mapping to a single field, such as a related DTO
 *      via a composite foreign key.</li>
 * </ul>
 */
public sealed interface ColumnMapping permits ColumnSpec, ManyToMany, MultiColumnSpec, OneToMany {
}
