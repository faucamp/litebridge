package org.litebridgedb.orm.api.register;

import org.litebridgedb.orm.api.spec.FieldSpec;
import org.litebridgedb.orm.api.spec.OneToMany;

/**
 * Builder class for defining one-to-many relationships between a field or property in a DTO (Data Transfer Object)
 * and a corresponding set of related records in a database.
 * <p>
 * This class is part of the Litebridge ORM's fluent API for registering mappings between DTOs and database tables.
 * It provides methods to specify one-to-many relationships, either by field-based or property-based access, and is
 * primarily designed for use in the context of table registration and ORM configuration.
 * <p>
 * Methods in this class return instances of {@code OneToMany}, which encapsulate the details of the one-to-many mapping.
 * A {@code OneToMany} instance includes a {@code FieldSpec}, representing the specific field or property in the DTO
 * that maps to the related database records.
 */
public class OneToManyBuilder {

    /**
     * Defines a one-to-many relationship between a specified field in a DTO (Data Transfer Object) and
     * a corresponding collection of rows in a database table.
     *
     * @param field The name of the field in the DTO that represents the one-to-many relationship. This
     *              should match the field's name in the DTO and will be used for field-level access.
     * @return An {@link OneToMany} instance encapsulating the relationship details based on the provided
     * field specification.
     */
    public OneToMany mappedByField(final String field) {
        return new OneToMany(new FieldSpec(field, false));
    }

    /**
     * Defines a one-to-many relationship based on a specified property in a DTO (Data Transfer Object).
     * This method creates a {@link OneToMany} instance that represents the mapping of a property
     * in the DTO to a collection of related records in a database table. The property access is
     * based on getter/setter conventions.
     *
     * @param property The name of the property in the DTO that represents the one-to-many relationship.
     *                 This should match the property name as defined by the getter/setter in the DTO.
     * @return An {@link OneToMany} instance encapsulating the relationship details based on the provided
     * property specification.
     */
    public OneToMany mappedByProperty(final String property) {
        return new OneToMany(new FieldSpec(property, true));
    }
}
