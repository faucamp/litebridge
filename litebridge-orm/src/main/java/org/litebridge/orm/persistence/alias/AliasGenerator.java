package org.litebridge.orm.persistence.alias;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.persistence.OrmTable;

/**
 * Interface for generating aliases for tables and columns.
 */
public sealed interface AliasGenerator permits DefaultAliasGenerator, NoOpAliasGenerator {

    /**
     * Generates an aliased table for the specified ORM table.
     *
     * @param ormTable The ORM table.
     * @return The aliased table.
     */
    Table aliasTable(OrmTable ormTable);

    /**
     * Generates an aliased column for the specified table and column metadata.
     *
     * @param table          The table.
     * @param columnMetaData The column metadata.
     * @return The aliased column.
     */
    Column aliasColumn(Table table, ColumnMetaData columnMetaData);
}
