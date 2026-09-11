package org.litebridge.db.spi;

import org.jspecify.annotations.Nullable;

public interface LitebridgeOverrideDatabaseProvider<LB> extends DatabaseProvider {

    Class<LB> litebridgeClass();

    default LB createLitebridge(final @Nullable Object[] constructorArgs) {
        // This method is not needed for Litebridge or LitebridgeCore
        throw new IllegalStateException("LitebridgeOverrideDatabaseProvider does not provide a construction implementation for custom Litebridge type: " + litebridgeClass());
    }
}
