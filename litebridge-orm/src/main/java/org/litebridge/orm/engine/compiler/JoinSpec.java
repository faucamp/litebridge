package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Table;
import org.litebridge.orm.engine.ast.JoinNode;
import org.litebridge.orm.persistence.OrmTable;

final class JoinSpec {

    private final String type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable String tableName;
    private final @Nullable OrmTable ormTable;
    private final JoinNode joinNode;
    private @Nullable Table aliasedTable;
    private final ConditionGroupSpecStack conditionGroupSpecStack = new ConditionGroupSpecStack();

    JoinSpec(final String type,
             final @Nullable Class<?> dtoClass,
             final @Nullable String tableName,
             final @Nullable OrmTable ormTable,
             final JoinNode joinNode) {
        this.type = type;
        this.dtoClass = dtoClass;
        this.tableName = tableName;
        this.ormTable = ormTable;
        this.joinNode = joinNode;
    }

    @Nullable Class<?> dtoClass() {
        return dtoClass;
    }

    @Nullable String tableName() {
        return tableName;
    }

    @Nullable OrmTable ormTable() {
        return ormTable;
    }

    JoinNode joinNode() {
        return joinNode;
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
