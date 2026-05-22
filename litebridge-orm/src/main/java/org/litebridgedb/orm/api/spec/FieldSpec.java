package org.litebridgedb.orm.api.spec;

/**
 * Represents a specification for mapping a field in a class to a database column or property.
 * <p>
 * This class is part of the ORM specification for defining how fields or properties in a DTO
 * (Data Transfer Object) are mapped to their corresponding database representations.
 * <p>
 * A {@code FieldSpec} can represent a field-level or property-level access based on the
 * {@code property} flag. Field-level access considers the field name directly for mapping,
 * whereas property-level access assumes usage of getter/setter conventions.
 *
 * @param name     The name of the field or property to be mapped.
 * @param property Indicates whether property-level access is enabled. {@code true} signifies
 *                 property-based access (via getter/setter), and {@code false} signifies direct field access.
 */
public record FieldSpec(String name, boolean property) implements FieldMapping {
}
