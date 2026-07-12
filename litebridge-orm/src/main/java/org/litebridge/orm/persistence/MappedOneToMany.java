package org.litebridge.orm.persistence;

import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

/**
 * Represents a one-to-many relationship mapping in an object-relational mapping (ORM) context.
 * <p>
 * This record encapsulates metadata related to a one-to-many relationship between entities.
 * Specifically, it includes information about the field on the "one" side of the relationship
 * (mapped by) and the collection field on the "many" side.
 * <p>
 * Implements {@link MappedFieldTarget}, serving as a target for mapped fields in an ORM context.
 *
 * @param mappedByField Refers to the field accessor for the field on the "one" side
 *                      that maps to the "many" side of the relationship.
 * @param collection    Refers to the field accessor for the collection on the "many" side
 *                      that represents the related entities.
 */
public record MappedOneToMany(FieldAccessor mappedByField, FieldAccessor collection) implements MappedFieldTarget {

}
