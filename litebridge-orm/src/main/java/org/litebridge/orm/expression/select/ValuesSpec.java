package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record ValuesSpec(String tableAlias,
                         String[] labels,
                         @Nullable Object[] values,
                         int rows) implements FromTargetSpec {

    public ValuesSpec(final String tableAlias, final String[] labels, final @Nullable Object[] values) {
        this(tableAlias, labels, values, 1);
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ValuesSpec(String alias, String[] oLabels, Object[] oValues, int oRows))) return false;
        return rows == oRows && Objects.deepEquals(labels, oLabels) && Objects.deepEquals(values, oValues) && Objects.equals(tableAlias, alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableAlias, Arrays.hashCode(labels), values.length, rows);
    }
}
