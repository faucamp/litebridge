package org.litebridge.db.spi.impl.sql;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.TableMetaData;
import org.litebridge.db.spi.expression.BindValueExpression;
import org.litebridge.db.spi.impl.ColumnIdentifierGenerator;
import org.litebridge.db.spi.query.Condition;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.LogicCondition;
import org.litebridge.db.spi.query.LogicConditionGroup;
import org.litebridge.db.spi.tx.ConnectionProvider;
import org.litebridge.db.spi.update.Merge;
import org.litebridge.db.spi.update.UpdateColumn;

import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;

/**
 * SQL generator for {@code MERGE} statements.
 */
public class MergeSqlGenerator extends AbstractSqlGenerator {

    /**
     * Creates a new {@code MergeSqlGenerator}.
     *
     * @param columnIdentifierGenerator column identifier generator
     * @param mathOperationGenerator    math operation generator
     * @param ensureTableMetaData       function that creates/retrieves table metadata
     */
    public MergeSqlGenerator(final ColumnIdentifierGenerator columnIdentifierGenerator,
                             final MathOperationGenerator mathOperationGenerator,
                             final BiFunction<Table, ConnectionProvider, TableMetaData> ensureTableMetaData) {
        super(columnIdentifierGenerator, mathOperationGenerator, ensureTableMetaData);
    }

    /**
     * Generates a SQL {@code MERGE INTO} statement string from the provided logical {@link Merge} object.
     *
     * @param merge              the {@link Merge} object representing the logical merge operation
     * @param connectionProvider the connection provider
     * @return the generated SQL statement string
     */
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

        return sql.toString();
    }

    protected void appendInsert(final StringBuilder sql, final Merge.MergeInsert insert) {
        final List<String> columnNames = insert.columns().stream().map(UpdateColumn::name).toList();
        sql.append("INSERT (")
                .append(String.join(", ", columnNames.stream().map(columnIdentifierGenerator::quoteIdentifier).toList()))
                .append(") VALUES ");

        for (int i = 0; i < insert.rows(); i++) {
            if (i > 0) {
                sql.append(", ");
            }

            sql.append('(');

            for (int j = 0; j < insert.columns().size(); j++) {
                final UpdateColumn insertColumn = insert.columns().get(j);

                if (j > 0) {
                    sql.append(", ");
                }

                sql.append(getColumnValueFragment(insertColumn));
            }

            sql.append(')');
        }
    }

    /**
     * Collects parameter bind indices from the given condition group in traversal order.
     *
     * @param conditionGroup   the condition group
     * @param parameterIndices the list to collect parameter indices into
     */
    protected void collectConditionGroupIndices(final @Nullable ConditionGroup conditionGroup, final List<Integer> parameterIndices) {
        if (conditionGroup == null) {
            return;
        }

        for (final LogicCondition logicCondition : conditionGroup.conditions()) {
            final Condition condition = logicCondition.condition();
            if (condition.rhs() instanceof BindValueExpression bve) {
                for (int i = 0; i < bve.size(); i++) {
                    parameterIndices.add(bve.index() + i);
                }
            }
        }

        for (final LogicConditionGroup logicConditionGroup : conditionGroup.subgroups()) {
            collectConditionGroupIndices(logicConditionGroup.conditionGroup(), parameterIndices);
        }
    }
}
