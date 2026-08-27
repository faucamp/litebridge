package org.litebridge.orm.engine.compiler;

import org.jspecify.annotations.Nullable;

final class JoinSpec {

    private final String type;
    private final @Nullable Class<?> dtoClass;
    private final @Nullable String tableName;
    private final ConditionGroupSpecStack conditionGroupSpecStack = new ConditionGroupSpecStack();

    public JoinSpec(final String type,
                    final @Nullable Class<?> dtoClass,
                    final @Nullable String tableName) {
        this.type = type;
        this.dtoClass = dtoClass;
        this.tableName = tableName;
    }

    public @Nullable Class<?> dtoClass() {
        return dtoClass;
    }

    public @Nullable String tableName() {
        return tableName;
    }

    public ConditionGroupSpecStack conditionGroupStack() {
        return conditionGroupSpecStack;
    }
}
