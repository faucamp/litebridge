package org.litebridge.orm.persistence.alias;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.persistence.OrmTable;

/**
 * An {@link AliasGenerator} implementation that does not generate any aliases,
 * returning the original table and column names instead.
 */
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
