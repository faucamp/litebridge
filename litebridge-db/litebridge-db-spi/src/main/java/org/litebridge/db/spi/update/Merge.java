package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Select;

import java.util.List;

public record Merge(Table table,
                    @Nullable Table usingTable,
                    @Nullable Select usingSelect,
                    ConditionGroup on,
                    @Nullable List<WhenMatched<WhenMatchedOperation>> whenMatched,
                    @Nullable List<WhenMatched<MergeInsert>> whenNotMatched) implements UpdateStatement {

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
