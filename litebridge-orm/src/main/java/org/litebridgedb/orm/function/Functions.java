package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;

public final class Functions {

    private Functions() {
    }

    public static Expression f(final String field) {
        return new SelectField(field);
    }

    public static Expression c(final String column) {
        return c(column, null);
    }

    public static Expression c(final String column, final @Nullable String alias) {
        return new ProtoSelectColumn(column, alias);
    }

    public static Expression count() {
        return new Count();
    }
}
