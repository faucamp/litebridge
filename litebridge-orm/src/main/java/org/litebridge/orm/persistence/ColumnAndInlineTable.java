package org.litebridge.orm.persistence;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.MappedFieldTarget;

record ColumnAndInlineTable(ColumnMetaData column, OrmTable tableSpec) implements MappedFieldTarget {
}