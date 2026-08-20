package org.litebridge.db.spi.impl.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.convert.TypeConverter;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.InsertV2;
import org.litebridge.db.spi.update.Merge;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

public class MergeSqlGenerator extends AbstractSqlGenerator {

    private final InsertSqlGenerator insertSqlGenerator;
    private final UpdateSqlGenerator updateSqlGenerator;
    private final DeleteSqlGenerator deleteSqlGenerator;

    public MergeSqlGenerator(final TypeConverter typeConverter,
                             final ColumnIdentifierGenerator columnIdentifierGenerator,
                             final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData,
                             final InsertSqlGenerator insertSqlGenerator,
                             final UpdateSqlGenerator updateSqlGenerator,
                             final DeleteSqlGenerator deleteSqlGenerator) {
        super(typeConverter, columnIdentifierGenerator, ensureTableMetaData);
        this.insertSqlGenerator = insertSqlGenerator;
        this.updateSqlGenerator = updateSqlGenerator;
        this.deleteSqlGenerator = deleteSqlGenerator;
    }

    public String prepareSql(final Merge merge, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("MERGE INTO "), merge.table());
        sql.append(" USING (");

        if (merge.usingTable() != null) {
            appendTable(sql, merge.usingTable());
        }

        sql.append(") ON ");
        appendConditionsAndSubgroups(sql, merge.on(), merge, connectionProvider);

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = merge.whenMatched();

        if (whenMatchedList != null) {
            for (Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched : whenMatchedList) {
                sql.append(" WHEN MATCHED");

                if (whenMatched.and() != null) {
                    sql.append(" AND ");
                    appendConditionsAndSubgroups(sql, whenMatched.and(), merge, connectionProvider);
                }

                sql.append(" THEN ");

                if (whenMatched.operation() instanceof Merge.MergeUpdate update) {
                    appendUpdate(sql, update);
                } else if (whenMatched.operation() instanceof Merge.MergeDelete) {
                    sql.append("DELETE");
                } else {
                    throw new IllegalArgumentException("Unsupported operation type: " + whenMatched.operation().getClass().getName());
                }
            }
        }

        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatchedList = merge.whenNotMatched();

        if (whenNotMatchedList != null) {
            for (Merge.WhenMatched<Merge.MergeInsert> whenNotMatched : whenNotMatchedList) {
                sql.append(" WHEN NOT MATCHED THEN ");
                appendInsert(sql, whenNotMatched.operation());
            }
        }

        return sql.toString();
    }

    protected String appendUpdate(final StringBuilder sql, final Merge.MergeUpdate update) {
        sql.append("UPDATE SET ");

        boolean first = true;

        for (InsertV2.InsertColumn updateColumn : update.columns()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(columnIdentifierGenerator.quoteIdentifier(updateColumn.name()));
            sql.append(" = ");
            sql.append(getColumnValue(updateColumn));
        }

        return sql.toString();
    }

    private static String getColumnValue(final InsertV2.InsertColumn updateColumn) {
        if (updateColumn.generatedValue() != null) {
            return Objects.requireNonNull(updateColumn.generatedValue()).toString();
        } else {
            return "?";
        }
    }

    protected void appendInsert(final StringBuilder sql, final Merge.MergeInsert insert) {
        final List<String> columnNames = insert.columns().stream().map(InsertV2.InsertColumn::name).toList();
        sql.append("INSERT (")
                .append(String.join(", ", columnNames.stream().map(columnIdentifierGenerator::quoteIdentifier).toList()))
                .append(") VALUES ");

        for (int i = 0; i < insert.rows(); i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append('(');

            for (int j = 0; j < insert.columns().size(); j++) {
                final InsertV2.InsertColumn insertColumn = insert.columns().get(j);

                if (j > 0) {
                    sql.append(", ");
                }

                sql.append(getColumnValue(insertColumn));
            }

            sql.append(')');
        }
    }
}
