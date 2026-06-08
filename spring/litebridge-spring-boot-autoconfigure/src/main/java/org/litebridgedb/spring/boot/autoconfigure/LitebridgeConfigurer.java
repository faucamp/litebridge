package org.litebridgedb.spring.boot.autoconfigure;

import org.litebridgedb.orm.Litebridge;

/**
 * Provides a mechanism for configuring an instance of Litebridge.
 * <p>
 * Implementations of this interface can define custom configurations
 * to be applied to a Litebridge object (such as registering DTO-table mappings).
 */
public interface LitebridgeConfigurer {

    /**
     * Called when Configures the given instance of Litebridge.
     *
     *
     * @param litebridge the Litebridge instance to configure
     */
    void configure(final Litebridge litebridge);

}
