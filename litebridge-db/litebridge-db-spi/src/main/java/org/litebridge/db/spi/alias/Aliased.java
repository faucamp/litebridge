package org.litebridge.db.spi.alias;

public interface Aliased<T> {

    String alias();

    T target();
}
