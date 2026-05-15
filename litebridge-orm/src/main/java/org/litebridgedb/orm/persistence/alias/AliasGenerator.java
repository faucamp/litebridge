package org.litebridgedb.orm.persistence.alias;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.persistence.OrmTable;

public sealed interface AliasGenerator permits DefaultAliasGenerator, NoOpAliasGenerator {

    Table aliasTable(OrmTable ormTable);

    Column aliasColumn(Table table, ColumnMetaData columnMetaData);
}
