package org.litebridgedb.orm.function;

import org.jspecify.annotations.Nullable;
import org.litebridgedb.commons.ObjectUtils;
import org.litebridgedb.db.spi.Column;
import org.litebridgedb.db.spi.Table;

import java.util.Map;
import java.util.function.Function;

final class ProtoExpressionRegistry {

    private static final Map<Class<? extends Expression>, Function<Column, Expression>> columnExpressionRegistry = Map.of(
            Avg.class, Avg::new,
            SelectColumn.class, SelectColumn::new,
            SelectField.class, SelectColumn::new);

    static boolean isSupported(final Class<? extends Expression> type) {
        return columnExpressionRegistry.containsKey(type);
    }

    static Expression resolve(final Class<? extends Expression> type,
                       final Table table,
                       final String columnName,
                       final @Nullable String columnAlias) {
        final Column column = new Column(table, columnName, columnAlias);
        return ObjectUtils.requireNonNull(columnExpressionRegistry.get(type), () -> new IllegalArgumentException("Unsupported expression type: " + type))
                .apply(column);
    }
}
