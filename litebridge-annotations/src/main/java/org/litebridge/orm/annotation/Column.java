package org.litebridge.orm.annotation;

import org.litebridge.db.spi.generator.ColumnValueGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define mapping between a Java field or method and a database column.
 * It provides metadata for ORM frameworks to map the annotated entity field or property
 * to the corresponding column in the database table.
 * <p>
 * This annotation can be applied to fields or methods in an entity class.
 * <p>
 * Attributes:
 * - `value`: Specifies the name of the column this field or method maps to in the database.
 * - `joinOn`: Indicates the condition used when performing a join operation on another table.
 * - `joinUsing`: Specifies whether the join should use the field's value as part of a "using" clause.
 * - `generator`: References a custom {@code ColumnValueGenerator} implementation to dynamically compute or fetch the column value.
 * - `generateUsingSequence`: Specifies the name of a database sequence to use for generating the column value.
 * <p>
 * Example Use Cases:
 * - Static column mapping using the `value` attribute for straightforward entity-table mapping.
 * - Dynamic column value generation through the `generator` attribute.
 * - Sequence-based value generation for primary key fields using the `generateUsingSequence` attribute.
 * <p>
 * Note:
 * - The `ColumnValueGenerator` referenced in the `generator` attribute provides a functional interface
 * for computing the column's value at runtime using metadata information.
 * - A blank or default value for an attribute signals that the feature is not applicable or disabled.
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
