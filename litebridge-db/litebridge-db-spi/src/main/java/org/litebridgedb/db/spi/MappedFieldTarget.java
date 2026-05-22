package org.litebridgedb.db.spi;

/**
 * Marker interface for a target for a mapped field in an object-relational mapping (ORM) context.
 * <p>
 * This interface is a marker for any structure that defines metadata or mappings
 * associated with a database field or relationship within an ORM framework.
 * Implementations of this interface may represent various types of mappings,
 * such as one-to-many, many-to-many, or column-to-table relationships.
 * <p>
 * Common use cases include defining the structure or mappings for:
 * - One-to-many relationships between entities.
 * - Many-to-many relationships using a join table.
 * - Database columns and inline tables.
 * <p>
 * This abstraction enables flexibility and extensibility by allowing multiple
 * forms of field mapping metadata to be handled uniformly.
 */
public interface MappedFieldTarget {
}
