package org.litebridge.example.common;

import org.litebridge.orm.Litebridge;

public abstract class AbstractExample {

    protected final Litebridge litebridge;

    protected AbstractExample(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    public abstract void run();
}
