package org.litebridge.db.spi;

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
}
