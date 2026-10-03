package org.litebridge.example.common;

import org.litebridge.orm.LitebridgeCore;

public abstract class AbstractExample {

    protected final LitebridgeCore litebridge;

    protected AbstractExample(final LitebridgeCore litebridge) {
        this.litebridge = litebridge;
    }

    @SuppressWarnings("unused")
    public abstract void run();
}
