package org.litebridge.db.spi.update;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Operation;
import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.ConditionGroup;
import org.litebridge.db.spi.query.Select;

import java.util.List;

public record Merge(Table table,
                    @Nullable Table usingTable,
                    @Nullable Select usingSelect,
                    ConditionGroup on,
                    List<WhenMatched> whenMatched,
                    @Nullable Insert whenNotMatched) implements UpdateStatement {

    public record WhenMatched(@Nullable ConditionGroup and, Operation operation) {
    }
}
