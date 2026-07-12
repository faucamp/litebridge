package org.litebridgedb.orm.persistence.alias;

import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.ColumnMetaData;
import org.litebridgedb.db.spi.Table;
import org.litebridgedb.orm.persistence.OrmTable;

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
