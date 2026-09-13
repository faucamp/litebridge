package org.litebridge.db.oracle.sql;

import org.litebridge.commons.CollectionUtils;
import org.litebridge.db.oracle.engine.OracleExecutionEngine;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.impl.sql.MathOperationGenerator;
import org.litebridge.db.spi.impl.sql.MergeSqlGenerator;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
     */
    public OracleMergeSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                                   final MathOperationGenerator mathOperationGenerator,
                                   final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
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
        final Table targetTable = merge.table();
        final StringBuilder sql = appendTable(new StringBuilder("MERGE INTO "), targetTable);

        if (targetTable.alias() != null) {
            sql.append(' ').append(columnIdentifierGenerator.createAliasDeclaration(Objects.requireNonNull(merge.table().alias())));
        }

        sql.append(" USING ");
        final Table usingTable = merge.usingTable();

        if (usingTable != null) {
            appendTable(sql, usingTable);

            if (usingTable.alias() != null) {
                sql.append(' ').append(columnIdentifierGenerator.createAliasDeclaration(Objects.requireNonNull(usingTable.alias())));
            }
        }

        sql.append(" ON (");
        appendConditionsAndSubgroups(sql, merge.on(), merge, connectionProvider);
        sql.append(')');

        final List<Merge.WhenMatched<Merge.WhenMatchedOperation>> whenMatchedList = merge.whenMatched();
        boolean updateMatched = false;
        boolean deleteMatched = false;

        if (!CollectionUtils.isEmpty(whenMatchedList)) {
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

        if (!CollectionUtils.isEmpty(whenNotMatchedList)) {
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
                if (whenMatched.operation() instanceof Merge.MergeUpdate(List<UpdateColumn> columns)) {
                    for (final UpdateColumn column : columns) {
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
