package org.litebridge.orm.spi;

import org.litebridge.db.spi.DatabaseProvider;
import org.litebridge.orm.LitebridgeBuilder;

/**
 * Specialised database provider for a custom Litebridge implementation.
 * <p>
 * This interface should be implemented by database providers that support
 * custom Litebridge types. It provides methods to identify the custom Litebridge
 * class type and optionally construct instances of it.
 * <p>
 * Note that the "built-in" Litebridge variants ({@link org.litebridge.orm.LitebridgeCore}
 * and {@link org.litebridge.orm.Litebridge}) do not require a custom construction implementation.
 *
 * @param <LB> The type of the custom Litebridge implementation supported by this provider.
 */
public interface LitebridgeOverrideDatabaseProvider<LB> extends DatabaseProvider {

    /**
     * Returns the class type of the custom Litebridge implementation supported by this provider.
     *
     * @return The class type of the custom Litebridge implementation.
     */
    Class<LB> litebridgeClass();

    /**
     * Returns an instance of the custom Litebridge implementation supported by this provider.
     * <p>
     * This method is not needed for {@link org.litebridge.orm.Litebridge} or {@link org.litebridge.orm.LitebridgeCore}.
     *
     * @param constructorArgs The constructor arguments for the custom Litebridge implementation.
     * @return An instance of the custom Litebridge implementation.
     */
    default LB createLitebridge(final LitebridgeBuilder.ConstructorArgs constructorArgs) {
        // This method is not needed for Litebridge or LitebridgeCore
        throw new IllegalStateException("LitebridgeOverrideDatabaseProvider does not provide a construction implementation for custom Litebridge type: " + litebridgeClass());
    }
}
