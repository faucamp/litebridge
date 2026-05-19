package org.litebridgedb.db.spi.generator;

import org.litebridgedb.db.spi.ColumnMetaData;

@FunctionalInterface
public interface ColumnValueGenerator {

    Object generate(ColumnMetaData columnMetaData);

}
