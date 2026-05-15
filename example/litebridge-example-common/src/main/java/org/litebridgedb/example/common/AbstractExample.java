package org.litebridgedb.example.common;

import org.litebridgedb.orm.Litebridge;

public abstract class AbstractExample {

    protected final Litebridge litebridge;

    protected AbstractExample(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    public abstract void run();
}
