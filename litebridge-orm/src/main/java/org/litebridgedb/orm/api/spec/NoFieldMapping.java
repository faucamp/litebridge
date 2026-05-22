package org.litebridgedb.orm.api.spec;

/**
 * Utility class representing a field mapping that does not map to any database field.
 * <p>
 * This is used internally by the ORM when traversing many-to-many JOIN tables.
 */
public final class NoFieldMapping implements FieldMapping {
}
