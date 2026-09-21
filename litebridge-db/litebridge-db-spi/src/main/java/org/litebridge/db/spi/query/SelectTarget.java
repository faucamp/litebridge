package org.litebridge.db.spi.query;

import org.litebridge.db.spi.Table;
import org.litebridge.db.spi.VirtualTable;
import org.litebridge.db.spi.alias.AliasedQuery;
import org.litebridge.db.spi.alias.AliasedTable;

public sealed interface SelectTarget permits Table, AliasedQuery, AliasedTable, Select, SelectTarget.Void {

    /**
     * Get the singleton empty `SELECT FROM` target; i.e. omit the FROM clause.
     * <p>
     * This is used for selecting literals or calling specific functions.
     * Most database providers simply omit the `FROM` clause; Oracle outputs `FROM DUAL` instead.
     *
     * @return a singleton representing "no FROM clause"
     */
    static Void voidTarget() {
        return Void.INSTANCE;
    }

    /**
     *
     */
    final class Void implements SelectTarget {
        private static final Void INSTANCE = new Void();
    }
}
