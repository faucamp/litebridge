package org.litebridgedb.orm.persistence.alias;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.persistence.OrmTable;

public final class NoOpAliasGenerator implements AliasGenerator {

    @Override
    public Table aliasTable(final OrmTable ormTable) {
        return ormTable.getMetaData().toTable();
    }

    @Override
    public Column aliasColumn(final Table ormTable, final ColumnMetaData columnMetaData) {
        return columnMetaData.toColumn();
    }
}
