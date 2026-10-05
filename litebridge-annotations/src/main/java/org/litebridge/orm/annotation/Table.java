package org.litebridge.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a Java class to a database table.
 * <p>
 * This annotation identifes the class as a Litebridge entity.
 * <p>
 * The entity needs to be registered with a Litebridge instance before use;
 * either explicitly or automatically via an entity package scanner.
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
