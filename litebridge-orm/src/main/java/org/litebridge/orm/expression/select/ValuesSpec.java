package org.litebridge.orm.expression.select;

import org.jspecify.annotations.Nullable;

import java.util.Arrays;
import java.util.Objects;

public record ValuesSpec(String tableAlias, String[] labels, @Nullable Object[] values) implements FromTargetSpec {

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof ValuesSpec(String alias, String[] oLabels, Object[] oValues))) return false;
        return Objects.deepEquals(labels, oLabels) && Objects.deepEquals(values, oValues) && Objects.equals(tableAlias, alias);
    }

    @Override
    public int hashCode() {
        return Objects.hash(tableAlias, Arrays.hashCode(labels), Arrays.hashCode(values));
    }
}
