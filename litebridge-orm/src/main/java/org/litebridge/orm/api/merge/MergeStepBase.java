package org.litebridge.orm.api.merge;

import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.TableMetaDataCache;

abstract sealed class MergeStepBase permits MergeAndStep, MergeOnStep {

    protected final Table targetTable;
    protected final Table usingTable;
    protected final LitebridgeContext litebridgeContext;

    MergeStepBase(final Table targetTable, final Table usingTable, final LitebridgeContext litebridgeContext) {
        this.targetTable = targetTable;
        this.usingTable = usingTable;
        this.litebridgeContext = litebridgeContext;
    }

    protected final Column createSpiColumn(final String column) {
        final TableMetaDataCache tableMetaDataCache = litebridgeContext.tableMetaDataCache();
        final TableMetaData targetTableMetaData = tableMetaDataCache.ensureTableMetaData(targetTable);
        final TableMetaData usingTableMetaData = tableMetaDataCache.ensureTableMetaData(usingTable);
        final boolean targetTableColumn = targetTableMetaData.hasColumn(column);
        final boolean usingTableColumn = usingTableMetaData.hasColumn(column);
        final Column spiColumn;

        if (targetTableColumn && usingTableColumn) {
            throw new IllegalArgumentException("Column '" + column + "' is present in both INTO and USING tables; specify table explicitly");
        } else if (targetTableColumn) {
            spiColumn = targetTableMetaData.column(column).toColumn();
        } else {
            spiColumn = usingTableMetaData.column(column).toColumn();
        }

        return spiColumn;
    }
}
