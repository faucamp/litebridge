package org.litebridge.db.spi;

public interface LitebridgeOverrideDatabaseProvider<LB> extends DatabaseProvider {

    Class<LB> litebridgeClass();
}
