package org.litebridge.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a one-to-many relationship between two entities.
 * <p>
 * This relationship maps a collection of child entities to a parent entity.
 * <p>
 * This annotation is applied to fields or properties within entity classes to
 * configure the many-to-many mapping.
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
