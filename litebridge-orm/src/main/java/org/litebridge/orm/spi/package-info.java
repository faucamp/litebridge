/**
 * Service Provider Interface (SPI) for expanding the Litebridge API.
 * <p>
 * The SPI allows database providers to specify the methods exposed by Litebridge
 * by limiting or extending the default {@link org.litebridge.orm.LitebridgeCore} instance type
 * returned by the {@link org.litebridge.orm.LitebridgeBuilder}.
 */
package org.litebridge.orm.spi;