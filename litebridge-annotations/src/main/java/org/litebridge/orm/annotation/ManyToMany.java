package org.litebridge.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a many-to-many relationship between two entities.
 * <p>
 * This annotation is applied to fields or properties within entity classes to
 * configure the many-to-many mapping.
 */
@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ManyToMany {

    /**
     * Specifies the name of the intermediate join table used to represent
     * a many-to-many relationship between two entities in the context of
     * Object-Relational Mapping (ORM). The join table is used to store the
     * associations between records of the current entity and the associated entity.
     *
     * @return The name of the join table used in the many-to-many relationship.
     */
    String joinTable();

    /**
     * Specifies the name of the column in the join table that references the primary key
     * of the current entity. This column is used to establish the link between the current
     * entity and the associated entity in the many-to-many relationship.
     *
     * @return The name of the join column used in the many-to-many relationship.
     */
    String joinColumn();

    /**
     * Specifies the name of the column in the join table that references the primary key
     * of the associated entity. This column is used to establish the link between the associated
     * entity and the current entity in the many-to-many relationship.
     *
     * @return The name of the inverse join column used in the many-to-many relationship.
     */
    String inverseJoinColumn();
}
