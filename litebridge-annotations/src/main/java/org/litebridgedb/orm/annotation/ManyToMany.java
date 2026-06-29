package org.litebridgedb.orm.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation used to define a many-to-many relationship between two entities
 * in the context of Object-Relational Mapping (ORM). This relationship represents
 * an association where multiple records in one entity are related to multiple records
 * in another entity, typically via a join table.
 * <p>
 * The relationship is configured through three attributes:
 * - `joinTable`: Specifies the name of the intermediate table used to link the two entities.
 * - `joinColumn`: Defines the column in the join table that references the primary key of the current entity.
 * - `inverseJoinColumn`: Specifies the column in the join table that references the primary key of the associated entity.
 * <p>
 * This annotation is applied to fields or methods within entity classes to declare
 * and configure the many-to-many mapping. Litebridge uses this metadata to manage
 * the relationships and generate the necessary SQL for data persistence.
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
