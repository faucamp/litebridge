package org.litebridge.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that designates a class as corresponding to a database table (i.e. an entity).
 * <p>
 * It is used in the context of Object-Relational Mapping (ORM) to specify
 * the table name in the database to which the annotated class maps.
 * <p>
 * Attributes:
 * - `value`: Represents the name of the database table that the entity class maps to.
 * <p>
 * Usage of this annotation allows ORM frameworks to identify and bind
 * the annotated class to the specific table for data persistence operations.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {

    /**
     * Specifies the name of the database table that the annotated class maps to.
     * <p>
     * This value is used in Object-Relational Mapping (ORM) to bind the entity class
     * to a specific table for data persistence operations.
     *
     * @return The name of the database table.
     */
    String value();
}
