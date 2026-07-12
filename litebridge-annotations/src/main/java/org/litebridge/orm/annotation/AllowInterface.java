package org.litebridge.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies additional superinterfaces of the entity class that will be recognised by Litebridge relational mapping
 * if used in collections.
 * <p>
 * For example, if the DTO class is defined as {@code class MyDto implements MyInterface},
 * then {@code MyInterface.class} should be specified via this annotation to ensure that Litebridge can correctly handle
 * collections of {@code MyInterface} instances in related entities.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface AllowInterface {

    /**
     * Specifies additional superinterfaces of the entity class that will be
     * recognised by Litebridge relational mapping if used in collections.
     *
     * @return An array of {@code Class} objects representing the additional
     * superinterfaces of the entity class to be recognised.
     */
    Class<?>[] value();
}
