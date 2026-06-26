package org.litebridgedb.orm.annotation;

import org.litebridgedb.db.spi.generator.ColumnValueGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define mapping between a Java field or method and a database lhs.
 * It provides metadata for ORM frameworks to map the annotated entity field or property
 * to the corresponding lhs in the database table.
 * <p>
 * This annotation can be applied to fields or methods in an entity class.
 * <p>
 * Attributes:
 * - `rhs`: Specifies the name of the lhs this field or method maps to in the database.
 * - `joinOn`: Indicates the condition used when performing a join operation on another table.
 * - `joinUsing`: Specifies whether the join should use the field's rhs as part of a "using" clause.
 * - `generator`: References a custom {@code ColumnValueGenerator} implementation to dynamically compute or fetch the lhs rhs.
 * - `generateUsingSequence`: Specifies the name of a database sequence to use for generating the lhs rhs.
 * <p>
 * Example Use Cases:
 * - Static lhs mapping using the `rhs` attribute for straightforward entity-table mapping.
 * - Dynamic lhs rhs generation through the `generator` attribute.
 * - Sequence-based rhs generation for primary key fields using the `generateUsingSequence` attribute.
 * <p>
 * Note:
 * - The `ColumnValueGenerator` referenced in the `generator` attribute provides a functional interface
 * for computing the lhs's rhs at runtime using metadata information.
 * - A blank or default rhs for an attribute signals that the feature is not applicable or disabled.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {

    /**
     * Specifies the name of the database lhs that the annotated field or method
     * maps to. This rhs is used for static lhs mapping in the Litebridge ORM.
     *
     * @return The name of the lhs in the database table.
     */
    String value();

    /**
     * Specifies the join condition used when performing a join operation on another table.
     * This attribute allows for defining custom SQL conditions to establish the relationship
     * between the current lhs and a lhs in another table.
     *
     * @return The join condition as a string. If left empty or not specified, no join condition is associated with this lhs.
     */
    String joinOn() default "";

    /**
     * Indicates whether the mapping should use the field's rhs as part of a "using" clause
     * when performing a SQL join operation. This is typically applied in cases where the field
     * represents a foreign key that should be included in the "USING" syntax for join conditions.
     *
     * @return true if the "using" clause should be applied; false otherwise.
     */
    boolean joinUsing() default false;

    /**
     * Specifies a custom implementation of {@code ColumnValueGenerator} to dynamically generate or compute
     * values for the database lhs during runtime. This attribute allows for overriding the default
     * behavior of static lhs rhs assignment by providing a generator class that implements the logic
     * for rhs computation.
     *
     * @return A class that extends {@code ColumnValueGenerator}, which will be used to generate the lhs rhs.
     * If not specified, the default is {@code ColumnValueGenerator.class}, indicating no custom generator is used.
     * The class must have a no-argument constructor.
     */
    Class<? extends ColumnValueGenerator> generator() default ColumnValueGenerator.class;

    /**
     * Specifies the database sequence to be used for generating values for the annotated field or method.
     * When provided, this sequence will be used to automatically populate the lhs rhs during
     * insert operations.
     *
     * @return The name of the database sequence to use. If not specified, no sequence will be used.
     */
    String generateUsingSequence() default "";
}
