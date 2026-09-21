package org.litebridge.db.spi;

import java.util.StringJoiner;

/**
 * Virtual table
 */
public final class VirtualTable extends Table {

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
