package org.litebridge.example.h2;

import org.litebridge.orm.Litebridge;

public abstract class AbstractExample {

    protected final Litebridge litebridge;

    protected AbstractExample(final Litebridge litebridge) {
        this.litebridge = litebridge;
    }

    abstract void run();
}
