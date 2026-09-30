package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;

public record ValuesSpec(@Nullable Object[] values) implements FromTargetSpec {
}
