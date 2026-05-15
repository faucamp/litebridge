/**
 * Core type conversion system for Litebridge.
 * <p>
 * This package contains the main {@link org.litebridgedb.db.spi.convert.TypeConverter} implementations,
 * including {@link org.litebridgedb.convert.ConfigurableTypeConverter} for manual configuration and
 * {@link org.litebridgedb.convert.DefaultTypeConverter} which automatically loads converters via {@link java.util.ServiceLoader}.
 */
@NullMarked
package org.litebridgedb.convert;

import org.jspecify.annotations.NullMarked;