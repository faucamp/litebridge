package org.litebridge.db.spi.impl.alias;

import org.jspecify.annotations.Nullable;
import org.litebridge.db.spi.alias.AliasTransformer;

/**
 * Implementation of {@code AliasTransformer} that converts database alias strings to uppercase.
 * <p>
 * This class provides a concrete transformation rule for alias strings, converting them to
 * uppercase using the {@code String.toUpperCase()} method. If the input alias is {@code null},
 * the transformation will return {@code null}.
 * <p>
 * This implementation is useful in scenarios where database aliases need to be in a consistent
 * uppercase format, for example, to ensure compatibility with case-sensitive database systems
 * or to follow specific naming conventions.
 *
 * <p>
 * Thread-safety: This class is stateless and therefore thread-safe.
 */
public final class UppercaseAliasTransformer implements AliasTransformer {

    @Override
    public @Nullable String transformAlias(final @Nullable String dbAlias) {
        return dbAlias != null ? dbAlias.toUpperCase() : null;
    }
}
