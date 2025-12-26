package org.litebridge.db.spi.query;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.Aliased;
import org.litebridge.db.spi.Table;

public class SelectField extends Aliased {

    private final Table table;

    public SelectField(final Table table, final String name) {
        this(table, name, null);
    }

    public SelectField(final Table table, final String name, @Nullable final String alias) {
        super(name, alias);
        this.table = table;
    }

    public Table table() {
        return table;
    }
}
