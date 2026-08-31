package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;

final class JoinSpec {

    private final String type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable String tableName;
    private @Nullable Table aliasedTable;
    private final ConditionGroupSpecStack conditionGroupSpecStack = new ConditionGroupSpecStack();

    JoinSpec(final String type,
             final @Nullable Class<?> dtoClass,
             final @Nullable String tableName) {
        this.type = type;
        this.dtoClass = dtoClass;
        this.tableName = tableName;
    }

    @Nullable Class<?> dtoClass() {
        return dtoClass;
    }

    @Nullable String tableName() {
        return tableName;
    }

    ConditionGroupSpecStack conditionGroupStack() {
        return conditionGroupSpecStack;
    }

    public @Nullable Table getAliasedTable() {
        return aliasedTable;
    }

    public void setAliasedTable(final Table aliasedTable) {
        this.aliasedTable = aliasedTable;
    }
}
