package org.litebridge.orm.engine;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;

import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

abstract sealed class AbstractInsertEngine extends AbstractUpdateEngine permits InsertEngine, MergeEngine {

    protected static UpdateMetaData createUpdateMetaData(final Supplier<Table> tableSupplier, final LitebridgeContext litebridgeContext) {
        final Table table = tableSupplier.get();
        final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
        final List<ColumnMetaData> generatedPrimaryKeyColumns = getGeneratedPrimaryKeyColumns(tableMetaData);

        if (generatedPrimaryKeyColumns.isEmpty()) {
            return new UpdateMetaData(false, Collections.emptyList(), new String[0]);
        }

        final String[] generatedPkColumnNames = generatedPrimaryKeyColumns.stream()
                .map(ColumnMetaData::name)
                .toArray(String[]::new);

        return new UpdateMetaData(true, generatedPrimaryKeyColumns, generatedPkColumnNames);
    }

    /**
     * Get the primary key columns for which the database generates values.
     *
     * @param tableMetaData the {@link TableMetaData} object containing the metadata of the target table
     * @return a list of {@link ColumnMetaData} objects representing the generated primary key columns
     */
    protected static List<ColumnMetaData> getGeneratedPrimaryKeyColumns(final TableMetaData tableMetaData) {
        return tableMetaData.primaryKey().stream()
                .filter(columnMetadata -> columnMetadata.isAutoIncrement()
                        || (columnMetadata.getGenerator() != null && SequenceColumnValueGenerator.class.isAssignableFrom(columnMetadata.getGenerator().getClass())))
                .toList();
    }
}
