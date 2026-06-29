package org.litebridgedb.orm.api.spec;

/**
 * Represents a one-to-many relationship mapping between a field in a DTO and a corresponding
 * collection of rows in a database table.
 * <p>
 * This class is used within the context of the ORM to define a mapping where a single field in
 * a DTO is associated with multiple related entries in another table. The field is specified
 * using a {@link FieldSpec} which holds the name and access characteristics of the field.
 * <p>
 * The `OneToMany` relationship is useful, for example, to model scenarios such as:
 * - A single entity having multiple child entities (e.g., a customer with many orders).
 * - One-to-many hierarchical relationships between database rows.
 * <p>
 * Implements the {@link ColumnMapping} interface to allow its usage within column mapping
 * operations.
 *
 * @param mappedByField The {@link FieldSpec} defining the field in the DTO that refers
 *                      to the related collection of database rows.
 */
public record OneToMany(FieldSpec mappedByField) implements ColumnMapping {
}
