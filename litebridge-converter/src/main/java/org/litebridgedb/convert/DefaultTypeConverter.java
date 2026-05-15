package org.litebridgedb.convert;

import org.litebridgedb.convert.converter.Converter;

import java.util.ServiceLoader;

/**
 * Extends {@link ConfigurableTypeConverter} and automatically populates itself with all
 * {@link Converter} implementations found on the classpath using the Java {@link ServiceLoader} mechanism.
 * <p>
 * This is the implementation typically used by Litebridge database providers to provide a set of standard converters.
 */
public class DefaultTypeConverter extends ConfigurableTypeConverter {

    /**
     * Constructs a new {@code DefaultTypeConverter} and registers all converters found via {@link ServiceLoader}.
     */
    public DefaultTypeConverter() {
        ServiceLoader.load(Converter.class).forEach(this::register);
    }
}
