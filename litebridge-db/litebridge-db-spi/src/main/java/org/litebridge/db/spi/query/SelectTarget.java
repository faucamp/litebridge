package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;

public sealed interface SelectTarget permits Table, AliasedQuery, AliasedTable, Select, SelectTarget.Void {

    static Void voidTarget() {
        return Void.INSTANCE;
    }

    final class Void implements SelectTarget {
        private static final Void INSTANCE = new Void();
    }
}
