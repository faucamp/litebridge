package org.litebridge.orm.persistence;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Column;
import org.litebridge.db.spi.ColumnMetaData;
import org.litebridge.db.spi.PreparedOperation;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.generator.SequenceColumnValueGenerator;
import org.litebridge.db.spi.query.UpdateMetaData;
import org.litebridge.db.spi.sql.BindValue;
import org.litebridge.db.spi.update.ColumnValue;
import org.litebridge.orm.api.select.ast.QueryNode;
import org.litebridge.orm.api.select.ast.SetNode;
import org.litebridge.orm.api.select.ast.WhereNode;
import org.litebridge.orm.engine.LitebridgeContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Abstract base class for building SQL statements.
 */
public abstract sealed class AbstractStatementBuilder implements StatementBuilder
        permits AbstractConditionalStatementBuilder, InsertBuilder {

    /**
     * The ORM table associated with the statement.
     */
    protected final OrmTable ormTable;
    private final StatementChain statementChain = new StatementChain();

    /**
     * The ORM context.
     */
    protected final LitebridgeContext litebridgeContext;

    /**
     * The current query node.
     */
    protected QueryNode node;

    /**
     * Constructs a new {@code AbstractStatementBuilder}.
     *
     * @param ormTable          The ORM table.
     * @param litebridgeContext The ORM context.
     */
    protected AbstractStatementBuilder(final OrmTable ormTable,
                                       final LitebridgeContext litebridgeContext) {
        this.ormTable = ormTable;
        this.litebridgeContext = litebridgeContext;
    }

    @Override
    public QueryNode node() {
        return node;
    }

    @Override
    public void addSetNode(final Column column, final @Nullable Object value, final boolean bindValue) {
        this.node = new SetNode(this.node, column, value, bindValue);
    }

    /**
     * Adds a column value to the statement.
     *
     * @param columnValue the column value to add
     */
    public void addColumn(final ColumnValue columnValue) {
        addSetNode(columnValue.column(), columnValue.value(), true);
    }

    @Override
    public StatementChain statementChain() {
        return statementChain;
    }

    @Override
    public abstract PreparedOperation build();

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
