package org.litebridge.spring.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Query {

    /**
     * The native SQL query string to execute.
     */
    String value();

    /**
     * Whether the query is a mutating statement (UPDATE, DELETE, INSERT).
     */
    boolean modifying() default false;
}