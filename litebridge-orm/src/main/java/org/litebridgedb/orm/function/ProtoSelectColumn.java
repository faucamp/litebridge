package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;

public record ProtoSelectColumn(String column, @Nullable String alias) implements Expression {
}
