package org.litebridge.orm.annotation;

import org.litebridge.db.spi.generator.ColumnValueGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Maps a Java field or property to a database column.
 * <p>
 * This may be applied to a Java field for direct field updates/reads,
 * or to a Java property method for property-based access column values.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Specifies the name of the database column that the annotated field or method
     * maps to.
     * <p>
     * This value is used for static column mapping in the Litebridge ORM.
     *
     * @return The name of the column in the database table.
     */
    String value();

    /**
     * The remote column name to which this column is joined.
     * <p>
     * Only the column name should be specified; the table is inferred from the field type.
     *
     * @return The join column name as a string. If left empty or not specified, no join condition is associated with this column.
     */
    String joinOn() default "";

    /**
     * Join using the the current column's name.
     * <p>
     * Indicates that the mapping should use the current column's name as part of a "USING" clause
     * when performing a SQL join operation.
     *
     * @return true if the "using" clause should be applied; false otherwise.
     */
    boolean joinUsing() default false;

    /**
     * Specifies a custom implementation of {@code ColumnValueGenerator} to dynamically generate or compute
     * values for the database column during runtime. This attribute allows for overriding the default
     * behavior of static column value assignment by providing a generator class that implements the logic
     * for value computation.
     *
     * @return A class that extends {@code ColumnValueGenerator}, which will be used to generate the column value.
     * If not specified, the default is {@code ColumnValueGenerator.class}, indicating no custom generator is used.
     * The class must have a no-argument constructor.
     */
    Class<? extends ColumnValueGenerator> generator() default ColumnValueGenerator.class;

    /**
     * Specifies the database sequence to be used for generating values for the annotated field or method.
     * When provided, this sequence will be used to automatically populate the column value during
     * insert operations.
     *
     * @return The name of the database sequence to use. If not specified, no sequence will be used.
     */
    String generateUsingSequence() default "";
}
