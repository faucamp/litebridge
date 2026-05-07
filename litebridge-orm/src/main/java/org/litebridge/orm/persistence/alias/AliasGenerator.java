package org.litebridge.orm.persistence.alias;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.persistence.OrmTable;

public sealed interface AliasGenerator permits DefaultAliasGenerator, NoOpAliasGenerator {

    Table aliasTable(OrmTable ormTable);

    Column aliasColumn(Table table, ColumnMetaData columnMetaData);
}
