package org.litebridge.orm.persistence;

import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.UpdateStatement;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for building SQL statements.
 *
 * @param <US> The type of update statement being built.
 */
public abstract sealed class AbstractStatementBuilder<US extends UpdateStatement> implements StatementBuilder<US>
        permits InsertBuilder, UpdateBuilder, DeleteBuilder {

    /**
     * The ORM table associated with the statement.
     */
    protected final OrmTable ormTable;
    private final StatementChain statementChain = new StatementChain();
    private final List<BindValue> bindValues = new ArrayList<>();

    /**
     * Constructs a new {@code AbstractStatementBuilder}.
     *
     * @param ormTable The ORM table.
     */
    protected AbstractStatementBuilder(final OrmTable ormTable) {
        this.ormTable = ormTable;
    }

    @Override
    public StatementChain statementChain() {
        return statementChain;
    }

    @Override
    public List<BindValue> bindValues() {
        return bindValues;
    }

    @Override
    public abstract US build();

    @Override
    public UpdateMetaData createUpdateMetaData() {
        final List<ColumnMetaData> generatedPrimaryKeyColumns = getGeneratedPrimaryKeyColumns(ormTable.getMetaData());

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
    private List<ColumnMetaData> getGeneratedPrimaryKeyColumns(final TableMetaData tableMetaData) {
        return tableMetaData.primaryKey().stream()
                .filter(columnMetadata -> columnMetadata.isAutoIncrement()
                        || (columnMetadata.getGenerator() != null && SequenceColumnValueGenerator.class.isAssignableFrom(columnMetadata.getGenerator().getClass())))
                .toList();
    }
}
