package org.litebridge.orm.api.merge;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.orm.engine.LitebridgeContext;
import org.litebridge.orm.persistence.TableMetaDataCache;
import org.litebridge.orm.persistence.TableRegistry;

import java.util.Objects;

abstract sealed class MergeStepBase permits MergeAndStep, MergeOnStep {

    /**
     * The target table name in SQL mode.
     */
    protected final @Nullable String targetTable;
    /**
     * The using table name in SQL mode.
     */
    protected final @Nullable String usingTable;
    /**
     * The target DTO class in DTO mode.
     */
    protected final @Nullable Class<?> targetDtoClass;
    /**
     * The using DTO class in DTO mode.
     */
    protected final @Nullable Class<?> usingDtoClass;

    /**
     * The Litebridge context.
     */
    protected final LitebridgeContext litebridgeContext;

    MergeStepBase(final String targetTable, final String usingTable, final LitebridgeContext litebridgeContext) {
        this.targetTable = targetTable;
        this.usingTable = usingTable;
        this.targetDtoClass = null;
        this.usingDtoClass = null;
        this.litebridgeContext = litebridgeContext;
    }

    MergeStepBase(final Class<?> targetDtoClass, final Class<?> usingDtoClass, final LitebridgeContext litebridgeContext) {
        this.targetDtoClass = targetDtoClass;
        this.usingDtoClass = usingDtoClass;
        this.targetTable = null;
        this.usingTable = null;
        this.litebridgeContext = litebridgeContext;
    }

    /**
     * Resolves a column name to an SPI {@link Column} by checking target and using tables.
     *
     * @param column the column name to resolve
     * @return the resolved SPI column
     */
    protected final Column createSpiColumn(final String column) {
        final TableRegistry tableRegistry = litebridgeContext.tableRegistry();
        final Table targetTable = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(this.targetTable));
        final Table usingTable = tableRegistry.getOrCreateSpiTable(Objects.requireNonNull(this.usingTable));

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
