package org.litebridge.db.spi.query;

import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.expression.LiteralExpression;

import java.util.List;
import java.util.Objects;
import java.util.StringJoiner;

public final class Values extends VirtualTable {

    private List<LiteralExpression> values;

    public Values(final List<LiteralExpression> values, final String alias) {
        super(alias);
        this.values = values;
    }

    public List<LiteralExpression> values() {
        return values;
    }

    @Override
    public boolean equals(final Object o) {
        if (!(o instanceof final Values values1)) return false;
        if (!super.equals(o)) return false;
        return Objects.deepEquals(values, values1.values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), values);
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Values.class.getSimpleName() + "[", "]")
                .add("alias='" + name() + "'")
                .add("values=" + values)
                .toString();
    }
}
