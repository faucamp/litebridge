package org.litebridge.db.spi.alias;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.query.SelectTarget;

import java.util.Objects;

public record AliasedTable(String alias, Table target) implements Aliased<Table>, SelectTarget {

    @Override
    public boolean equals(final Object o) {
        if (o instanceof Table t) {
            return target.equals(t);
        }

        if (!(o instanceof AliasedTable(String thatAlias, Table thatTarget))) return false;
        return Objects.equals(alias, thatAlias) && Objects.equals(target, thatTarget);
    }
}
