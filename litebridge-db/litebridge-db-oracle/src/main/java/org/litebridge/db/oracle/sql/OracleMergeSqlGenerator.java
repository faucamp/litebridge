package org.litebridge.db.oracle.sql;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.DeleteSqlGenerator;
import org.litebridge.db.spi.impl.sql.InsertSqlGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.MergeSqlGenerator;
import org.litebridge.db.spi.impl.sql.UpdateSqlGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;
import java.util.function.BiFunction;

public class OracleMergeSqlGenerator extends MergeSqlGenerator {

    public OracleMergeSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                   final MathOperationGenerator mathOperationGenerator,
                                   final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData,
                                   final InsertSqlGenerator insertSqlGenerator,
                                   final UpdateSqlGenerator updateSqlGenerator,
                                   final DeleteSqlGenerator deleteSqlGenerator) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData, insertSqlGenerator, updateSqlGenerator, deleteSqlGenerator);
    }

    @Override
    public String prepareSql(final Merge merge, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("MERGE INTO "), merge.table());
        sql.append(" USING ");

        if (merge.usingTable() != null) {
            appendTable(sql, merge.usingTable());
        }

        sql.append(" ON (");
        appendConditionsAndSubgroups(sql, merge.on(), merge, connectionProvider);
        sql.append(')');

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = merge.whenMatched();

        if (whenMatchedList != null) {
            sql.append(" WHEN MATCHED THEN ");

            for (Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched : whenMatchedList) {

                if (whenMatched.operation() instanceof Merge.MergeUpdate update) {
                    appendUpdate(sql, update, whenMatched, merge, connectionProvider);
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

    private String appendUpdate(final StringBuilder sql,
                                final Merge.MergeUpdate update,
                                final Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched,
                                final Merge merge,
                                final ConnectionProvider connectionProvider) {
        sql.append("UPDATE SET ");

        boolean first = true;

        for (UpdateColumn updateColumn : update.columns()) {
            if (first) {
                first = false;
            } else {
                sql.append(", ");
            }

            sql.append(columnIdentifierGenerator.quoteIdentifier(updateColumn.name()));
            sql.append(" = ");
            sql.append(getColumnValueFragment(updateColumn));
        }

        if (whenMatched.and() != null) {
            sql.append(" WHERE ");
            appendConditionsAndSubgroups(sql, whenMatched.and(), merge, connectionProvider);
        }

        return sql.toString();
    }
}
