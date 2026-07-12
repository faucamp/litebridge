package org.litebridge.orm.persistence;

import org.litebridge.commons.type.ConcurrentLazy;
import org.litebridge.db.spi.MappedFieldTarget;
import org.litebridge.tracking.FieldAccessor;

/**
 * A many-to-many relationship mapping in an object-relational mapping (ORM) context.
 * <p>
 * This record encapsulates metadata related to a join table used to establish the relationship
 * between two entities in a database.
 * <p>
 * The record provides information regarding the join table, its join columns, and the collection
 * field that represents the relationship in the application layer. It also includes mechanisms
 * to lazily resolve the target table associated with the many-to-many relationship.
 *
 * @param joinTable         The intermediary table that connects two entities in a many-to-many relationship.
 * @param joinColumn        The column in the join table that links it to the source entity.
 * @param collection        The field in the source entity that holds the collection representing the relationship.
 * @param targetTable:      The table representing the target entity in the relationship, resolved lazily.
 * @param inverseJoinColumn The column in the join table that links it to the target entity.
 */
public record MappedManyToMany(OrmTable joinTable,
                               String joinColumn,
                               FieldAccessor collection,

                               ConcurrentLazy<OrmTable> targetTable,
                               String inverseJoinColumn) implements MappedFieldTarget {
}
