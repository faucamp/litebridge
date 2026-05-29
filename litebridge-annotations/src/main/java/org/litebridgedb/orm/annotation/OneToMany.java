package org.litebridgedb.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that establishes a one-to-many relationship between two entities in the context of
 * Object-Relational Mapping (ORM). This relationship maps a collection of child entities to a parent entity.
 * <p>
 * The `mappedByField` attribute specifies the field in the child entity that owns the relationship.
 * It is essential to correctly define this attribute to maintain bidirectional associations
 * between parent and child entities.
 * <p>
 * Attributes:
 * - `mappedByField`: Declares the name of the field in the child entity that acts as the owner of the relationship.
 * <p>
 * This annotation is primarily applied to fields or methods in an entity class to indicate
 * the one-to-many association. Litebridge leverages this metadata to manage cascading operations and other relational
 * operations.
 * <p>
 * Example Use Case:
 * - A `Department` entity can have a one-to-many relationship with `Employee` entities,
 * where the `mappedByField` specifies the field in the `Employee` entity referencing the `Department`.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface OneToMany {

    /**
     * Specifies the name of the field in the child entity that owns the relationship
     * in a bidirectional association. This attribute is typically used in the context
     * of Object-Relational Mapping (ORM) to establish a one-to-many relationship.
     *
     * @return The name of the field in the child entity that represents the owning side
     * of the relationship.
     */
    String mappedByField();
}
