package org.litebridge.orm.persistence;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;

/**
 * A combination of a column and an inline table mapping in the context of object-relational mapping.
 *
 * @param column    The metadata of the database column.
 * @param tableSpec The inline or associated table specification.
 */
record ColumnAndInlineTable(ColumnMetaData column, OrmTable tableSpec) implements MappedFieldTarget {
}