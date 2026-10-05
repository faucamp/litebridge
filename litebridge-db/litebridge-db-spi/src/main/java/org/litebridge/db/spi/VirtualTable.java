package org.litebridge.db.spi;

import org.litebridge.db.spi.query.Values;

import java.util.StringJoiner;

/**
 * Virtual table
 */
public sealed class VirtualTable extends Table permits Values {

    private static final VirtualTable EMPTY_INSTANCE = new VirtualTable("");

    public VirtualTable(final String alias) {
        super(null, null, alias);
    }

    public static VirtualTable anonymous() {
        return EMPTY_INSTANCE;
    }

    @Override
    public boolean isVirtual() {
        return true;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VirtualTable.class.getSimpleName() + "[", "]")
                .add("alias='" + name() + "'")
                .toString();
    }
}
