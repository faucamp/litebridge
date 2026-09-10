package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Select;

import java.util.List;

/**
 * Logical representation of a SQL {@code MERGE} statement.
 * <p>
 * It is used by {@link org.litebridge.db.spi.DatabaseProvider} implementations
 * to generate SQL {@code MERGE} statement strings.
 *
 * @param table          merge target table
 * @param usingTable     merge using/source table (if {@code usingSelect} is {@code null})
 * @param usingSelect    merge using/source select statement (if {@code usingTable} is {@code null})
 * @param on             merge condition
 * @param whenMatched    when matched clauses
 * @param whenNotMatched when not matched clauses
 */
public record Merge(Table table,
                    @Nullable Table usingTable,
                    @Nullable Select usingSelect,
                    ConditionGroup on,
                    @Nullable List<WhenMatched<WhenMatchedOperation>> whenMatched,
                    @Nullable List<WhenMatched<MergeInsert>> whenNotMatched) implements UpdateStatement {

    public Merge {
        // Validate parameters
        if (usingTable == null && usingSelect == null) {
            throw new IllegalArgumentException("At least one of usingTable or usingSelect must be specified");
        }

        if (whenMatched == null && whenNotMatched == null) {
            throw new IllegalArgumentException("No WHEN MATCHED/WHEN NOT MATCHED clauses provided");
        }
    }

    /**
     * Logical representation of a merge {@code WHEN MATCHED}/{@code WHEN NOT MATCHED} clause.
     *
     * @param and       the {@code AND} condition for the match clause
     * @param operation the operation to perform when the match clause is satisfied
     * @param <T>       the type of operation
     */
    public record WhenMatched<T>(@Nullable ConditionGroup and, T operation) {
    }

    /**
     * Marker interface for merge {@code WHEN MATCHED} update operations.
     */
    public interface WhenMatchedOperation {
    }

    /**
     * Logical representation of an {@code UPDATE} in a merge {@code WHEN MATCHED} clause.
     *
     * @param columns the columns to update
     */
    public record MergeUpdate(List<UpdateColumn> columns) implements WhenMatchedOperation {
    }

    /**
     * Logical representation of a {@code DELETE} in a merge {@code WHEN MATCHED} clause.
     */
    public record MergeDelete() implements WhenMatchedOperation {
    }

    /**
     * Logical representation of an {@code INSERT} in a merge {@code WHEN NOT MATCHED} clause.
     *
     * @param columns the columns to insert
     * @param rows    the number of rows to insert
     */
    public record MergeInsert(List<UpdateColumn> columns, int rows) {
    }
}
