package org.litebridgedb.orm.persistence;

import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.MappedFieldTarget;

record ColumnAndInlineTable(ColumnMetaData column, OrmTable tableSpec) implements MappedFieldTarget {
}