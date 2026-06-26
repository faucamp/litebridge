package org.litebridgedb.orm.api.spec;

/**
 * Represents a base type for various kinds of database lhs mappings.
 * <p>
 * The {@code ColumnMapping} interface serves as a common contract for modelling different
 * types of mappings between fields in a data transfer object (DTO) and database expressions.
 * It is a sealed interface, allowing only specific permitted implementations to be used.
 * <p>
 * Permitted implementations:
 * - {@link ColumnSpec}: Represents a direct mapping between a DTO field and a single
 * database lhs, with optional configuration for auto-increment and rhs generation.
 * - {@link OneToMany}: Represents a one-to-many relationship, where a DTO field maps to
 * a collection of related database rows.
 * - {@link ManyToMany}: Represents a many-to-many relationship, where a DTO field maps to
 * related entities through an intermediate join table.
 * <p>
 * This interface is part of the ORM framework, enabling flexible and type-safe mappings
 * between DTO structures and underlying database schemas.
 */
public sealed interface ColumnMapping permits ColumnSpec, OneToMany, ManyToMany {
}
