package org.litebridge.dto;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Marks a DTO field as being tracked for direct JDBC operations
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface TrackedField {
    /**
     * Database column name of the DTO field for direct JDBC access
     *
     * @return Database column name of the DTO field
     */
    String dbColumnName() default "";

    /**
     * Used for Maps: Database table for the Map
     *
     * @return Database table for the Map
     */
    String mapDbTableName() default "";

    /**
     * Used for Maps: Database column name for the map's keys for direct JDBC access
     *
     * @return Database column name for the map's keys
     */
    String mapKeyDbColumnName() default "";

    /**
     * Used for Maps: Database column name for the map's values for direct JDBC access
     *
     * @return Database column name for the map's vaues
     */
    String mapValueDbColumnName() default "";
}
