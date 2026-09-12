package org.litebridge.orm.engine;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.update.Insert;

import java.util.List;
import java.util.function.Supplier;

/**
 * Provides common functionality for creating update metadata.
 */
public non-sealed abstract class AbstractInsertEngine extends AbstractUpdateEngine {

    /**
     * Creates an instance of {@link UpdateMetaData} based on the provided operation details, table supplier,
     * and context.
     * <p>
     * This method focuses on handling insert operations and determines whether generated keys
     * should be included in the metadata.
     *
     * @param preparedOperation the prepared database operation containing the structured operation
     *                          and associated bind values
     * @param tableSupplier     a supplier that provides the current {@link Table} instance
     * @param litebridgeContext the context containing metadata cache and database-related utilities
     * @return an {@link UpdateMetaData} instance with the details of the update operation, or {@code null}
     * if the provided operation is not an insert
     */
    public static @Nullable UpdateMetaData createUpdateMetaData(final PreparedOperation preparedOperation,
                                                                final Supplier<Table> tableSupplier,
                                                                final LitebridgeContext litebridgeContext) {
        final int rows;
        final int bindValueColumns;

        if (preparedOperation.operation() instanceof Insert insert) {
            rows = insert.rows();
            bindValueColumns = insert.columns().size();

            if (!insert.returnGeneratedKeys()) {
                return new UpdateMetaData(false, null, null, rows, bindValueColumns);
            }

            final Table table = tableSupplier.get();
            final TableMetaData tableMetaData = litebridgeContext.tableMetaDataCache().ensureTableMetaData(table);
            final List<ColumnMetaData> generatedPrimaryKeyColumns = getGeneratedPrimaryKeyColumns(tableMetaData);

            if (generatedPrimaryKeyColumns.isEmpty()) {
                return new UpdateMetaData(false, null, null, rows, bindValueColumns);
            }

            final String[] generatedPkColumnNames = generatedPrimaryKeyColumns.stream()
                    .map(ColumnMetaData::name)
                    .toArray(String[]::new);

            return new UpdateMetaData(true, generatedPrimaryKeyColumns, generatedPkColumnNames, rows, bindValueColumns - generatedPkColumnNames.length);
        } else {
            return null;
        }
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
