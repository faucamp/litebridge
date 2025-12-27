package org.litebridge.orm.api.spec;

import org.jspecify.annotations.Nullable;

public interface ColumnSpec {
    String name();

    boolean autoIncrement();

    @Nullable
    String sequence();

    @Nullable
    String joinColumn();
}
