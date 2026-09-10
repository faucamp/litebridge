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

    public record WhenMatched<T>(@Nullable ConditionGroup and, T operation) {
    }

    public interface WhenMatchedOperation {
    }

    public record MergeUpdate(List<UpdateColumn> columns) implements WhenMatchedOperation {
    }

    public record MergeDelete() implements WhenMatchedOperation {
    }

    public record MergeInsert(List<UpdateColumn> columns, int rows) {
    }
}
