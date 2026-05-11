/**
 * Core type conversion system for Litebridge.
 * <p>
 * This package contains the main {@link org.litebridge.db.spi.convert.TypeConverter} implementations,
 * including {@link org.litebridge.convert.ConfigurableTypeConverter} for manual configuration and
 * {@link org.litebridge.convert.DefaultTypeConverter} which automatically loads converters via {@link java.util.ServiceLoader}.
 */
@NullMarked
package org.litebridge.convert;

import org.jspecify.annotations.NullMarked;