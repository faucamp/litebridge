package org.litebridge.db.oracle.sql;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
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

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

/**
 * SQL generator for {@code MERGE} statements using Oracle syntax.
 */
public class OracleMergeSqlGenerator extends MergeSqlGenerator {

    /**
     * Creates a new {@code OracleMergeSqlGenerator}.
     *
     * @param columnIdentifierGenerator column identifier generator
     * @param mathOperationGenerator    math operation generator
     * @param ensureTableMetaData       function that creates/retrieves table metadata
     * @param insertSqlGenerator        SQL generator for {@code INSERT} statements
     * @param updateSqlGenerator        SQL generator for {@code UPDATE} statements
     * @param deleteSqlGenerator        SQL generator for {@code DELETE} statements
     */
    public OracleMergeSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                   final MathOperationGenerator mathOperationGenerator,
                                   final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData,
                                   final InsertSqlGenerator insertSqlGenerator,
                                   final UpdateSqlGenerator updateSqlGenerator,
                                   final DeleteSqlGenerator deleteSqlGenerator) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData, insertSqlGenerator, updateSqlGenerator, deleteSqlGenerator);
    }

    /**
     * Generates an Oracle-syntax {@code MERGE INTO} SQL statement string from the provided logical {@link Merge} object.
     *
     * @param merge              the {@link Merge} object representing the logical merge operation
     * @param connectionProvider the connection provider
     * @return the generated SQL query string
     */
    @Override
    public String generateSql(final Merge merge, final ConnectionProvider connectionProvider) {
        final StringBuilder sql = appendTable(new StringBuilder("MERGE INTO "), merge.table());
        sql.append(" USING ");

        if (merge.usingTable() != null) {
            appendTable(sql, merge.usingTable());
        }

        sql.append(" ON (");
        appendConditionsAndSubgroups(sql, merge.on(), merge, connectionProvider);
        sql.append(')');

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = merge.whenMatched();
        boolean updateMatched = false;
        boolean deleteMatched = false;

        if (whenMatchedList != null) {
            sql.append(" WHEN MATCHED THEN ");

            for (Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched : whenMatchedList) {

                if (whenMatched.operation() instanceof Merge.MergeUpdate update) {
                    if (deleteMatched) {
                        throw new IllegalArgumentException("DELETE must be the last operation in a WHEN MATCHED clause for Oracle databases");
                    }

                    appendUpdate(sql, update, whenMatched, merge, connectionProvider);
                    updateMatched = true;
                } else if (whenMatched.operation() instanceof Merge.MergeDelete) {
                    if (!updateMatched) {
                        throw new IllegalArgumentException("DELETE must follow an UPDATE in a WHEN MATCHED clause for Oracle databases");
                    }

                    appendDelete(sql, whenMatched, merge, connectionProvider);
                    deleteMatched = true;
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

        final String sqlString = sql.toString();
        final int[] permutation = computeParameterPermutation(merge);

        if (permutation.length > 0 && !isIdentityPermutation(permutation)) {
            OracleExecutionEngine.registerParameterPermutation(sqlString, permutation);
        }

        return sqlString;
    }

    /**
     * Computes the parameter permutation index mapping for an Oracle MERGE statement.
     *
     * @param merge the merge operation
     * @return an array of bind parameter indices in the order corresponding to the SQL placeholders
     */
    public int[] computeParameterPermutation(final Merge merge) {
        final List<Integer> indices = new ArrayList<>();
        collectConditionGroupIndices(merge.on(), indices);

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = merge.whenMatched();

        if (whenMatchedList != null) {
            for (final Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched : whenMatchedList) {
                if (whenMatched.operation() instanceof Merge.MergeUpdate update) {
                    for (final UpdateColumn column : update.columns()) {
                        if (column.bindValueIndex() != null) {
                            indices.add(column.bindValueIndex());
                        }
                    }

                    collectConditionGroupIndices(whenMatched.and(), indices);
                } else if (whenMatched.operation() instanceof Merge.MergeDelete) {
                    collectConditionGroupIndices(whenMatched.and(), indices);
                }
            }
        }

        final List<Merge.WhenMatched<Merge.MergeInsert>> whenNotMatchedList = merge.whenNotMatched();

        if (whenNotMatchedList != null) {
            for (final Merge.WhenMatched<Merge.MergeInsert> whenNotMatched : whenNotMatchedList) {
                collectConditionGroupIndices(whenNotMatched.and(), indices);
                final Merge.MergeInsert insert = whenNotMatched.operation();

                for (int i = 0; i < insert.rows(); i++) {
                    for (final UpdateColumn column : insert.columns()) {
                        if (column.bindValueIndex() != null) {
                            indices.add(column.bindValueIndex());
                        }
                    }
                }
            }
        }

        return indices.stream().mapToInt(Integer::intValue).toArray();
    }

    private static boolean isIdentityPermutation(final int[] permutation) {
        for (int i = 0; i < permutation.length; i++) {
            if (permutation[i] != i) {
                return false;
            }
        }
        return true;
    }

    private void appendUpdate(final StringBuilder sql,
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
    }

    private void appendDelete(final StringBuilder sql,
                              final Merge.WhenMatched<Merge.WhenMatchedOperation> whenMatched,
                              final Merge merge,
                              final ConnectionProvider connectionProvider) {
        sql.append(" DELETE WHERE ");

        if (whenMatched.and() == null) {
            throw new IllegalArgumentException("DELETE must have a WHERE clause for Oracle databases");
        }

        appendConditionsAndSubgroups(sql, whenMatched.and(), merge, connectionProvider);
    }
}
